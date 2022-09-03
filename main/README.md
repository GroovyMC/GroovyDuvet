# GroovyDuvet
[![Version](https://img.shields.io/badge/dynamic/xml?style=for-the-badge&color=blue&label=Latest%20Version&prefix=v&query=metadata%2F%2Flatest&url=https%3A%2F%2Fmaven.moddinginquisition.org%2Freleases%2Fio%2Fgithub%2Flukebemish%2Fgroovyduvet%2Fgroovyduvet%2Fmaven-metadata.xml)](https://maven.moddinginquisition.org/#/releases/io/github/lukebemish/groovyduvet/groovyduvet)
[![Javadoc](https://img.shields.io/badge/dynamic/xml?style=for-the-badge&color=blue&label=Groovydocs&prefix=v&query=metadata%2F%2Flatest&url=https%3A%2F%2Fmaven.moddinginquisition.org%2Freleases%2Fio%2Fgithub%2Flukebemish%2Fgroovyduvet%2Fgroovyduvet%2Fmaven-metadata.xml)](https://maven.moddinginquisition.org/javadoc/releases/io/github/lukebemish/groovyduvet/groovyduvet/latest)

GroovyDuvet is a Quilt language adapter for mods written in Groovy. It allows the use of scripts, classes, fields, or methods as entrypoints, provides
mappings at runtime for dynamically compiled code, and provides wrappers and DSLs around Minecraft and QSL code for ease of development.

## Using

To use, add the following to your `build.gradle`:
```gradle
repositories {
    maven {
        url "https://maven.moddinginquisition.org/releases"
    }
}

dependencies {
    modImplementation 'io.github.lukebemish.groovyduvet:groovyduvet:<version>'
}
```

Then, use the `groovyduvet` language adapter for your groovy entrypoints. Entrypoints can target classes, static fields, or static methods that extend
the entrypoint type just like usual; GroovyDuvet can also target local variables inside of scripts. The script will be run, and the variable will be
extracted. If the entrypoint class has exactly one abstract method, GroovyDuvet can also target closures, through any of the previous methods, or entire
scripts, where the method arguments are fed into the script as local variables going `arg0`, `arg1`, etc.

For a template mod using GroovyDuvet, see: [quilt-template-mod-groovy](https://github.com/lukebemish/quilt-template-mod-groovy/).

## Included Groovy Modules

 * stdlib
 * contracts
 * datetime
 * nio
 * macro
 * macro-library
 * templates
 * typecheckers
 * dateutil
 * ginq
 * toml
 * json

## Support
If you are having issues using GroovyDuvet or wish to discuss contributions to the project, you may either open an issue or PR on this repo or go to the `groovy-mc` channel here: <https://discord.gg/Em7b3dv4Nk>
