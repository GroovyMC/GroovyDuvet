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

package io.github.lukebemish.groovyduvet.wrapper.minecraft.extension.registry

import groovy.transform.CompileStatic
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

@CompileStatic
class RegistryExtension {
    static <A> A getAt(Registry<A> self, ResourceLocation name) {
        return self.get(name)
    }

    static <A> A getAt(Registry<A> self, ResourceKey<A> key) {
        return self.get(key)
    }

    static <A> void putAt(Registry<A> self, ResourceLocation name, A value) {
        Registry.register(self, name, value)
    }

    static <A> void putAt(Registry<A> self, ResourceKey<A> key, A value) {
        Registry.register(self, key, value)
    }

    static <A> ResourceLocation getAt(Registry<A> self, A member) {
        return self.getKey(member)
    }
}
