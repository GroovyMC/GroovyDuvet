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

package io.github.lukebemish.groovywrapper.wrapper.minecraft.impl.codec

import groovy.transform.CompileStatic
import org.apache.groovy.util.BeanUtils
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class ExposeCodecTransformation extends AbstractASTTransformation {

    static final ClassNode MY_TYPE = makeWithoutCaching(io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.ExposeCodec)
    static final ClassNode TARGET_TYPE = makeWithoutCaching(io.github.lukebemish.groovywrapper.wrapper.minecraft.api.codec.ExposesCodec)
    static final ClassNode CODEC_NODE = makeWithoutCaching('com.mojang.serialization.Codec')

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        AnnotationNode anno = (AnnotationNode) nodes[0]
        if (MY_TYPE != anno.getClassNode()) return

        if (parent instanceof FieldNode) {
            if (!parent.static) {
                throw new RuntimeException('ExposeCodec may only expose static properties!')
            }
            ClassNode clazz = parent.declaringClass
            ClassNode type = parent.type
            String name = parent.name
            doApply(clazz, type, name)
        } else if (parent instanceof MethodNode) {
            if (!parent.static) {
                throw new RuntimeException('ExposeCodec may only expose static properties!')
            }
            if (parent.parameters.size() != 0) {
                throw new RuntimeException("ExposeCodec can only be used to expose fields or getters from within a class. Currently applied to the method $parent.name, which takes ${parent.parameters.size()} arguments.")
            }
            ClassNode clazz = parent.declaringClass
            ClassNode type = parent.returnType
            if (! parent.name.startsWith('get')) {
                throw new RuntimeException("ExposeCodec can only be used to expose fields or getters from within a class. Currently applied to the method ${parent.name}.")
            }
            String name = BeanUtils.decapitalize(parent.name.substring(3))
            doApply(clazz, type, name)
        }
    }

    static void doApply(ClassNode parent, ClassNode type, String name) {
        if (!StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(type, CODEC_NODE)) {
            throw new RuntimeException("ExposeCodec used in ${parent.name} must annotate either a field of type Codec<${parent.nameWithoutPackage}> or a getter that return Codec<${parent.nameWithoutPackage}>. Current detected type is ${type.toString(false)}")
        }
        if (parent.annotations*.classNode.find {it == TARGET_TYPE}) {
            throw new RuntimeException("Cannot use ExposeCodec to expose a property when the parent class already has a codec exposed!")
        }
        parent.addAnnotation(new AnnotationNode(TARGET_TYPE).tap {
            addMember('value', new ConstantExpression(name))
        })
    }
}
