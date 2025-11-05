# Vineflower

Vineflower is a modern general purpose Java & JVM language decompiler focused on providing the best quality, speed, and usability.

Vineflower's features include:
- Java 21+ support, including records, sealed classes, switch expressions, pattern matching, and more
- Clean code generation and output, with automatic output formatting
- Multithreaded decompilation

Examples of Vineflower's output compared to other decompilers can be found on [the website](https://vineflower.org/output-comparison/).

## Use

Vineflower can be used from the console or as a library. To run Vineflower from the command line, download the latest release from the [Releases tab](https://github.com/Vineflower/vineflower/releases).
You can then run Vineflower with `java -jar vineflower.jar <arguments> <source> <destination>`.
`<arguments>` is the list of [commandline arguments](https://vineflower.org/usage/) that you want to pass to the decompiler.
`<source>` can be a jar, zip, folder, or class file, and `<destination>` can be a folder, zip, jar, or omitted to print to the console.

To use Vineflower as a library, you can find distributions on maven central. Vineflower 1.9+ requires Java 11 or higher to run, and Vineflower 1.11+ requires Java 17 or higher to run.
Vineflower can be addded as a dependency in gradle with:
```groovy
dependencies {
    implementation 'org.vineflower:vineflower:<version>'
}
```

More instructions on how to interface with Vineflower can be found on [the website](https://vineflower.org/usage-code/).

For IDE use, the [Vineflower Intellij IDEA plugin](https://plugins.jetbrains.com/plugin/18032-quiltflower) replaces Fernflower in IDEA with Vineflower.

Please report any issues to the [Issues tab!](https://github.com/Vineflower/vineflower/issues)

### Building
Vineflower can be built simply with `./gradlew build`.

### Support
For support or questions, please join one of the listed [social platforms](https://vineflower.org/socials/).

## Contributing
Contributions are always welcome! [The website](https://vineflower.org/development/) has detailed instructions on how to set up Vineflower development, as well as information on debugging.
When submitting pull requests, please target the latest `develop/1.xx.y` branch.

### Special Thanks
Vineflower is a fork of Jetbrains' Fernflower, MinecraftForge's ForgeFlower, FabricMC's fork of Fernflower, and a direct continuation of work on Quiltflower. Special thanks to:

* [Stiver](https://blog.jetbrains.com/idea/2024/11/in-memory-of-stiver/), for creating Fernflower
* JetBrains, for maintaining Fernflower
* MinecraftForge Team, for maintaining ForgeFlower
* FabricMC Team, for maintaining Fabric's fork of Fernflower
* CFR, for its large suite of very useful tests