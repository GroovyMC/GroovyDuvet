package org.groovymc.groovyduvet.wrapper.extension.event

import dev.lukebemish.autoextension.AutoExtension
import groovy.transform.CompileStatic
import net.fabricmc.fabric.api.event.Event
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

@CompileStatic
@AutoExtension
class EventExtension {
    static <T> T call(Event<T> event) {
        return event.invoker()
    }

    @ApiStatus.Experimental
    static <T> Event<T> leftShift(Event<T> event, T toRegister) {
        event.register(toRegister)
        event.invoker()
        return event
    }

    static <T> EventPhase<T> getAt(Event<T> event, ResourceLocation phase) {
        return new EventPhase<>(event, phase)
    }
}
