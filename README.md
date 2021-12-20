# TestFairy plugin for Android Studio / IntelliJ IDEA

[![Build Status](https://travis-ci.org/testfairy/testfairy-android-studio-plugin.svg?branch=master)](https://travis-ci.org/testfairy/testfairy-android-studio-plugin)

## Installation

You can install the `TestFairy Integration` plugin either manually by downloading it from the [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/7845) or directly from within your IDE by following [these instructions] (https://docs.testfairy.com/Android/Uploading_with_Android_Studio.html).

# Development

```bash
# Build
docker run -it --rm -v `pwd`:`pwd` -w `pwd` --env TF_API_KEY=<yourkey> androidsdk/android-30:latest bash scripts/build.sh

# Use built binary
ls -a ./TestFairyAndroidStudioPlugin.zip
```

Find `TestFairyAndroidStudioPlugin.zip` in the project root and install it to your Android Studio locally.

```bash
# Launch development environment
docker run -it --rm -v `pwd`:`pwd` -w `pwd` androidsdk/android-30:latest bash

# inside docker
    # Attempt first build and prepare environment
    bash scripts/build.sh
    
    # Develop new features
    
    # Build again
    bash scripts/build.sh
    
    # Locate TestFairyAndroidStudioPlugin.zip in project root and install it to your Android Studio 

    # Quit docker and REMOVE environment while keeping the changes in source code
    exit
    
# Push
git ...
```

## Publish

- Create a release and wait for CI to upload to central repository.

## Bugs

Please report problems on [GitHub Issues](https://github.com/testfairy/testfairy-android-studio-plugin/issues).

## License

Plugin and sources are available under the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
