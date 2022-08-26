package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic

@CompileStatic
class CodecRetriever {
    static <A> Codec<A> getAt(Class<? extends A> clazz) {
        List<CodecSerializable> serializers = clazz.declaredAnnotations.findAll {it.annotationType() == CodecSerializable}.collect {(CodecSerializable)it }
        if (serializers.size() >= 1)
            return clazz.metaClass.getProperty(clazz, serializers[0].property()) as Codec<A>

        throw new IllegalArgumentException("Class $clazz has no codec to retrieve!")
    }
}
