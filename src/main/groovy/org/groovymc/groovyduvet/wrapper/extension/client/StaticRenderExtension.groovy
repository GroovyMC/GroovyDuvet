package org.groovymc.groovyduvet.wrapper.extension.client

import dev.lukebemish.autoextension.AutoExtension
import groovy.transform.CompileStatic
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import org.groovymc.cgl.api.extension.EnvironmentExtension

@CompileStatic
@EnvironmentExtension(EnvironmentExtension.Side.CLIENT)
@AutoExtension(isStatic = true)
class StaticRenderExtension {
    static void putAt(ItemBlockRenderTypes type, Block block, RenderType layer) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, layer)
    }

    static void putAt(ItemBlockRenderTypes type, Fluid fluid, RenderType layer) {
        BlockRenderLayerMap.INSTANCE.putFluid(fluid, layer)
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
