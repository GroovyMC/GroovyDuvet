/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.core.impl

import groovy.transform.CompileStatic
import groovy.transform.stc.POJO
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.quiltmc.loader.api.LanguageAdapterException
import org.quiltmc.loader.api.ModContainer

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@CompileStatic
@POJO
class GroovyAdapterImpl implements GroovyAdapter.DelegatedLanguageAdapter {

    <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        String [] parts = value.split('::')

        if (parts.size() > 2) {
            throw new LanguageAdapterException("Invalid handle format: $value")
        }

        Class<?> c

        try {
            c = Class.forName(parts[0], true, GroovyAdapter.KNOT_LOADER)
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
                        int numParams = method.parameters.size()

                        Constructor ctor = c.getConstructor()

                        return (T) DefaultTypeTransformation.castToType({ Object... args ->
                            Script script = (Script) ctor.newInstance()
                            Map bindings = [:]
                            for (int i : 0..<numParams) {
                                bindings["arg$i"] = args[i]
                            }
                            script.binding = new Binding(bindings)
                            script.run()
                        }, type)
                    } else {
                        throw new LanguageAdapterException("Scripts evaluated without a local variable to capture may only be used for entrypoints with a single abstract method!")
                    }
                } else {
                    Script script = (Script) c.getConstructor().newInstance()
                    script.binding = new Binding(['entrypointType':type])
                    script.run()
                    Object o
                    try {
                        o = script.getProperty(parts[1])
                    } catch (MissingPropertyException ignored) {
                        //noinspection GroovyUnusedCatchParameter
                        try {
                            o = script.binding.getVariable(parts[1])
                        } catch (MissingPropertyException ignored2) {
                            throw new LanguageAdapterException("Could not find local variable ${parts[1]} in script entrypoint ${parts[0]}.")
                        }
                    }
                    return (T) DefaultTypeTransformation.castToType(o, type)
                }
            } catch (Exception e) {
                throw new LanguageAdapterException(e)
            }
        }

        if (parts.size() == 1) {
            try {
                Object o = c.getConstructor().newInstance()
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

        final MethodClosure closure
        if (methods.every {!it.static}) {
            Object o = c.getConstructor().newInstance()
            closure = new MethodClosure(o, parts[1])
        } else
            closure = new MethodClosure(c, parts[1])

        try {
            return (T) DefaultTypeTransformation.castToType(closure, type)
        } catch (GroovyCastException e) {
            throw new LanguageAdapterException(e)
        }
    }
}
