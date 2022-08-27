package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec

import groovy.transform.CompileStatic
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Exposes a single field or getter method as providing a codec within a class. The transformation gives the parent
 * class the {@link ExposesCodec} annotation, pointing at the property defined by the annotated method or field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD])
@GroovyASTTransformationClass('io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.impl.codec.ExposeCodecTransformation')
@CompileStatic
@interface ExposeCodec {
}
