package io.github.lukebemish.groovyduvet.wrapper.minecraft.extension.chat

import groovy.transform.CompileStatic
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

@CompileStatic
class ComponentExtension {
    static MutableComponent plus(MutableComponent self, Component component) {
        return self.append(component)
    }

    static MutableComponent plus(MutableComponent self, String component) {
        return self.append(component)
    }

    static MutableComponent leftShift(MutableComponent self, Style style) {
        self.withStyle(style)
        return self
    }

    static MutableComponent leftShift(MutableComponent self, ChatFormatting style) {
        self.withStyle(style)
        return self
    }

    static MutableComponent leftShift(MutableComponent self, List<ChatFormatting> style) {
        self.withStyle(style.<ChatFormatting>toArray(new ChatFormatting[0]))
        return self
    }
}
