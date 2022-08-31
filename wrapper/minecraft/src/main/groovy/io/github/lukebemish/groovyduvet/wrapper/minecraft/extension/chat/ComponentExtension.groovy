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
