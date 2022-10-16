/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.wrapper.mdg.transform

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.transform.stc.ClosureParams
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.jetbrains.annotations.ApiStatus

import java.lang.reflect.Modifier

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class CompileTimeMdgClassRelocator implements ASTTransformation {
    static final ClassNode CLOSURE_PARAMS = makeWithoutCaching(ClosureParams)
    static final ClassNode INTERNAL = makeWithoutCaching(ApiStatus.Internal)

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        ModuleNode root = source.AST
        root.classes.each { clazz ->
            clazz.name = "io.github.groovymc.modsdotgroovy.dsl.${clazz.name}"
        }
        ParameterVisitor visitor = new ParameterVisitor(source)
        source.AST.classes.each {visitor.visitClass(it)}
    }

    @TupleConstructor
    private static class ParameterVisitor extends ClassCodeVisitorSupport {
        SourceUnit sourceUnit

        @Override
        void visitField(FieldNode node) {
            if (node.owner.name == 'io.github.groovymc.modsdotgroovy.dsl.ModsDotGroovy' && node.name == 'data') {
                var method = new MethodNode(
                        'getData',
                        Modifier.PUBLIC,
                        node.type,
                        Parameter.EMPTY_ARRAY,
                        ClassNode.EMPTY_ARRAY,
                        new ReturnStatement(new FieldExpression(node))
                )
                method.addAnnotation(INTERNAL)
                node.owner.addMethod(method)
            }
            super.visitField(node)
        }

        @Override
        void visitMethod(MethodNode node) {
            node.parameters.each { parameter ->
                parameter.annotations.each { annotation ->
                    if (annotation.classNode == CLOSURE_PARAMS) {
                        annotation.members.each { member ->
                            if (member.key == 'options') {
                                List<String> options = []
                                if (member.value instanceof ConstantExpression) {
                                    ConstantExpression c = (ConstantExpression) member.value
                                    if (c.value instanceof String)
                                        options.add((String) c.value)
                                } else if (member.value instanceof ListExpression) {
                                    ListExpression l = (ListExpression) member.value
                                    l.expressions.each {
                                        if (member.value instanceof ConstantExpression) {
                                            ConstantExpression c = (ConstantExpression) member.value
                                            if (c.value instanceof String)
                                                options.add((String) c.value)
                                        }
                                    }
                                }
                                List<String> collectedOptions = options.collect {
                                    if (it.startsWith('modsdotgroovy') || !it.contains('.'))
                                        "io.github.groovymc.modsdotgroovy.dsl.${it}" as String
                                    else
                                        it as String
                                }
                                member.value = new ListExpression(collectedOptions.collect { (Expression) new ConstantExpression(it) })
                            }
                            return
                        }
                    }
                }
            }
            super.visitMethod(node)
        }
    }
}
