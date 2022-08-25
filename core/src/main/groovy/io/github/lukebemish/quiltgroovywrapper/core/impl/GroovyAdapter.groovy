package io.github.lukebemish.quiltgroovywrapper.core.impl

import groovy.transform.CompileStatic
import io.github.lukebemish.quiltgroovywrapper.core.impl.mappings.MetaclassMappingsProvider
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.quiltmc.loader.api.LanguageAdapter
import org.quiltmc.loader.api.LanguageAdapterException
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase

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
                Script script = (Script) c.getDeclaredConstructor().newInstance()
                if (parts.size() == 1) {
                    Object o = script.run()
                    if (o === null) {
                        throw new LanguageAdapterException("Scripts evaluated without a local variable to capture must return a value to be cast to the entrypoint class!")
                    }
                    return (T) DefaultTypeTransformation.castToType(o, type)
                } else {
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
