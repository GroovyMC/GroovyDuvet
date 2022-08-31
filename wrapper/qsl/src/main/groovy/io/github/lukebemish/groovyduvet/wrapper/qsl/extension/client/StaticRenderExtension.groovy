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

import groovy.transform.CompileStatic
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap

@CompileStatic
@Environment(EnvType.CLIENT)
class StaticRenderExtension {
    static void putAt(ItemBlockRenderTypes type, Block block, RenderType layer) {
        BlockRenderLayerMap.put(layer, block)
    }

    static void putAt(ItemBlockRenderTypes type, Fluid fluid, RenderType layer) {
        BlockRenderLayerMap.put(layer, fluid)
    }

    static RenderType getAt(ItemBlockRenderTypes type, Block block) {
        ItemBlockRenderTypes.getChunkRenderType(block.defaultBlockState())
    }

    static RenderType getAt(ItemBlockRenderTypes type, Fluid fluid) {
        ItemBlockRenderTypes.getRenderLayer(fluid.defaultFluidState())
    }

    static RenderType getAt(ItemBlockRenderTypes type, BlockState state) {
        ItemBlockRenderTypes.getChunkRenderType(state)
    }

    static RenderType getAt(ItemBlockRenderTypes type, FluidState fluid) {
        ItemBlockRenderTypes.getRenderLayer(fluid)
    }
}
