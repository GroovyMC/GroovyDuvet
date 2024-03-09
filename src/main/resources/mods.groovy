FabricModsDotGroovy.make {
    license = 'LGPL-3.0-or-later'
    contact {
        issues = 'https://github.com/GroovyMC/groovyduvet/issues'
        sources = 'https://github.com/GroovyMC/groovyduvet'
    }
    icon = 'assets/groovyduvet/icon.png'
    id = 'groovyduvet'
    version = environmentInfo.version
    name = 'GroovyDuvet'
    description = 'Language adapter and wrapper libraries for Groovy mods on Quilt/Fabric'
    authors {
        person 'Luke Bemish'
    }
    depends {
        fabricloader = ">=${libs.versions.fabric_loader}"
        minecraft = "~${libs.versions.minecraft}"
        groovyduvet_core = ">=${libs.versions.groovyduvet_core}"
        commongroovylibrary = ">=${libs.versions.cgl}"
    }
    custom {
        modmenu = ['badges': ['library']]
    }
}
