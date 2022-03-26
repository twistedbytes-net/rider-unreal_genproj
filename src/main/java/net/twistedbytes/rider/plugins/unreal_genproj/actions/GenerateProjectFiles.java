package net.twistedbytes.rider.plugins.unreal_genproj.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateProjectFiles extends AnAction {

    private static final Logger log = Logger.getInstance(GenerateProjectFiles.class);

    private static final Pattern RE_ENGINE_PATH = Pattern.compile("^(.+)/Engine/Source/UE[45]Editor\\.Target\\.cs$");
    private static final Pattern RE_UPROJECT = Pattern.compile("^(.+\\.uproject)$");

    private boolean isBusy = false;

    @Override public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(!isBusy);
    }

    @Override public void actionPerformed(@NotNull AnActionEvent event) {
        setBusy(true);

        log.info("Generating Visual Studio project files for UE project ...");

        Project project = getEventProject(event);
        if (project == null) {
            log.error("No project reference found.");
            setBusy(false);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(
                project,
                "Generating Visual Studio project files ...",
                false,
                PerformInBackgroundOption.ALWAYS_BACKGROUND) {
            @Override public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                runBlockingAction(project);
            }
        });
    }

    private void runBlockingAction(Project project) {
        try {
            Module[] modules = ModuleManager.getInstance(project).getSortedModules();
            if (modules.length != 1) {
                StringBuilder sb = new StringBuilder("More than one module found:");
                for (int i = 0; i < modules.length; i++) {
                    Module module = modules[i];
                    sb.append(String.format("Module[%d] projectName<%s> projectFilePath<%s>",
                            i,
                            module.getProject().getName(),
                            module.getProject().getProjectFilePath()));
                }
                log.error(sb.toString());

                setBusy(false);
                return;
            }

            Module firstModule = modules[0];
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(firstModule);

            String engineRootPath = null;
            VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
            for (VirtualFile virtualFile : contentRoots) {
                String path = virtualFile.getCanonicalPath();
                if (path == null) {
                    continue;
                }

                Matcher matcher = RE_ENGINE_PATH.matcher(path);
                if (matcher.matches() && matcher.groupCount() > 0) {
                    engineRootPath = matcher.group(1);
                    break;
                }
            }

            final String uproject = Arrays.stream(contentRoots)
                    .map(VirtualFile::getCanonicalPath)
                    .filter(Objects::nonNull)
                    .filter(RE_UPROJECT.asPredicate())
                    .findFirst()
                    .orElse(null);

            if (engineRootPath == null) {
                log.error("Could not determine engine root path.");
                setBusy(false);
                return;
            }
            log.info("Detected engine root path: " + engineRootPath);

            if (uproject == null) {
                log.error("Could not determine uproject.");
                setBusy(false);
                return;
            }
            log.info("Detected uproject: " + uproject);

            final String binUBT = engineRootPath + "/Engine/Binaries/DotNET/UnrealBuildTool.exe";
            final List<String> command = Arrays.asList(
                    "\"" + binUBT + "\"",
                    "-waitmutex",
                    "-projectfiles",
                    "\"-project=" + uproject + "\"",
                    "-game",
                    "-rocket", // TODO or "-engine" for source builds, See: FDesktopPlatformBase::GenerateProjectFiles
                    "-progress"
            );
            log.info("Running command: " + command);
            ProcessBuilder builder = new ProcessBuilder(command);
            try {
                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info(line);
                    }
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.error(line);
                    }
                }

                try {
                    // TODO run process in another thread and track it with a nice progress bar
                    final int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        log.info("Project files successfully regenerated. Refreshing open project ...");
                        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                    } else {
                        final String msg = String.format("Failed to regenerate project files (Code: %d). See 'idea.log' for more details.", exitCode);
                        log.error(msg);
                        Messages.showErrorDialog(
                                msg,
                                "Generate Visual Studio project files ..."
                        );

                    }
                } catch (InterruptedException e) {
                    log.error(e);
                }
            } catch (IOException ex) {
                log.error(ex);
            }

            setBusy(false);

        } catch (Exception e) {
            setBusy(false);
            throw new RuntimeException("Failed to execute the command!", e);
        }
    }

    private void setBusy(boolean isBusy) {
        this.isBusy = isBusy;
    }

}
