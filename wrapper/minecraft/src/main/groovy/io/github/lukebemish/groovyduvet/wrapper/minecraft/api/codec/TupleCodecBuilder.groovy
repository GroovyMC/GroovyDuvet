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

package io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
/**
 * A tool for assembling codecs for structures with more than 16 fields. For structures with less, use
 * {@link com.mojang.serialization.codecs.RecordCodecBuilder} instead.
 */
@AutoFinal
@CompileStatic
class TupleCodecBuilder {
    private final GetterMapCodec[] codecs

    private TupleCodecBuilder(GetterMapCodec[] codecs) {
        this.codecs = codecs
    }

    /**
     * Begin a builder using a series of {@link MapCodec} linked to getters.
     * @param codecs The combined {@link MapCodec} and getters, in order, to use in the builder.
     * @return A blank builder with the provided codecs and getters attached
     */
    static TupleCodecBuilder of(GetterMapCodec[] codecs) {
        return new TupleCodecBuilder(codecs)
    }

    /**
     * Combine a {@link MapCodec} and a getter that provides the type encoded by the {@link MapCodec}.
     */
    static <A> GetterMapCodec forGetter(MapCodec<A> codec, Closure<A> getter) {
        return new GetterMapCodec(codec, getter)
    }

    /**
     * Provide a new builder that includes all the features of the existing codecs, in addition to those provided.
     * @param codecs The combines {@link MapCodec} and getters to combine with the existing ones.
     * @return A builder containing both the original codecs, with the new ones appended afterwards.
     */
    TupleCodecBuilder with(GetterMapCodec[] codecs) {
        return new TupleCodecBuilder(this.codecs + codecs)
    }

    /**
     * Apply an encoding function to the builder to produce a {@link Codec}
     * @param closure The encoding function to use. Should take arguments of the same types as the stored
     * {@link GetterMapCodec}, in the same order.
     * @return A {@link Codec} of the return type of the provided closure.
     */
    <O> Codec<O> apply(Closure<O> closure) {
        return new Codec<O>() {
            @Override
            <T> DataResult<Pair<O, T>> decode(DynamicOps<T> ops, T input) {
                ops.getMap(input).flatMap { map ->
                    List<DataResult> partials = []
                    TupleCodecBuilder.this.codecs.each {
                        partials << it.codec.decode(ops, map)
                    }
                    var values = partials.collect { it.resultOrPartial(s -> { }) }
                    boolean partial = values.any { it.empty }
                    try {
                        Object[] args = values.collect { it.orElse(null) }.toArray()
                        O result = (args.inject(closure) {cl, arg -> cl.curry(arg)} as Closure<O>).call()
                        return partial ?
                                DataResult.error("Missing ${values.findIndexValues { it.isEmpty() }.collect { TupleCodecBuilder.this.codecs[it as int].codec.decoder() }}",
                                        new Pair<>(result, input)) :
                                DataResult.success(new Pair<>(result, input))
                    } catch (Exception e) {
                        return DataResult.error(e.message ?: e.class.name) as DataResult<Pair<O, T>>
                    }
                }
            }

            @Override
            <T> DataResult<T> encode(O input, DynamicOps<T> ops, T prefix) {
                var builder = ops.mapBuilder()
                for (GetterMapCodec it : codecs) {
                    builder = it.codec.encode(it.getter.call(input), ops, builder)
                }
                return builder.build(prefix)
            }
        }
    }

    final static class GetterMapCodec<A> {
        final MapCodec<A> codec
        final Closure<A> getter
        private GetterMapCodec(MapCodec<A> codec, Closure<A> getter) {
            this.codec = codec
            this.getter = getter
        }
    }
}
