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
            properties.setProperty(ExtensionSideChecker.INSTANCE_CLASSES, String.join(",", Arrays.stream(properties.getProperty(ExtensionSideChecker.INSTANCE_CLASSES).split(",")).map(s -> {
                s = s.trim();
                if (s.isEmpty() || ExtensionSideChecker.checkSidedness(s, classLoader))
                    return s;
                return "";
            }).toList()));
        if (properties.getProperty(ExtensionSideChecker.STATIC_CLASSES) != null)
            properties.setProperty(ExtensionSideChecker.STATIC_CLASSES, String.join(",", Arrays.stream(properties.getProperty(ExtensionSideChecker.STATIC_CLASSES).split(",")).map(s -> {
                s = s.trim();
                if (s.isEmpty() || ExtensionSideChecker.checkSidedness(s, classLoader))
                    return s;
                return "";
            }).toList()));
    }
}
