package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec

import groovy.transform.CompileStatic

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a class as having a codec that can be used with members of that class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CompileStatic
@interface ExposesCodec {
    /**
     * The name of the property at which the codec is stored.
     */
    String value()
}