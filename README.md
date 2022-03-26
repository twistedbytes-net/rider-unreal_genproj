# Unreal GenProj

![Build](https://github.com/twistedbytes-net/rider-unreal_genproj/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/18845.svg)](https://plugins.jetbrains.com/plugin/18845)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/18845.svg)](https://plugins.jetbrains.com/plugin/18845)

<!-- Plugin description -->

Adds a new toolbar button which will allow you to generate Visual Studio project files for your Unreal Engine project without having to leave the IDE.

![New toolbar button](https://github.com/twistedbytes-net/rider-unreal_genproj/raw/master/docs/preview-toolbar-button.png)

# Use-case 1: Checking out another Git branch

You checked out to another Git branch. This action can lead to outdated generated source files and thus manifests in some of your source files simply not being visible in Rider until you decide to (re)generate project files.

In order to generate project files, you would usually locate the `.uproject` file of the project you are currently working with in Rider and then right-click it to display the context menu.

![Context Menu: Generate Visual Studio project files](https://github.com/twistedbytes-net/rider-unreal_genproj/raw/master/docs/generate-project-files-context-menu.png)

You should see a menu item called `Generate Visual Studio project files`. After selecting it, a dialog will appear that shows the progress of the generation process.

![Dialog: Generate Visual Studio project files](https://github.com/twistedbytes-net/rider-unreal_genproj/raw/master/docs/generate-project-files-dialog.png)

When all project files have been generated this dialog will disappear. Now, when switching back to the Rider window again it should detect that it needs to reload the open project. Afterwards all of your source files should correctly show up in the *Project* tool window.    

# Use-case 2: External changes (outside Rider)

You moved some source files around to other directories, renamed existing files and/or added new ones. All these actions have to be considered as external changes. Rider will not (fully) know about external changes (new files), but it surely will complain about missing files (renamed, moved, or deleted) that it expects to exist.

In the end this use-case is very similar to use-case 1. This is because Git is also applying external changes to your source files when you check out to another branch.

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Unreal GenProj"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/twistedbytes-net/rider-unreal_genproj/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
