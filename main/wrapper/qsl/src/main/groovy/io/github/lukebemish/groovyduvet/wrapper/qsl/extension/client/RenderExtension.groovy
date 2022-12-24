/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.wrapper.qsl.extension.client

import dev.lukebemish.autoextension.AutoExtension
import groovy.transform.CompileStatic
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import org.quiltmc.loader.api.minecraft.ClientOnly
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap

@CompileStatic
@ClientOnly
@AutoExtension
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
