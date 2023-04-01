/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.core.impl.mappings

import groovy.transform.CompileStatic
import groovy.transform.stc.POJO
import org.apache.groovy.util.BeanUtils
import org.codehaus.groovy.reflection.CachedField
import org.codehaus.groovy.reflection.GeneratedMetaMethod
import org.codehaus.groovy.runtime.MetaClassHelper
import org.codehaus.groovy.runtime.metaclass.MultipleSetterProperty

import java.lang.reflect.Field
import java.lang.reflect.Method

@CompileStatic
@POJO
class MappingMetaClass extends DelegatingMetaClass {

    private final Map<String, String> fieldMap
    private final Map<String, List<String>> methodMap

    private final Map<String, MetaProperty> metaProperties = new LinkedHashMap<>()

    MappingMetaClass(MetaClass delegate, LoadedMappings mappings) {
        super(delegate)
        Map<String, String> fields = new HashMap<>()
        Map<String, List<String>> methods = new HashMap<>()

        this.fieldMap = getFields(mappings, theClass)
        this.methodMap = getMethods(mappings, theClass)
    }

    protected static Map<String, String> getFields(LoadedMappings mappings, Class clazz) {
        if (clazz==null)
            return Map.of()
        var fields = mappings.fields.getOrDefault(clazz.name, new HashMap<>())
        fields += getFields(mappings, clazz.superclass)
        for (Class aClass : clazz.interfaces) {
            fields += getFields(mappings, aClass)
        }
        return fields
    }

    protected static Map<String, List<String>> getMethods(LoadedMappings mappings, Class clazz) {
        if (clazz==null)
            return Map.of()
        var methods = mappings.methods.getOrDefault(clazz.name, new HashMap<>())
        methods += getMethods(mappings, clazz.superclass)
        for (Class aClass : clazz.interfaces) {
            methods += getMethods(mappings, aClass)
        }
        return methods
    }

    @Override
    void initialize() {
        super.initialize()
        setupProperties()
    }

    @Override
    Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        try {
            return super.invokeStaticMethod(object, methodName, arguments)
        } catch (MissingMethodException e) {
            if (this.methodMap!==null) {
                // Check whether the method is in the mappables
                // If it is, map it and invoke that method
                List<String> mapped = this.methodMap.get(methodName)
                if (mapped!==null) for (String possible : mapped) {
                    try {
                        return super.invokeStaticMethod(object, possible, arguments)
                    } catch (MissingMethodException ignored) {}
                }
            }
            throw e
        }
    }

    @Override
    Object invokeMethod(Object object, String methodName, Object arguments) {
        try {
            return super.invokeMethod(object, methodName, arguments)
        } catch (MissingMethodException e) {
            if (this.methodMap!==null) {
                // Check whether the method is in the mappables
                // If it is, map it and invoke that method
                List<String> mapped = this.methodMap[methodName]
                if (mapped!==null) for (String possible : mapped) {
                    try {
                        return super.invokeMethod(object, possible, arguments)
                    } catch (MissingMethodException ignored) {}
                }
            }
            throw e
        }
    }

    @Override
    Object invokeMethod(Object object, String methodName, Object[] arguments) {
        try {
            return super.invokeMethod(object, methodName, arguments)
        } catch (MissingMethodException e) {
            if (this.methodMap!==null) {
                // Check whether the method is in the mappables
                // If it is, map it and invoke that method
                List<String> mapped = this.methodMap[methodName]
                if (mapped!==null) for (String possible : mapped) {
                    try {
                        return super.invokeMethod(object, possible, arguments)
                    } catch (MissingMethodException ignored) {}
                }
            }
            throw e
        }
    }

    @Override
    Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args)
        } catch (MissingMethodException e) {
            if (this.methodMap!==null) {
                // Check whether the method is in the mappables
                // If it is, map it and invoke that method
                List<String> mapped = this.methodMap[name]
                if (mapped!==null) for (String possible : mapped) {
                    try {
                        return super.invokeMethod(possible, args)
                    } catch (MissingMethodException ignored) {}
                }
            }
            throw e
        }
    }

    @Override
    Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        try {
            return super.invokeMethod(sender, receiver, methodName, arguments, isCallToSuper, fromInsideClass)
        } catch (MissingMethodException e) {
            if (this.methodMap!==null) {
                // Check whether the method is in the mappables
                // If it is, map it and invoke that method
                List<String> mapped = this.methodMap[methodName]
                if (mapped!==null) for (String possible : mapped) {
                    try {
                        return super.invokeMethod(sender, receiver, methodName, arguments, isCallToSuper, fromInsideClass)
                    } catch (MissingMethodException ignored) {}
                }
            }
            throw e
        }
    }

    @Override
    void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        try {
            super.setProperty(sender, receiver, messageName, messageValue, useSuper, fromInsideClass)
        } catch (MissingPropertyException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[messageName]
                if (mapped!==null) super.setProperty(sender, receiver, mapped, messageValue, useSuper, fromInsideClass)
                return
            }
            throw e
        }
    }

    @Override
    void setProperty(Object object, String property, Object newValue) {
        try {
            super.setProperty(object, property, newValue)
        } catch (MissingPropertyException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[property]
                if (mapped!==null) super.setProperty(object, mapped, newValue)
                return
            }
            throw e
        }
    }

    @Override
    void setProperty(String propertyName, Object newValue) {
        try {
            super.setProperty(propertyName, newValue)
        } catch (MissingPropertyException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[propertyName]
                if (mapped!==null) super.setProperty(mapped, newValue)
                return
            }
            throw e
        }
    }

    @Override
    Object getProperty(Class sender, Object receiver, String messageName, boolean useSuper, boolean fromInsideClass) {
        try {
            return super.getProperty(sender, receiver, messageName, useSuper, fromInsideClass)
        } catch (MissingPropertyException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[messageName]
                if (mapped!==null) return super.getProperty(sender,receiver,mapped,useSuper,fromInsideClass)
            }
            throw e
        }
    }

    @Override
    Object getProperty(String propertyName) {
        try {
            return super.getProperty(propertyName)
        } catch (MissingPropertyException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[propertyName]
                if (mapped!==null) return super.getProperty(mapped)
            }
            throw e
        }
    }

    @Override
    Object getProperty(Object object, String property) {
        try {
            return super.getProperty(object, property)
        } catch (MissingPropertyException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[property]
                if (mapped!==null) return super.getProperty(object, mapped)
            }
            throw e
        }
    }

    @Override
    Object getAttribute(Object object, String attribute) {
        try {
            return super.getAttribute(object, attribute)
        } catch (MissingPropertyException | MissingFieldException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[attribute]
                if (mapped!==null) return super.getAttribute(object, mapped)
            }
            throw e
        }
    }

    @Override
    Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        try {
            return super.getAttribute(sender, receiver, messageName, useSuper)
        } catch (MissingPropertyException | MissingFieldException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[messageName]
                if (mapped!==null) return super.getAttribute(sender, receiver, mapped, useSuper)
            }
            throw e
        }
    }

    @Override
    void setAttribute(Object object, String attribute, Object newValue) {
        try {
            super.setAttribute(object, attribute, newValue)
        } catch (MissingPropertyException | MissingFieldException e) {
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[attribute]
                if (mapped!==null) super.setAttribute(object, mapped, newValue)
                return
            }
            throw e
        }
    }

    @Override
    void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        try {
            super.setAttribute(sender, receiver, messageName, messageValue, useSuper, fromInsideClass)
        } catch (MissingPropertyException | MissingFieldException e) {
            
            if (this.fieldMap!==null) {
                // Check whether the field is in the mappables
                // If it is, map it and invoke that method
                String mapped = this.fieldMap[messageName]
                if (mapped!==null) super.setAttribute(sender, receiver, mapped, messageValue, useSuper, fromInsideClass)
                return
            }
            throw e
        }
    }

    // Now stuff that isn't just direct get/set/invoke (metaprogramming, yay!)
    @Override
    MetaProperty getMetaProperty(String name) {
        MetaProperty old = super.getMetaProperty(name)
        if (old !== null) return old

        return this.metaProperties.get(name)
    }

    private void setupProperties() {
        Set<String> known = super.getProperties().each {it.name}.<String>toSet()
        this.metaProperties.clear()
        fieldMap.forEach (fieldName, srgFieldName) -> {
            Field field;
            try {
                field = this.theClass.getField(srgFieldName)
            } catch (NoSuchFieldException ignored) {
                field = null
            }
            if (field !== null && !known.contains(fieldName)) {
                Class fieldType = field.type
                String getterName = MetaProperty.getGetterName(fieldName, fieldType)
                String setterName = MetaProperty.getSetterName(fieldName)
                MetaMethod getter = getMetaMethod(getterName)
                List<String> setters = methodMap.get(setterName)?:[]
                if (setters.size() <= 1) {
                    MetaMethod setter = setters.size()==0?null:getMetaMethod(setterName, fieldType)
                    MetaBeanProperty property = new MetaBeanProperty(fieldName, fieldType, getter, setter)
                    property.setField(new CachedField(field))
                    metaProperties[fieldName] = property
                } else {
                    MultipleSetterProperty property = new MultipleSetterProperty(fieldName)
                    property.setField(new CachedField(field))
                    property.setGetter(getter)
                    metaProperties[fieldName] = property
                }
            }
            return
        }
        methodMap.forEach (method, srgMethods) -> {
            if (method.startsWith("is") || method.startsWith("get")) {
                String fieldName = BeanUtils.decapitalize(method.replaceFirst(/is|get/,''))
                if (fieldName.isEmpty()) return
                if (!this.metaProperties.containsKey(fieldName) && !known.contains(fieldName)) {
                    //MetaMethod getter = getMetaMethod(method)
                    Method getter = null
                    srgMethods.each {
                        try {
                            getter = theClass.getMethod(it)
                        } catch (NoSuchMethodException ignored) {}
                    }
                    if (getter !== null && (method.startsWith("get") ^ getter.getReturnType()===Boolean.TYPE) && getter.parameterCount === 0) {
                        String setterName = MetaProperty.getSetterName(fieldName)
                        List<String> setters = methodMap.get(setterName)?:[]
                        MetaMethod metaGetter = getMetaMethod(getter.name)
                        if (setters.size() <= 1) {
                            MetaMethod metaSetter = null
                            if (setters.size() == 1) {
                                try {
                                    Method setter = theClass.getMethod(setters.get(0))
                                    if (setter.parameterTypes.size() == 1) {
                                        metaSetter = getMetaMethod(setters.get(0), setter.parameterTypes[0])
                                    }
                                } catch (NoSuchMethodException ignored) {}
                            }
                            MetaBeanProperty property = new MetaBeanProperty(fieldName, getter.getReturnType(), metaGetter, metaSetter)
                            metaProperties[fieldName] = property
                        } else {
                            MultipleSetterProperty property = new MultipleSetterProperty(fieldName)
                            property.setGetter(metaGetter)
                            metaProperties[fieldName] = property
                        }
                    }
                }
            }
        }
    }

    @Override
    List<MetaProperty> getProperties() {
        return super.getProperties() + this.metaProperties.values()
    }

    @Override
    MetaMethod getMetaMethod(String name, Object[] args) {
        MetaMethod old = super.getMetaMethod(name, args)
        if (old !== null) return old

        List<String> methods = this.methodMap[name]
        if (methods===null) methods = []

        return methods.stream().map(it->super.getMetaMethod(it, args))
                .filter(it->it!==null)
                .filter(it->{
                    try {
                        it.checkParameters(args.collect {it.class} as Class[])
                        return true
                    } catch (IllegalArgumentException ignored) {
                        return false
                    }
                }).findFirst().orElse(null)
    }

    @Override
    List<MetaMethod> getMethods() {
        return expandMethods(super.getMethods())
    }

    private List<MetaMethod> expandMethods(List<MetaMethod> methods) {
        List<MetaMethod> namedMethods = []
        methods.each {
            String srg = it.name
            String official = this.methodMap.find {
                it.value.contains(srg)
            }
            if (official !== null) {
                GeneratedMetaMethod newMethod = new GeneratedMetaMethod(official, it.declaringClass, it.returnType, it.parameterTypes*.theClass as Class[]) {
                    @Override
                    Object invoke(Object object, Object[] arguments) {
                        return it.invoke(object, arguments)
                    }
                }
                namedMethods += newMethod
            }
        }

        return methods + namedMethods
    }

    @Override
    MetaMethod getStaticMetaMethod(String name, Object[] args) {
        MetaMethod old = super.getStaticMetaMethod(name, args)
        if (old !== null) return old

        List<String> methods = this.methodMap[name]
        if (methods===null) methods = []

        return methods.stream().map(it->super.getStaticMetaMethod(it, args))
                .filter(it->it!==null)
                .filter(it->{
                    try {
                        it.checkParameters(args.collect {it.class} as Class[])
                        return true
                    } catch (IllegalArgumentException ignored) {
                        return false
                    }
                }).findFirst().orElse(null)
    }

    @Override
    MetaMethod getStaticMetaMethod(String name, Class[] argTypes) {
        MetaMethod old = super.getStaticMetaMethod(name, argTypes)
        if (old !== null) return old

        List<String> methods = this.methodMap[name]
        if (methods===null) methods = []

        return methods.stream().map(it->super.getStaticMetaMethod(it, argTypes))
                .filter(it->it!==null)
                .filter(it->{
                    try {
                        it.checkParameters(argTypes)
                        return true
                    } catch (IllegalArgumentException ignored) {
                        return false
                    }
                }).findFirst().orElse(null)
    }

    @Override
    List<MetaMethod> getMetaMethods() {
        return expandMethods(super.getMetaMethods())
    }

    @Override
    MetaProperty hasProperty(Object obj, String name) {
        MetaProperty old = super.hasProperty(obj, name)
        if (old !== null) return old
        return getMetaProperty(name)
    }

    @Override
    List<MetaMethod> respondsTo(Object obj, String name) {
        List<MetaMethod> old = super.respondsTo(obj, name)
        if (!old.isEmpty()) return old
        return getMetaMethods().findAll {it.name == name}
    }

    @Override
    List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes) {
        List<MetaMethod> old = super.respondsTo(obj, name, argTypes)
        if (!old.isEmpty()) return old

        Class[] classes = MetaClassHelper.castArgumentsToClassArray(argTypes);
        MetaMethod m = getMetaMethod(name, classes)
        if (m !== null) {
            return Collections.singletonList(m)
        }
        return Collections.emptyList()
    }
}
