/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

ModsDotGroovy.make {
    issueTrackerUrl = 'https://github.com/GroovyMC/groovyduvet/issues'
    license = 'LGPL-3.0-or-later'
    mod {
        modId = 'groovyduvet_groovy'
        version = this.version
        group = this.group
        displayName = 'GroovyDuvet: Dev Environment Groovy Holder'
        description = 'Bundled Groovy libraries, for use in dev environment only.'
        author 'Luke Bemish'
        intermediate_mappings = "net.fabricmc:intermediary"
        dependencies {
            quiltloader = ">=${this.quiltLoaderVersion}"
            minecraft = "~${this.minecraftVersion}"
        }
        displayUrl = 'https://github.com/GroovyMC/groovyduvet'
    }
}