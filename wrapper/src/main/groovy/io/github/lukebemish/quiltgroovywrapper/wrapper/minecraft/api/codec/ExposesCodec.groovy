package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec

import groovy.transform.CompileStatic

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CompileStatic
@interface ExposesCodec {
    String value()
}