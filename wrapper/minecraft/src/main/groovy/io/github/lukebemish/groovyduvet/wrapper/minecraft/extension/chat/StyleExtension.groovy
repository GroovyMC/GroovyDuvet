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
