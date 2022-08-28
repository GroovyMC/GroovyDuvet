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

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import groovy.transform.CompileStatic

import java.util.stream.Stream

/**
 * A DynamicOps for converting to/from the formats used by groovy's JSON and TOML libraries - {@link Map}, {@link List},
 * {@link Number}, {@link Boolean}, {@link String}, {@link Date}, and null.
 * @see {@link groovy.json.JsonOutput}
 * @see {@link groovy.json.JsonSlurper}
 * @see {@link groovy.toml.TomlBuilder}
 * @see {@link groovy.toml.TomlSlurper}
 */
@Singleton
@CompileStatic
class ObjectOps implements DynamicOps<Object> {

    @Override
    Object empty() {
        return null
    }

    @Override
    <U> U convertTo(DynamicOps<U> outOps, Object input) {
        if (input instanceof Map)
            return convertMap(outOps, input)
        if (input instanceof List)
            return convertList(outOps, input)
        if (input == null)
            return outOps.empty()
        if (input instanceof Boolean)
            return outOps.createBoolean(input)
        if (input instanceof String)
            return outOps.createString(input)
        if (input instanceof Number)
            return outOps.createNumeric(input)
        if (input instanceof Date)
            return outOps.createString(input.format("yyyy-MM-dd'T'HH:mm:ssZ"))
        throw new UnsupportedOperationException("ObjectOps was unable to convert a value: " + input)
    }

    @Override
    DataResult<Number> getNumberValue(Object i) {
        return i instanceof Number
                ? DataResult.success(i)
                : DataResult.error("Not a number: " + i) as DataResult<Number>
    }

    @Override
    Object createNumeric(Number i) {
        return i
    }

    @Override
    DataResult<String> getStringValue(Object input) {
        if (input instanceof Date)
            return DataResult.success(input.format("yyyy-MM-dd'T'HH:mm:ssZ"))
        return (input instanceof Map || input instanceof List) ?
                DataResult.error("Not a string: " + input) as DataResult<String> :
                DataResult.success(String.valueOf(input))
    }

    @Override
    Object createString(String value) {
        return value
    }

    @Override
    DataResult<Object> mergeToList(Object list, Object value) {
        if (!(list instanceof List) && list != this.empty())
            return DataResult.error("mergeToList called with not a list: " + list, list)
        final List result = []
        if (list != this.empty()) {
            List listAsCollection = list as List
            result.addAll(listAsCollection)
        }
        result.add(value)
        return DataResult.success(result) as DataResult<Object>
    }

    @Override
    DataResult<Object> mergeToMap(Object map, Object key, Object value) {
        if (!(map instanceof Map) && map != this.empty()) {
            return DataResult.error("mergeToMap called with not a map: " + map, map)
        }
        DataResult<String> stringResult = this.getStringValue(key)
        Optional<DataResult.PartialResult<String>> badResult = stringResult.error()
        if (badResult.isPresent())
            return DataResult.error("key is not a string: " + key, map)
        return stringResult.flatMap{
            final Map output = [:]
            if (map != this.empty())
            {
                Map oldConfig = map as Map
                output.putAll(oldConfig)
            }
            output.put(it, value)
            return DataResult.success(output)
        } as DataResult<Object>
    }

    @Override
    DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
        if (!(input instanceof Map))
            return DataResult.error("Not a map: " + input)
        final Map config = input as Map
        return DataResult.success(config.entrySet().stream().map {
            return Pair.of(it.key, it.value)
        })
    }

    @Override
    Object createMap(Stream<Pair<Object, Object>> map) {
        final Map result = [:]
        map.iterator().each {
            result.put(this.getStringValue(it.getFirst()).getOrThrow(false, {}), it.getSecond())
        }
        return result
    }

    @Override
    DataResult<Stream<Object>> getStream(Object input) {
        if (input instanceof List)
        {
            @SuppressWarnings("unchecked")
            List list = input as List
            return DataResult.success(list.stream())
        }
        return DataResult.error("Not a list: " + input)
    }

    @Override
    Object createList(Stream<Object> input) {
        return input.toList()
    }

    @Override
    Object remove(Object input, String key) {
        if (input instanceof Map) {
            final Map result = [:]
            input.entrySet().stream()
                    .filter {key == it.key}
                    .iterator()
                    .each {result.put(it.key, it.value)}
            return result
        }
        return input
    }
}
