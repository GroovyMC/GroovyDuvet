package io.github.lukebemish.groovyduvet.wrapper.minecraft.extension.chat

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.chat.StyleBuilder
import net.minecraft.network.chat.Style

@CompileStatic
@AutoFinal
class StyleExtension {
    static Style of(Style type,
                    @DelegatesTo(value = StyleBuilder, strategy = Closure.DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'io.github.lukebemish.groovyduvet.wrapper.minecraft.api.chat.StyleBuilder')
                    Closure closure) {
        final builder = new StyleBuilder()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(builder)
        return builder.style
    }
}
