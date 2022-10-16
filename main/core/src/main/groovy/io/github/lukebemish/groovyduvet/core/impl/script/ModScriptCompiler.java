package io.github.lukebemish.groovyduvet.core.impl.script;

import groovy.lang.GroovyShell;
import io.github.groovymc.modsdotgroovy.dsl.ModsDotGroovy;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public final class ModScriptCompiler {
    public static CompilerConfiguration getModScriptCompilerConfig(String group, String modid) {
        return new CompilerConfiguration()
                .addCompilationCustomizers(new ImportCustomizer()
                        .addStarImports("io.github.groovymc.modsdotgroovy"));
    }
    public static CompilerConfiguration getMDGExtractionConfig(String group) {
        return new CompilerConfiguration()
                .addCompilationCustomizers(new ImportCustomizer()
                                .addStarImports("io.github.groovymc.modsdotgroovy"),
                        new ASTTransformationCustomizer(new OnlyKeepMDGBlockTransformation(group)));
    }

    @SuppressWarnings("rawtypes")
    public static Map loadMDGFromClass(String modid, InputStream input) {
        Object output = new GroovyShell(QuiltLauncherBase.getLauncher().getTargetClassLoader(), getMDGExtractionConfig(MDG_GENERATED_PACKAGE))
                .evaluate(new InputStreamReader(input));
        if (output instanceof ModsDotGroovy mdg) {
            //noinspection UnstableApiUsage
            return mdg.getData();
        }
        return Map.of();
    }

    public static final String MDG_GENERATED_PACKAGE = "io.github.lukebemish.groovyduvet.runtimegenerated";
    public static volatile int mdgGeneratedClassCount = 0;

    public static synchronized String getMDGGeneratedClassName() {
        return MDG_GENERATED_PACKAGE + ".MDGGenerated$" + mdgGeneratedClassCount++;
    }
}
