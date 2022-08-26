package io.github.lukebemish.quiltgroovywrapper.test

import com.google.gson.GsonBuilder
import com.mojang.serialization.JsonOps
import groovy.transform.Immutable
import io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec.CodecRetriever
import io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec.CodecSerializable
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks

@Immutable
@CodecSerializable
class Test {
    int i
    String value
}

@Immutable(knownImmutableClasses = [ResourceLocation])
@CodecSerializable
class Test2 {
    Test test
    ResourceLocation rl
}

final GSON = new GsonBuilder().setPrettyPrinting().create()

final json = CodecRetriever[Test2].encodeStart(JsonOps.INSTANCE, new Test2(
        new Test(12,"stuff"),
        Registry.BLOCK.getKey(Blocks.DIRT)
)).getOrThrow(false, {})

println GSON.toJson(json)