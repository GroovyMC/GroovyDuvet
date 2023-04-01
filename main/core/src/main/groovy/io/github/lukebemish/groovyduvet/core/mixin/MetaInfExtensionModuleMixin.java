/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.core.mixin;

import io.github.lukebemish.groovyduvet.core.impl.ExtensionSideChecker;
import org.codehaus.groovy.runtime.m12n.MetaInfExtensionModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Properties;

@Mixin(value = MetaInfExtensionModule.class, remap = false)
public class MetaInfExtensionModuleMixin {
    @Inject(method = "newModule", at = @At("HEAD"))
    private static void groovyduvet$modifyProperties(Properties properties, ClassLoader classLoader, CallbackInfoReturnable<MetaInfExtensionModule> cir) {
        if (properties.getProperty(ExtensionSideChecker.INSTANCE_CLASSES) != null)
            properties.setProperty(ExtensionSideChecker.INSTANCE_CLASSES, String.join(",", Arrays.stream(properties.getProperty(ExtensionSideChecker.INSTANCE_CLASSES).split("[,; ]")).map(s -> {
                s = s.trim();
                if (s.isEmpty() || ExtensionSideChecker.checkSidedness(s, classLoader))
                    return s;
                return "";
            }).toList()));
        if (properties.getProperty(ExtensionSideChecker.STATIC_CLASSES) != null)
            properties.setProperty(ExtensionSideChecker.STATIC_CLASSES, String.join(",", Arrays.stream(properties.getProperty(ExtensionSideChecker.STATIC_CLASSES).split("[,; ]")).map(s -> {
                s = s.trim();
                if (s.isEmpty() || ExtensionSideChecker.checkSidedness(s, classLoader))
                    return s;
                return "";
            }).toList()));
    }
}
