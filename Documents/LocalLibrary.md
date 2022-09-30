# Local Library

Memo about make local library for Android Studio

## Environment

- Android Studio Chipmunk | 2021.2.1 Patch 2
- SDK 32; minimum SDK 30
- Kotlin version: 1.7.10

## Directory structure

> **MyFirstApp**
> - **build.gradle**: project gradle script
> - **settings.gradle**: project gradle configuration file
> - **app**: main app
> - **app/src**: main app source
> - **app/build.gradle**: main app module gradle script
> - **Libraries**: libraries
>   - **SomeLib**: a library
>   - **SomeLib/src**: library source
>   - **SomeLib/build.gradle**: library module gradle script

## Configuration

- Add 2 lines into **settings.gradle**:
```
include ':SomeLib'
project(':SomeLib').projectDir = new File(rootProject.projectDir, 'Libraries/SomeLib')
```

- Add line to **app/build.gradle**:
```
dependencies {
  ...
  implementation project(':SomeLib')
}
```

- **Libraries/SomeLib/build.gradle** should be similar to **app/build.gradle**, excepts:
```
plugins {
  id 'com.android.library' // instead of 'com.android.application'
  ...
}

android {
  compileSdk 32

  defaultConfig {
    // remove line `applicationId ...`
    ...
  }

  ...
}

dependencies {
  // dependencies of library go here
}
```
