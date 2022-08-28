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

package io.github.lukebemish.groovyduvet.core.impl

import groovy.transform.CompileStatic
import io.github.lukebemish.groovyduvet.core.impl.mappings.MetaclassMappingsProvider
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.quiltmc.loader.api.LanguageAdapter
import org.quiltmc.loader.api.LanguageAdapterException
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase

import java.lang.reflect.Method
import java.lang.reflect.Modifier

@CompileStatic
class GroovyAdapter implements LanguageAdapter {
    GroovyAdapter() {
        if (!QuiltLoader.developmentEnvironment) {
            MetaclassMappingsProvider.setup()
        }
    }

    @Override
    <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        String [] parts = value.split('::')

        if (parts.size() > 2) {
            throw new LanguageAdapterException("Invalid handle format: $value")
        }

        Class<?> c

        try {
            //TODO: see if there's any recommended alternative to QuiltLauncherBase here
            c = Class.forName(parts[0], true, QuiltLauncherBase.getLauncher().getTargetClassLoader())
        } catch (ClassNotFoundException e) {
            throw new LanguageAdapterException(e)
        }

        if (Script.isAssignableFrom(c)) {
            try {
                // Different logic for scripts
                if (parts.size() == 1) {
                    List<Method> methods = type.declaredMethods.findAll {(it.modifiers && Modifier.ABSTRACT !== 0) }
                    if (methods.size() == 1) {
                        Method method = methods.get(0)
                        List<String> argNames = method.parameters.collect {it.name}

                        return (T) DefaultTypeTransformation.castToType({ Object... args ->
                            Script script = (Script) c.getDeclaredConstructor().newInstance()
                            Map bindings = [:]
                            for (int i : 0..<argNames.size()) {
                                bindings[argNames[i]] = args[i]
                            }
                            script.binding = new Binding(bindings)
                            script.run()
                        }, type)
                    } else {
                        throw new LanguageAdapterException("Scripts evaluated without a local variable to capture may only be used for entrypoints with a single abstract method!")
                    }
                } else {
                    Script script = (Script) c.getDeclaredConstructor().newInstance()
                    script.run()
                    return (T) DefaultTypeTransformation.castToType(script.evaluate(parts[1]), type)
                }
            } catch (Exception e) {
                throw new LanguageAdapterException(e)
            }
        }

        if (parts.size() == 1) {
            try {
                Object o = c.getDeclaredConstructor().newInstance()
                return (T) DefaultTypeTransformation.castToType(o,type)
            } catch (Exception e) {
                throw new LanguageAdapterException(e)
            }
        }

        final List<MetaMethod> methods = c.metaClass.methods.findAll {it.name == parts[1]}

        MetaProperty property = c.metaClass.hasProperty(c, parts[1])

        if (property != null) {
            if (!methods.isEmpty()) {
                throw new LanguageAdapterException("Ambiguous ${value} - refers to both property and method!")
            } else {
                try {
                    return (T) DefaultTypeTransformation.castToType(property.getProperty(c), type)
                } catch (GroovyCastException e) {
                    throw new LanguageAdapterException(e)
                }
            }
        }

        if (methods.isEmpty()) {
            throw new LanguageAdapterException("Could not find ${value}!")
        }

        MethodClosure closure = new MethodClosure(c, parts[1])

        try {
            return (T) DefaultTypeTransformation.castToType(closure, type)
        } catch (GroovyCastException e) {
            throw new LanguageAdapterException(e)
        }
    }
}
