/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
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
