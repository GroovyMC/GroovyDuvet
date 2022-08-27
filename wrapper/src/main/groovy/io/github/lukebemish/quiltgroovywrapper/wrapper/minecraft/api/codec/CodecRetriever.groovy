package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.Memoized

import java.nio.ByteBuffer
import java.util.stream.IntStream
import java.util.stream.LongStream

/**
 * A tool for retrieving codecs to encode or decode objects. Can retrieve codecs from members of any type with the
 * {@link ExposesCodec} annotation, as well as any of the build in primitive codecs in {@link Codec}.
 */
@CompileStatic
class CodecRetriever {
    /**
     * Retrieve a codec matching a given class.
     * @param clazz The class to locate a codec for.
     * @return A codec matching that class.
     * @throws {@link IllegalArgumentException} if no codec can be retrieved.
     */
    @Memoized
    static <A> Codec<A> getAt(Class<A> clazz) {
        String propertyName = (clazz.declaredAnnotations.find {it instanceof ExposesCodec} as ExposesCodec)?.value()
        if (propertyName !== null && propertyName != '' && clazz.metaClass.hasProperty(clazz, propertyName)) {
            Object value = clazz.metaClass.getProperty(clazz, propertyName)
            if (value instanceof Codec) {
                return value
            }
        }
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

        throw new IllegalArgumentException("Class ${clazz.name} has no codec to retrieve!")
    }

    /**
     * Retrieve a codec matching a given object.
     * @param obj The object to locate a codec for.
     * @return A codec matching that object's class.
     * @throws {@link IllegalArgumentException} if no codec can be retrieved.
     */
    static <A> Codec<A> getAt(A obj) {
        return getAt((Class<A>)obj.class)
    }
}
