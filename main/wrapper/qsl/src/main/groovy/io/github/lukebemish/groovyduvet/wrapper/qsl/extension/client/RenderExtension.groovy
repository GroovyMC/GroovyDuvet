/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.wrapper.qsl.extension.client

import groovy.transform.CompileStatic
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap

@CompileStatic
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
