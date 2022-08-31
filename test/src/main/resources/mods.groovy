/*
 * Copyright (C) 2022 Luke Bemish
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */

ModsDotGroovy.make {
    issueTrackerUrl = 'https://github.com/lukebemish/groovyduvet/issues'
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
            quiltLoader = ">=${this.quiltLoaderVersion}"
            minecraft = "~${this.minecraftVersion}"
            groovyduvet = ">=${this.version}"
        }
        displayUrl = 'https://github.com/lukebemish/groovyduvet'
        entrypoints {
            client_init = adapted {
                adapter = 'groovyduvet'
                value = 'io.github.lukebemish.groovyduvet.test.TestMod'
            }
        }
    }
}