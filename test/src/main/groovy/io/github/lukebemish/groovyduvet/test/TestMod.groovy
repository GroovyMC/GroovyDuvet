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

package io.github.lukebemish.groovyduvet.test

import com.mojang.serialization.Codec
import groovy.json.JsonOutput
import groovy.transform.*
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.*
import net.minecraft.ChatFormatting
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.block.Blocks
import org.quiltmc.qsl.networking.api.EntityTrackingEvents

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

@CompileStatic
@TupleConstructor
@ToString
@CodecSerializable
class TestTupleCodecBuilder {
    final int int0
    final int int1
    final int int2
    final int int3
    final int int4
    final int int5
    final int int6
    final int int7
    final int int8
    final int int9
    final int int10
    final int int11
    final int int12
    final int int13
    final int int14
    final int int15
    final int int16
    final int int17
}

final map = CodecRetriever[TestTupleCodecBuilder].encodeStart(ObjectOps.instance,
        new TestTupleCodecBuilder(1,2,3,4,5,6,7,8,9,
                10,11,12,13,14,15,16,17,18)).getOrThrow(false, {})
println JsonOutput.prettyPrint(JsonOutput.toJson(map))
println CodecRetriever[TestTupleCodecBuilder].decode(ObjectOps.instance,map).getOrThrow(false, {})

println Registry.BLOCK[new ResourceLocation('stone')]

EntityTrackingEvents.START_TRACKING.register { entity, player ->
    player.sendSystemMessage Component.literal("Test") << Style.of {
        style ChatFormatting.DARK_BLUE
        strikethrough = true
    }
}
