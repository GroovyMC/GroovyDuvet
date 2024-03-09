FabricModsDotGroovy.make {
    license = 'LGPL-3.0-or-later'
    id = 'groovyduvet_test'
    version = environmentInfo.version
    name = 'GroovyDuvet - Test'
    depends {
        groovyduvet = ">=${environmentInfo.version}"
    }
    entrypoints {
        entrypoint('main') {
            adapter = 'groovyduvet'
            value = 'org.groovymc.groovyduvet.test.TestMod'
        }
    }
}
