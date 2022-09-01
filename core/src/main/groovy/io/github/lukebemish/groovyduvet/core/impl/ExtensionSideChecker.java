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

package io.github.lukebemish.groovyduvet.core.impl;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExtensionSideChecker {
    private ExtensionSideChecker() {}
    public static final String INSTANCE_CLASSES = "extensionClasses";
    public static final String STATIC_CLASSES = "staticExtensionClasses";
    static final String ONLYIN = "Lnet/minecraftforge/api/distmarker/OnlyIn;";
    static final String ENVIRONMENT = "Lnet/fabricmc/api/Environment;";

    static final Logger LOGGER = LogUtils.getLogger();

    public static boolean checkSidedness(String className, ClassLoader classLoader) {
        AtomicBoolean isOnDist = new AtomicBoolean(true);
        try (var stream = classLoader.getResourceAsStream(className.replace('.', '/') + ".class")) {
            if (stream == null) {
                return false;
            }
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    if (desc.equals(ONLYIN) || desc.equals(ENVIRONMENT)) {
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitEnum(String name, String descriptor, String value) {
                                if (name.equals("value")) {
                                    String s = value.toUpperCase(Locale.ROOT);
                                    if (s.equals("SERVER") | s.equals("DEDICATED_SERVER")) {
                                        isOnDist.set(MinecraftQuiltLoader.getEnvironmentType() == EnvType.SERVER);
                                        if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT)
                                            LOGGER.info("Skipping extension class {} as we are on the client", className);
                                    } else if (s.equals("CLIENT")) {
                                        isOnDist.set(MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT);
                                        if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.SERVER)
                                            LOGGER.info("Skipping extension class {} as we are on the server", className);
                                    }
                                }
                            }
                        };
                    }
                    return super.visitAnnotation(desc, visible);
                }
            }, ClassReader.SKIP_CODE);
        } catch (Exception e) {
            isOnDist.set(false);
        }

        return isOnDist.get();
    }
}
