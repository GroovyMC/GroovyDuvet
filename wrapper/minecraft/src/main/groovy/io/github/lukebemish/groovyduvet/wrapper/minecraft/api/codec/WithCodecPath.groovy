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

/**
 * These path elements can be used with {@link WithCodec} to target individual nested type parameters with a codec.
 */
enum WithCodecPath {
    /**
     * Targets the type held by a {@link List}.
     */
    LIST,
    /**
     * Targets the type held by an {@link java.util.Optional}.
     */
    OPTIONAL,
    /**
     * Targets the type of the keys of a {@link Map}.
     */
    MAP_KEY,
    /**
     * Targets the type of the values of a {@link Map}
     */
    MAP_VAL,
    /**
     * Targets the type of the first value of a {@link com.mojang.datafixers.util.Pair}.
     */
    PAIR_FIRST,
    /**
     * Targets the type of the second value of a {@link com.mojang.datafixers.util.Pair}.
     */
    PAIR_SECOND,
    /**
     * Targets the type of the left value of a {@link com.mojang.datafixers.util.Either}.
     */
    EITHER_LEFT,
    /**
     * Targets the type of the right value of a {@link com.mojang.datafixers.util.Either}.
     */
    EITHER_RIGHT
}