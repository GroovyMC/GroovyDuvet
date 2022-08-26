package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec

import groovy.transform.CompileStatic
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass('io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.impl.codec.CodecSerializableTransformation')
@CompileStatic
@interface CodecSerializable {
    String property() default '$CODEC'
}