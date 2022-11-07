# Mod Menu - BTA Port

[![](https://jitpack.io/v/Turnip-Labs/ModMenu.svg)](https://jitpack.io/#Turnip-Labs/ModMenu)

~~Hard to be more descriptive than that.~~ It enriches the standard Minecraft menu with an interface displaying a one-dimensional array of modifications

A picture's worth 2 words

![](https://i.imgur.com/BvJYJ1C.png "Mod Menu")

### Installation on [Better than Adventure](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/3106066-better-than-adventure-for-beta-1-7-3-timely)
1. Download and install [Prism Launcher](https://prismlauncher.org/download/) if you haven't already.
2. Press "create instance", and press "import from zip", and select the instance zip which can be downloaded [here](https://drive.google.com/file/d/1V6nHw_uErtckjTWjfbmX2_qebeTXLbQV/view?usp=sharing).
3. Download ModMenu from the [releases page](https://github.com/Turnip-Labs/ModMenu/releases).
4. Click on your new Prism Launcher instance and click "edit instance" on the right. Click "loader mods" then "add", and navigate to the mod you just downloaded, and press OK.

### Developers:
- You can obtain the latest build via [JitPack](https://jitpack.io/). Add following to your project's `build.gradle`:
  ```groovy
  repositories {
      // ...
      maven {
          name = 'JitPack'
          url = 'https://jitpack.io/'
      }
  }
  
  dependencies {
      // ...
      modImplementation "com.github.Turnip-Labs:ModMenu:<VERSION>"
  }
  ```
  Replace the version with the latest version, which you can find above.
- The icon comes from the icon specified in your fabric.mod.json (as per the spec)
- Clientside-only and API badges are defined as custom objects in your `fabric.mod.json` as such:
```json
"custom": {
    "modmenu:api": true,
    "modmenu:clientsideOnly": true
}
```
- Mod parenting is used to display a mod as a child of another one. This is meant to be used for mods divided into different modules. The following element in a `fabric.mod.json` will define the mod as a child of the mod 'flamingo':
```json
"custom": {
    "modmenu:parent": "flamingo"
}
```
- ModMenuAPI
    - To use the API, implement the `ModMenuApi` interface on a class and add that as an entry point of type `modmenu` in your `fabric.mod.json` as such:
  ```json
  "entrypoints": {
  	"modmenu": [ "com.example.mod.ExampleModMenuApiImpl" ]
  }
  ```
    - Features
        - Mods can provide a Screen factory to provide a custom config screen to open with the config button. Implement the `getConfigScreenFactory` method in your API implementation.
