# TestFairy plugin for Android Studio / IDEA

[![Build Status](https://magnum.travis-ci.com/testfairy/testfairy-android-studio-plugin.svg?token=dEpogdhSnM976NXR45qA&branch=master)](https://magnum.travis-ci.com/testfairy/testfairy-android-studio-plugin)

## Requirements
TestFairy Integration intellij plugin has been tested on IDEA 13 and Android Studio 1.1+ (Android Studio will be used in further text for brevity).

It is based on [TestFairy plugin for Gradle](https://github.com/testfairy/testfairy-gradle-plugin),
so it requires a Gradle based Android application module.

At the moment, we only support Android Studio projects with settings.gradle in project root
and Android application module included on its very first line.

## Plugin Setup
The plugin is available in [JetBrains repo](https://plugins.jetbrains.com/plugin/7845?pr=), so it can be found using Preferences/Plugins/Browse repositories in your IDE.

Alternatively, you can manually install the latest release:

1. Download plugin installation from: [TestFairy Integration plugin releases](https://github.com/testfairy/testfairy-android-studio-plugin/releases)
1. Go to Android Studio Preferences (Mac OS: Android Studio/Preferences; Others: File/Settings), and type Plugins.
1. Click on 'Install plugin from disk...'.
1. Locate the downloaded file and select it.

## Usage
To upload the android app to TestFairy, use Tools/TestFairy/Upload to TestFairy or click on TestFairy toolbar icon near the Build and Run buttons.

The first time you do it, the plugin will prompt you for your IDE Master Password (if you have one) and TestFairy API key.
After that, your project will be configured for integrated uploads to TestFairy.

In order to change TestFairy API key, you can always go to Tools/TestFairy/Settings.
The plugin will always apply the most recently entered API key to your projects.


