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

package io.github.lukebemish.groovyduvet.core.impl.mappings

import groovy.transform.CompileStatic

@CompileStatic
class LoadedMappings {
    // runtime class name (with dots) to map of moj -> runtime names
    final Map<String, Map<String, List<String>>> methods
    final Map<String, Map<String, String>> fields
    final Set<String> mappable

    LoadedMappings(Map<String, Map<String, List<String>>> methods, Map<String, Map<String, String>> fields) {
        this.methods = methods
        this.fields = fields

        List<String> emptyRemovalQueue = []
        methods.each (className,methodMap) -> {
            List<String> noKnownMappingsRemovalQueue = []
            methodMap.forEach(official,srg) -> {
                List<String> unnecessaryRemovalQueue = []
                srg.forEach (a)->{
                    if (official==a) unnecessaryRemovalQueue.add(a)
                }
                unnecessaryRemovalQueue.each {srg.remove it}
                if (srg.isEmpty()) noKnownMappingsRemovalQueue.add(official)
            }
            noKnownMappingsRemovalQueue.each {methodMap.remove it}

            if (methodMap.isEmpty()) {
                emptyRemovalQueue.add(className)
            }
        }
        emptyRemovalQueue.each {methods.remove it}

        emptyRemovalQueue.clear()
        fields.forEach (className,fieldMap) -> {
            List<String> unnecessaryRemovalQueue = []
            fieldMap.forEach (official,srg) -> {
                if (official==srg) unnecessaryRemovalQueue.add(official)
                return
            }
            unnecessaryRemovalQueue.each {fieldMap.remove it}

            if (fieldMap.isEmpty()) {
                emptyRemovalQueue.add(className)
            }
        }
        emptyRemovalQueue.each {fields.remove it}

        this.mappable = methods.keySet() + fields.keySet()
    }
}
