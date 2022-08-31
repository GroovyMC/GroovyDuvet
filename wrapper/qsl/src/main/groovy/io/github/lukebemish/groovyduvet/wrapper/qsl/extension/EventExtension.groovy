package io.github.lukebemish.groovyduvet.wrapper.qsl.extension

import groovy.transform.CompileStatic
import org.quiltmc.qsl.base.api.event.Event

@CompileStatic
class EventExtension {
    static <T> T call(Event<T> event) {
        return event.invoker()
    }
}
