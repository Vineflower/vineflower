# Quiltflower

Quiltflower is a modern, general purpose JVM language decompiler focused on providing the best quality, speed, and usability.

Quiltflower's features include:
- Java 20+ support, including records, sealed classes, switch expressions, and more
- Kotlin decompilation support, where Kotlin classes will be decompiled into Kotlin instead of Java
- A robust [plugin system](needs wiki page), allowing developers to integrate custom decompilation logic
- Clean code generation and output, with automatic output formatting
- Multithreaded decompilation

Examples of Quiltflower's output, compared to other decompilers, can be found on [the wiki.](https://github.com/QuiltMC/quiltflower/wiki)

## Use
Want to use Quiltflower? There are a few ways! For Fabric and Architectury projects, [Loom Quiltflower](https://github.com/Juuxel/LoomQuiltflower) allows you to run genSources with Quiltflower.
The [Quiltflower Intellij IDEA plugin](https://plugins.jetbrains.com/plugin/18032-quiltflower) replaces Fernflower in IDEA with Quiltflower, and allows you to modify its settings.

If you want to run Quiltflower from the commandline, head over to the [Releases tab](https://github.com/QuiltMC/quiltflower/releases) and grab the latest release.
You can then run Quiltflower with `java -jar quiltflower.jar <arguments> <source> <destination>`.
`<arguments>` is the list of [commandline arguments](https://github.com/QuiltMC/quiltflower/wiki) that you want to pass to the decompiler.
`<source>` can be a jar, zip, folder, or class file, and `<destination>` can be a folder, zip, jar, or excluded, to print to the console.


To use Quiltflower as a library, you can find distributions on [Quilt's maven](https://maven.quiltmc.org/repository/release/) or on maven central.
Quiltflower can be imported with gradle with:
```groovy
dependencies {
    implementation 'org.quiltmc:quiltflower:<version>'
}
```
Instructions on how to interface with Quiltflower can be found on [the wiki.](https://github.com/QuiltMC/quiltflower/wiki)

Make sure to report any issues to the [Issues tab!](https://github.com/QuiltMC/quiltflower/issues)

### Building
Quiltflower can be built simply with `./gradlew build`.

### Support
For support or questions, please feel free to discuss with us at the [Quilt toolchain discord](https://discord.quiltmc.org/toolchain), or on the [Quiltflower Discussion tab](https://github.com/QuiltMC/quiltflower/discussions).

## Contributing
Contributions are always welcome! We are always looking for help with bugfixes, new features, and enhancements. If you'd like to work on a feature or bugfix, feel free to simply open a PR! If you want to communicate about a change before making it, you can get in touch with the methods listed above.
Quiltflower's codebase is rather old and some concepts may be difficult to navigate. For help, please check out [CONTRIBUTING.md](./CONTRIBUTING.md) and [ARCHITECTURE.md](./ARCHITECTURE.md).

### Special Thanks
Quiltflower is a fork of both Jetbrains' Fernflower and MinecraftForge's ForgeFlower.

* Jetbrains- For maintaining Fernflower
* Forge Team- For maintaining ForgeFlower
* CFR- For it's large suite of very useful tests