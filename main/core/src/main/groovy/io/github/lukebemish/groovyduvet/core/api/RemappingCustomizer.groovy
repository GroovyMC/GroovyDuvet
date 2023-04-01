/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.core.api

import groovy.transform.CompileStatic
import io.github.lukebemish.groovyduvet.core.impl.compile.ClassMappings
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer

@SuppressWarnings('unused')
@CompileStatic
class RemappingCustomizer extends CompilationCustomizer {
    RemappingCustomizer() {
        super(CompilePhase.CONVERSION)
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        ModuleNode ast = source.getAST()

        ast.imports.replaceAll {
            if (it.className !== null) {
                String newType = ClassMappings.mojToRuntime[it.className]
                if (newType !== null) {
                    var type = ClassHelper.make(newType)
                    if (!it.star && !it.static) {
                        return new ImportNode(type, newType.substring(newType.lastIndexOf('.') + 1))
                    } else {
                        it.type = type
                    }
                }
            }
            return it
        }

        List<ImportNode> packageImports = []

        ast.imports.removeIf {
            if (it.packageName !== null && ClassMappings.mojToRuntimePackages.containsKey(it.packageName)) {
                packageImports.add(it)
                return true
            }
        }

        packageImports.each {
            ClassMappings.mojToRuntimePackages[it.packageName].each { moj, runtime ->
                ast.imports.add(new ImportNode(ClassHelper.make(runtime), moj))
            }
        }
    }
}
