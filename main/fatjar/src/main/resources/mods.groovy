/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

ModsDotGroovy.make {
    issueTrackerUrl = 'https://github.com/GroovyMC/groovyduvet/issues'
    license = 'LGPL-3.0-or-later'
    mod {
        modId = 'groovyduvet'
        version = this.version
        group = this.group
        displayName = 'GroovyDuvet'
        description = 'Language adapter and wrapper libraries for Groovy mods on Quilt'
        author 'Luke Bemish'
        intermediate_mappings = "net.fabricmc:intermediary"
        dependencies {
            quiltLoader = ">=${this.quiltLoaderVersion}"
            minecraft = "~${this.minecraftVersion}"
            groovyduvet_core = ">=${this.version}"
            commongroovylibrary = ">=${this.libs.versions.cgl}"
            groovyduvet_wrapper_qsl = ">=${this.version}"
        }
        displayUrl = 'https://github.com/GroovyMC/groovyduvet'
        logoFile = 'assets/groovyduvet/icon.png'
    }
    modmenu = ['badges':['library']]
}