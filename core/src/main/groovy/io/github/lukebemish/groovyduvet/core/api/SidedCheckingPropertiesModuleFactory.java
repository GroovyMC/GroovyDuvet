package io.github.lukebemish.groovyduvet.core.api;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.runtime.m12n.MetaInfExtensionModule;
import org.codehaus.groovy.runtime.m12n.PropertiesModuleFactory;
import org.objectweb.asm.*;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class SidedCheckingPropertiesModuleFactory extends PropertiesModuleFactory {
    static final String INSTANCE_CLASSES = "extensionClasses";
    static final String STATIC_CLASSES = "staticExtensionClasses";
    static final String ONLYIN = "Lnet/minecraftforge/api/distmarker/OnlyIn;";
    static final String ENVIRONMENT = "Lnet/fabricmc/api/Environment;";

    static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public ExtensionModule newModule(Properties properties, ClassLoader classLoader) {
        if (properties.getProperty(INSTANCE_CLASSES) != null)
            properties.setProperty(INSTANCE_CLASSES, String.join(",", Arrays.stream(properties.getProperty(INSTANCE_CLASSES).split(",")).map(s -> {
                s = s.trim();
                if (s.isEmpty() || checkSidedness(s, classLoader))
                    return s;
                return "";
            }).toList()));
        if (properties.getProperty(STATIC_CLASSES) != null)
            properties.setProperty(STATIC_CLASSES, String.join(",", Arrays.stream(properties.getProperty(STATIC_CLASSES).split(",")).map(s -> {
                s = s.trim();
                if (s.isEmpty() || checkSidedness(s, classLoader))
                    return s;
                return "";
            }).toList()));
        return MetaInfExtensionModule.newModule(properties, classLoader);
    }

    boolean checkSidedness(String className, ClassLoader classLoader) {
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
                                    switch (value.toUpperCase(Locale.ROOT)) {
                                        case "SERVER", "DEDICATED_SERVER" -> {
                                            isOnDist.set(MinecraftQuiltLoader.getEnvironmentType() == EnvType.SERVER);
                                            if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT)
                                                LOGGER.info("Skipping extension class {} as we are on the client", className);
                                        }
                                        case "CLIENT" -> {
                                            isOnDist.set(MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT);
                                            if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.SERVER)
                                                LOGGER.info("Skipping extension class {} as we are on the server", className);
                                        }
                                        default -> {}
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
