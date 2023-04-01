/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

ModsDotGroovy.make {
    issueTrackerUrl = 'https://github.com/GroovyMC/groovyduvet/issues'
    license = 'LGPL-3.0-or-later'
    mod {
        modId = 'groovyduvet_wrapper_qsl'
        version = this.version
        group = this.group
        displayName = 'GroovyDuvet: QSL Wrappers'
        description = 'Wrapper module for GroovyDuvet with wrappers for the Quilt Standard Libraries'
        author 'Luke Bemish'
        intermediate_mappings = "net.fabricmc:intermediary"
        dependencies {
            quiltLoader = ">=${this.quiltLoaderVersion}"
            minecraft = "~${this.minecraftVersion}"
            groovyduvet_core = ">=${this.version}"
        }
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