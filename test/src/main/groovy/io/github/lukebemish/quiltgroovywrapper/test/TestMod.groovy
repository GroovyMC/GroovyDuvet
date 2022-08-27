package io.github.lukebemish.quiltgroovywrapper.test

import com.google.gson.GsonBuilder
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import groovy.transform.Immutable
import groovy.transform.KnownImmutable
import groovy.transform.TupleConstructor
import io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec.CodecRetriever
import io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec.ExposeCodec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks

@Immutable
@CodecSerializable
class Test {
    int i
    String value
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

@Immutable(knownImmutableClasses = [ResourceLocation])
@CodecSerializable
class Test2 {
    Test test
    ResourceLocation rl
    List<Test3> test3
}

final GSON = new GsonBuilder().setPrettyPrinting().create()

final json = CodecRetriever[Test2].encodeStart(JsonOps.INSTANCE, new Test2(
        new Test(12,"stuff"),
        Registry.BLOCK.getKey(Blocks.DIRT),
        [new Test3(new char[] {'t','e','s','t'})]
)).getOrThrow(false, {})

println GSON.toJson(json)

println CodecRetriever[Test3]