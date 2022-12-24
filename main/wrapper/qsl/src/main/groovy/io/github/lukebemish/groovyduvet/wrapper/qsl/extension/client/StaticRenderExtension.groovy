/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.wrapper.qsl.extension.client

import dev.lukebemish.autoextension.AutoExtension
import groovy.transform.CompileStatic
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import org.quiltmc.loader.api.minecraft.ClientOnly
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap

@CompileStatic
@ClientOnly
@AutoExtension(isStatic = true)
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
