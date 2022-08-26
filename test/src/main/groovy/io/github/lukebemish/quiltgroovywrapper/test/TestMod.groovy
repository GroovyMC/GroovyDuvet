package io.github.lukebemish.quiltgroovywrapper.test

import net.minecraft.core.Registry
import net.minecraft.world.level.block.Blocks

return { mod ->
    println "Dirt block: ${Registry.BLOCK.getKey(Blocks.DIRT)}"
}