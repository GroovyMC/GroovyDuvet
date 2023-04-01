/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.core.impl

import groovy.transform.CompileStatic
import groovy.transform.stc.POJO
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.runtime.m12n.ExtensionModule
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint

import java.nio.file.Files
import java.nio.file.Path

@CompileStatic
@POJO
class DevExtensionLoader implements PreLaunchEntrypoint {

    @Override
    void onPreLaunch(ModContainer mod) {
        if (QuiltLoader.isDevelopmentEnvironment()) {
            if (GroovySystem.metaClassRegistry instanceof MetaClassRegistryImpl) {
                MetaClassRegistryImpl registry = GroovySystem.metaClassRegistry as MetaClassRegistryImpl
                Map<CachedClass, List<MetaMethod>> map = [:]
                ExtensionModuleScanner scanner = new ExtensionModuleScanner((ExtensionModule module) -> {
                    List<MetaMethod> metaMethods = module.getMetaMethods()
                    if (!registry.moduleRegistry.hasModule(module.getName())) {
                        registry.moduleRegistry.addModule(module)
                        for (MetaMethod metaMethod : metaMethods) {
                            if (metaMethod.isStatic()) {
                                registry.staticMethods.add(metaMethod)
                            } else {
                                registry.instanceMethods.add(metaMethod)
                            }
                        }
                    }
                    for (MetaMethod metaMethod : metaMethods) {
                        CachedClass cachedClass = metaMethod.getDeclaringClass()
                        List<MetaMethod> methods = map.computeIfAbsent(cachedClass, k -> new ArrayList<MetaMethod>(4))
                        methods.add(metaMethod)
                    }
                }, DevExtensionLoader.classLoader)
                QuiltLoader.allMods.each {
                    Path path = it.getPath(ExtensionModuleScanner.MODULE_META_INF_FILE)
                    if (Files.exists(path)) {
                        Properties properties = new Properties()
                        properties.load(path.newReader())
                        if (properties.extensionClasses != null)
                            properties.extensionClasses = (properties.extensionClasses as String).split(',').findAll { String s ->
                                s = s.trim()
                                try {
                                    Class.forName(s, true, DevExtensionLoader.classLoader)
                                    return true
                                } catch (Exception ignored) {
                                    return false
                                }
                            }.join(',')
                        if (properties.staticExtensionClasses != null)
                            properties.staticExtensionClasses = (properties.staticExtensionClasses as String).split(',').findAll { String s ->
                                s = s.trim()
                                try {
                                    Class.forName(s, true, DevExtensionLoader.classLoader)
                                    return true
                                } catch (Exception ignored) {
                                    return false
                                }
                            }.join(',')
                        scanner.scanExtensionModuleFromProperties(properties)
                        registry.registerExtensionModuleFromProperties(properties, DevExtensionLoader.classLoader, map)
                    }
                }
                map.each { key, value ->
                    try {
                        key.addNewMopMethods(value)
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
