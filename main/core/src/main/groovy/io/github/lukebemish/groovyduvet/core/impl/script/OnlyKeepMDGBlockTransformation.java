package io.github.lukebemish.groovyduvet.core.impl.script;

import io.github.groovymc.modsdotgroovy.dsl.ModsDotGroovy;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;

@GroovyASTTransformation
public class OnlyKeepMDGBlockTransformation implements ASTTransformation {
    private static final ClassNode MDG_ENTRY = makeWithoutCaching(ModsDotGroovy.class);

    public final String group;

    public OnlyKeepMDGBlockTransformation(String group) {
        this.group = group;
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        var searcher = new MDGSearcher(source);
        ModuleNode root = source.getAST();
        root.setPackageName(group);
        root.getStatementBlock().visit(searcher);
        root.getStatementBlock().getStatements().removeIf(s->true);
        root.getStatementBlock().addStatement(new ExpressionStatement(searcher.mdgBlock));

    }

    private static class MDGSearcher extends ClassCodeVisitorSupport {
        SourceUnit sourceUnit;
        StaticMethodCallExpression mdgBlock;

        protected MDGSearcher(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
            if (mdgBlock != null) {
                if (call.getMethod().equals("make") && call.getOwnerType().equals(MDG_ENTRY)) {
                    mdgBlock = call;
                }
            }
            super.visitStaticMethodCallExpression(call);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }
    }
}
