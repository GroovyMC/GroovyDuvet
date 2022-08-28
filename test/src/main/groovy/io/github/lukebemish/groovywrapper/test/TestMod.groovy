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

package io.github.lukebemish.groovywrapper.test


import com.mojang.serialization.Codec
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.KnownImmutable
import groovy.transform.TupleConstructor
import io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.CodecRetriever
import io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.ExposeCodec
import io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.ObjectOps
import io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.WithCodec
import io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.WithCodecPath
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.block.Blocks

@Immutable(knownImmutableClasses = [Optional])
@CodecSerializable
@CompileStatic
class Test {
    int i
    String value
    @WithCodec(value = { IntProvider.NON_NEGATIVE_CODEC }, path = WithCodecPath.LIST) List<IntProvider> ints
    @WithCodec(value = { IntProvider.NON_NEGATIVE_CODEC }, path = WithCodecPath.OPTIONAL) Optional<IntProvider> maybeInt
}

@KnownImmutable
@TupleConstructor
class Test3 {
    private static Codec<Test3> codecInternal = Codec.STRING.xmap({ new Test3(it.chars) }, { Test3 it -> String.valueOf(it.chars) })

    char[] chars

    @ExposeCodec
    static Codec<Test3> getCodec() {
        return codecInternal
    }
}

@Immutable(knownImmutableClasses = [ResourceLocation,IntProvider])
@CodecSerializable
class Test2 {
    Test test
    ResourceLocation rl
    List<Test3> test3
    @WithCodec({ IntProvider.POSITIVE_CODEC }) IntProvider intProvider
}



final json = CodecRetriever[Test2].encodeStart(ObjectOps.instance, new Test2(
        new Test(12,"stuff",[UniformInt.of(1,2)],Optional.of(UniformInt.of(4,5))),
        Registry.BLOCK.getKey(Blocks.DIRT),
        [new Test3(new char[] {'t','e','s','t'})],
        UniformInt.of(3,6)
)).getOrThrow(false, {})

println JsonOutput.prettyPrint(JsonOutput.toJson(json))

println CodecRetriever[Test3]

println CodecRetriever[Direction]