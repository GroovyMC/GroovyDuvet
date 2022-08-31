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

package io.github.lukebemish.groovyduvet.wrapper.qsl.extension.event

import groovy.transform.CompileStatic

import net.minecraft.resources.ResourceLocation
import org.quiltmc.qsl.base.api.event.Event

@CompileStatic
class EventExtension {
    static <T> T call(Event<T> event) {
        return event.invoker()
    }

    static <T> void add(Event<T> event, T toRegister) {
        Class<? super T> clazz = event.type
        event.register((T) toRegister.asType(clazz))
    }

    static <T> void leftShift(Event<T> event, T toRegister) {
        add(event, toRegister)
    }

    static <T> void add(Event<T> event, ResourceLocation phase, T toRegister) {
        Class<? super T> clazz = event.type
        event.register(phase, (T) toRegister.asType(clazz))
    }
}
