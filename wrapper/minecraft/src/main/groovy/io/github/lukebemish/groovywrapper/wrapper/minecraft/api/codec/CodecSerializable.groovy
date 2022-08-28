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
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * The @CodecSerializable annotation builds a codec based on the constructor and fields of a class. The transformation
 * bases this codec off the class's constructor which accepts the most parameters. Each of these parameters must have
 * the same name and type as a property defined on the object. This type can be the type parameter of any
 * {@link com.mojang.serialization.codecs.PrimitiveCodec} defined in {@link com.mojang.serialization.Codec}, any other
 * class with the {@link CodecSerializable} annotation, any class that exposes a codec with {@link ExposesCodec}, any
 * enum which extends {@link net.minecraft.util.StringRepresentable}, or any class {@code A} with a single public static
 * field of type {@code Codec<A>}, or any {@link List}, {@link java.util.Optional}, {@link Map},
 * {@link com.mojang.datafixers.util.Pair}, or {@link com.mojang.datafixers.util.Either} parameterized with a type or
 * types that satisfy the same requirements.
 *
 * The codec is constructed with {@link com.mojang.serialization.codecs.RecordCodecBuilder}, and so the constructor is
 * limited to 16 parameters. To use more than that, reorganize and nest your data structures.
 *
 * The {@link ExposesCodec} annotation will be automatically applied with the proper value if it is not already present
 * on the class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass('io.github.lukebemish.groovywrapper.wrapper.minecraft.impl.codec.CodecSerializableTransformation')
@CompileStatic
@interface CodecSerializable {
    /**
     * The property at which the assembled codec will be stored. Can be accessed normally, or through
     * {@link CodecRetriever}.
     */
    String property() default '$CODEC'
}