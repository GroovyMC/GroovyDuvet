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

package io.github.lukebemish.groovyduvet.wrapper.qsl.extension.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap

@Environment(EnvType.CLIENT)
class RenderExtension {
    static RenderType leftShift(RenderType layer, Block block) {
        BlockRenderLayerMap.put(layer, block)
        return layer
    }

    static RenderType leftShift(RenderType layer, Fluid fluid) {
        BlockRenderLayerMap.put(layer, fluid)
        return layer
    }
}
