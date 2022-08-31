package io.github.lukebemish.groovyduvet.wrapper.minecraft.api.chat

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.ResourceLocation

class StyleBuilder {
    Style style = Style.EMPTY

    void setStyle(Style style) {
        this.style = style
    }

    void setStyle(ChatFormatting style) {
        this.style = Style.EMPTY.applyFormat(style)
    }

    void setStyle(List<ChatFormatting> styles) {
        this.style = Style.EMPTY.applyFormats(styles.<ChatFormatting>toArray(new ChatFormatting[]{}))
    }

    void style(Style style) {
        this.style = style.applyTo(this.style)
    }

    void style(ChatFormatting style) {
        this.style = this.style.applyFormat(style)
    }

    void style(List<ChatFormatting> styles) {
        this.style = this.style.applyFormats(styles.<ChatFormatting>toArray(new ChatFormatting[]{}))
    }

    void style(ChatFormatting[] styles) {
        this.style = this.style.applyFormats(styles)
    }

    void setColor(TextColor color) {
        this.style = this.style.withColor(color)
    }

    void setColor(int color) {
        this.style = this.style.withColor(color)
    }

    void setBold(boolean is) {
        this.style = this.style.withBold(is)
    }

    void setItalic(boolean is) {
        this.style = this.style.withItalic(is)
    }

    void setUnderlined(boolean is) {
        this.style = this.style.withUnderlined(is)
    }

    void setStrikethrough(boolean is) {
        this.style = this.style.withStrikethrough(is)
    }

    void setObfuscated(boolean is) {
        this.style = this.style.withObfuscated(is)
    }

    void setFont(ResourceLocation rl) {
        this.style = this.style.withFont(rl)
    }

    void setClickEvent(ClickEvent event) {
        this.style = this.style.withClickEvent(event)
    }

    void setHoverEvent(HoverEvent event) {
        this.style = this.style.withHoverEvent(event)
    }
}
