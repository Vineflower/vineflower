# Vineflower

Vineflower is a modern, general purpose JVM language decompiler focused on providing the best quality, speed, and usability.

Vineflower's features include:
- Java 21+ support, including records, sealed classes, switch expressions, pattern matching, and more
- Clean code generation and output, with automatic output formatting
- Multithreaded decompilation

Examples of Vineflower's output, compared to other decompilers, can be found on [the wiki.](https://github.com/Vineflower/vineflower/wiki)

## Use
Want to use Vineflower? There are a few ways! The [Vineflower Intellij IDEA plugin](https://plugins.jetbrains.com/plugin/18032-quiltflower) replaces Fernflower in IDEA with Vineflower, and allows you to modify its settings.

If you want to run Vineflower from the commandline, head over to the [Releases tab](https://github.com/Vineflower/vineflower/releases) and grab the latest release.
You can then run Vineflower with `java -jar vineflower.jar <arguments> <source> <destination>`.
`<arguments>` is the list of [commandline arguments](https://github.com/Vineflower/vineflower/wiki) that you want to pass to the decompiler.
`<source>` can be a jar, zip, folder, or class file, and `<destination>` can be a folder, zip, jar, or excluded to print to the console.

To use Vineflower as a library, you can find distributions on maven central. Vineflower 1.9+ requires Java 11 or higher to run, and Vineflower 1.11+ requires Java 17 or higher to run.
Vineflower can be imported with gradle with:
```groovy
dependencies {
    implementation 'org.vineflower:vineflower:<version>'
}
```
Vineflower 1.10+ provides two artifacts, the regular jar and the slim jar. The regular jar contains the decompiler and a handful of plugins, such as a Kotlin decompiler and variable renamer.
The slim jar contains only the decompiler. If unsure, simply use the regular jar as before.

Instructions on how to interface with Vineflower can be found on [the wiki.](https://github.com/Vineflower/vineflower/wiki)

Please report any issues to the [Issues tab!](https://github.com/Vineflower/vineflower/issues)

### Building
Vineflower can be built simply with `./gradlew build`.

### Support
For support or questions, please join one of the listed [social platforms](https://github.com/Vineflower), or on the [discussion tab](https://github.com/Vineflower/vineflower/discussions).

## Contributing
Contributions are always welcome! We are always looking for help with bugfixes, new features, and enhancements. If you'd like to work on a feature or bugfix, feel free to simply open a PR! If you want to communicate about a change before making it, you can get in touch with the methods listed above.
Vineflower's codebase is rather old and some concepts may be difficult to navigate. For help, please check out [CONTRIBUTING.md](./CONTRIBUTING.md) and [ARCHITECTURE.md](./ARCHITECTURE.md).

### Pull Requests
When submitting pull requests, please target the latest `develop/1.xx.y` branch.

### Special Thanks
Vineflower is a fork of Jetbrains' Fernflower, MinecraftForge's ForgeFlower, FabricMC's fork of Fernflower, and a direct continuation of work on Quiltflower.

* Stiver- For creating Fernflower
* Jetbrains- For maintaining Fernflower
* Forge Team- For maintaining ForgeFlower
* Fabric Team- For maintaining Fabric's fork of Fernflower
* CFR- For its large suite of very useful tests