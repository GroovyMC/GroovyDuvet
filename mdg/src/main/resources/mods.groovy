/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

ModsDotGroovy.make {
    issueTrackerUrl = 'https://github.com/GroovyMC/groovyduvet/issues'
    license = 'MIT'
    mod {
        modId = 'groovyduvet_wrapper_mdg'
        version = this.version
        group = this.group
        displayName = 'GroovyDuvet: ModsDotGroovy Wrappers'
        description = 'Wrapper module for GroovyDuvet that relocates and bundles the mods.groovy DSL'
        author 'Luke Bemish'
        author 'Matyrobbrt'
        author 'GroovyMC'
        intermediate_mappings = "net.fabricmc:intermediary"
        displayUrl = 'https://github.com/GroovyMC/groovyduvet'
    }
    modmenu = [
            'badges':['library'],
            'parent':[
                    'id':'groovyduvet',
                    'name':'GroovyDuvet',
                    'description':'Language adapter and wrapper libraries for Groovy mods on Quilt',
                    'icon':'assets/groovyduvet/icon.png',
                    'badges':['library']
            ]
    ]
}