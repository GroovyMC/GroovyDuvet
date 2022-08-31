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

package io.github.lukebemish.groovyduvet.core.mappings

import groovy.transform.CompileStatic
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap

@CompileStatic
class MappingMetaClassCreationHandle extends MetaClassRegistry.MetaClassCreationHandle {

    final LoadedMappings mappings

    private static boolean hasWrapped = false

    MappingMetaClassCreationHandle(LoadedMappings mappings) {
        this.mappings = mappings
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        MetaClass delegate = super.createNormalMetaClass(theClass, registry)
        MetaClass wrapped = wrapMetaClass(delegate)
        return wrapped === null ? delegate : wrapped
    }

    private MappingMetaClass wrapMetaClass(MetaClass delegated) {
        if (shouldWrap(delegated.theClass)) {
            // Check if the class is in the remapping key set
            return new MappingMetaClass(delegated, mappings)
        }
        return null
    }

    private shouldWrap(Class clazz) {
        if (clazz==null)
            return false
        if (mappings.mappable.contains(clazz.name))
            return true
        if (shouldWrap(clazz.superclass))
            return true
        for (Class aClass : clazz.interfaces) {
            if (shouldWrap(aClass))
                return true
        }
    }

    static synchronized applyCreationHandle(LoadedMappings mappings) {
        if (!hasWrapped) {
            MetaClassRegistry registry = GroovySystem.metaClassRegistry

            if (mappings === null) throw new IllegalArgumentException("Found uninitialized runtime mappings!")
            hasWrapped = true
            var instance = new MappingMetaClassCreationHandle(mappings)
            registry.metaClassCreationHandle = instance
            synchronized (MetaClassRegistry) {
                Map<Class, MetaClass> queue = new Object2ObjectArrayMap<>()
                for (def it : registry.iterator()) {
                    if (it instanceof MetaClass) {
                        MetaClass wrapped = instance.wrapMetaClass(it)
                        if (wrapped !== null)
                            queue[it.theClass] = wrapped
                    }
                }
                queue.forEach {clazz, metaClazz ->
                    registry.setMetaClass(clazz, metaClazz)
                }
            }
        }
    }
}
