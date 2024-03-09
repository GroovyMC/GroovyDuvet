package org.groovymc.groovyduvet.wrapper.extension.client

import dev.lukebemish.autoextension.AutoExtension
import groovy.transform.CompileStatic
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import org.groovymc.cgl.api.extension.EnvironmentExtension

@CompileStatic
@EnvironmentExtension(EnvironmentExtension.Side.CLIENT)
@AutoExtension
class RenderExtension {
    static RenderType leftShift(RenderType layer, Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, layer)
        return layer
    }

    static RenderType leftShift(RenderType layer, Fluid fluid) {
        BlockRenderLayerMap.INSTANCE.putFluid(fluid, layer)
        return layer
    }
}
