/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

ModsDotGroovy.make {
    issueTrackerUrl = 'https://github.com/GroovyMC/groovyduvet/issues'
    license = 'LGPL-3.0-or-later'
    mod {
        modId = 'groovyduvet_core'
        version = this.version
        group = this.group
        displayName = 'GroovyDuvet: Core'
        description = 'Core module for GroovyDuvet'
        author 'Luke Bemish'
        intermediate_mappings = "net.fabricmc:intermediary"
        language_adapters = [
                'groovyduvet': 'io.github.lukebemish.groovyduvet.core.impl.GroovyAdapter'
        ]
        dependencies {
            quiltLoader = ">=${this.quiltLoaderVersion}"
            minecraft = "~${this.minecraftVersion}"
        }
        displayUrl = 'https://github.com/GroovyMC/groovyduvet'
        entrypoints {
            pre_launch = [
                    adapted {
                        adapter = 'groovyduvet'
                        value = 'io.github.lukebemish.groovyduvet.core.impl.DevExtensionLoader'
                    },
                    adapted {
                        adapter = 'groovyduvet'
                        value = 'io.github.lukebemish.groovyduvet.core.impl.mappings.MetaclassMappingsProvider'
                    }
            ]
        }
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
    mixin = "groovyduvet.mixin.json"
}