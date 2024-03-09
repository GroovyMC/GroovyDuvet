package org.groovymc.groovyduvet.wrapper.extension.event

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.fabricmc.fabric.api.event.Event
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

@CompileStatic
class EventPhase<T> {
    private final Event<T> event
    private final ResourceLocation phase

    @PackageScope
    EventPhase(Event<T> event, ResourceLocation phase) {
        this.event = event
        this.phase = phase
    }

    @ApiStatus.Experimental
    void leftShift(T toRegister) {
        this.event.register(this.phase, toRegister)
    }
}
