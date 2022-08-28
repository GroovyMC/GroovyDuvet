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

package io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec

import groovy.transform.CompileStatic

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a class as having a codec that can be used with members of that class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CompileStatic
@interface ExposesCodec {
    /**
     * The name of the property at which the codec is stored. If the annotated class is {@code A}, the value should
     * point to a property of type {@code Codec<A>}.
     */
    String value()
}