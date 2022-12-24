/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

ModsDotGroovy.make {
    issueTrackerUrl = 'https://github.com/GroovyMC/groovyduvet/issues'
    license = 'LGPL-3.0-or-later'
    mod {
        modId = 'groovyduvet_test'
        version = this.version
        group = this.group
        displayName = 'GroovyDuvet Test Mod'
        description = 'Test Mod for GroovyDuvet'
        author 'Luke Bemish'
        intermediate_mappings = "net.fabricmc:intermediary"
        dependencies {
            quiltloader = ">=${this.quiltLoaderVersion}"
            minecraft = "~${this.minecraftVersion}"
            groovyduvet = ">=${this.version}"
        }
        displayUrl = 'https://github.com/GroovyMC/groovyduvet'
        entrypoints {
            client_init = adapted {
                adapter = 'groovyduvet'
                value = 'io.github.lukebemish.groovyduvet.test.TestMod'
            }
        }
    }
}