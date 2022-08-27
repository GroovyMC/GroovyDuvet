package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.Memoized

import java.nio.ByteBuffer
import java.util.stream.IntStream
import java.util.stream.LongStream

@CompileStatic
class CodecRetriever {
    @Memoized
    static <A> Codec<A> getAt(Class<A> clazz) {
        List<CodecSerializable> serializers = clazz.declaredAnnotations.findAll {it.annotationType() == CodecSerializable}.collect {(CodecSerializable)it }
        if (serializers.size() >= 1)
            return clazz.metaClass.getProperty(clazz, serializers[0].property()) as Codec<A>
        if (clazz == Integer)
            return Codec.INT as Codec<A>
        if (clazz == Boolean)
            return Codec.BOOL as Codec<A>
        if (clazz == Short)
            return Codec.SHORT as Codec<A>
        if (clazz == Byte)
            return Codec.BYTE as Codec<A>
        if (clazz == Float)
            return Codec.FLOAT as Codec<A>
        if (clazz == Double)
            return Codec.DOUBLE as Codec<A>
        if (clazz == Long)
            return Codec.LONG as Codec<A>
        if (clazz == String)
            return Codec.STRING as Codec<A>
        if (clazz == ByteBuffer)
            return Codec.BYTE_BUFFER as Codec<A>
        if (clazz == IntStream)
            return Codec.INT_STREAM as Codec<A>
        if (clazz == LongStream)
            return Codec.LONG_STREAM as Codec<A>

        String propertyName = (clazz.declaredAnnotations.find {it instanceof ExposesCodec} as ExposesCodec)?.value()
        if (propertyName !== null && propertyName != '' && clazz.metaClass.hasProperty(clazz, propertyName)) {
            Object value = clazz.metaClass.getProperty(clazz, propertyName)
            if (value instanceof Codec) {
                return value
            }
        }

        throw new IllegalArgumentException("Class ${clazz.name} has no codec to retrieve!")
    }

    static <A> Codec<A> getAt(A obj) {
        if (obj instanceof Class<A>)
            return getAt((Class<A>)obj)
        return getAt((Class<A>)obj.class)
    }
}
