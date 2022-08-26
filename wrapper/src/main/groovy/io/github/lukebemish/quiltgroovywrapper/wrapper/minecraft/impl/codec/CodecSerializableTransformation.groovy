package io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.impl.codec

import groovy.transform.CompileStatic
import io.github.lukebemish.quiltgroovywrapper.wrapper.minecraft.api.codec.CodecSerializable
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.TransformWithPriority
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport

import java.lang.reflect.Modifier
import java.nio.ByteBuffer
import java.util.stream.IntStream
import java.util.stream.LongStream

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class CodecSerializableTransformation extends AbstractASTTransformation implements TransformWithPriority {

    static final ClassNode MY_TYPE = makeWithoutCaching(CodecSerializable)
    static final String CODEC = 'com.mojang.serialization.Codec'
    static final ClassNode CODEC_NODE = makeWithoutCaching(CODEC)
    static final String RECORD_CODEC_BUILDER = 'com.mojang.serialization.codecs.RecordCodecBuilder'
    static final String INSTANCE = 'com.mojang.serialization.codecs.RecordCodecBuilder$Instance'
    static final ClassNode INSTANCE_NODE = makeWithoutCaching(INSTANCE)
    static final ClassNode OPTIONAL = makeWithoutCaching(Optional)

    static final ClassNode BYTE_BUFFER = makeWithoutCaching(ByteBuffer)
    static final ClassNode INT_STREAM = makeWithoutCaching(IntStream)
    static final ClassNode LONG_STREAM = makeWithoutCaching(LongStream)
    static final ClassNode PAIR = makeWithoutCaching('com.mojang.datafixers.util.Pair')
    static final ClassNode EITHER = makeWithoutCaching('com.mojang.datafixers.util.Either')
    static final ClassNode STRING_REPRESENTABLE = makeWithoutCaching('net.minecraft.util.StringRepresentable')

    public static final String DEFAULT_CODEC_PROPERTY = '$CODEC'

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        AnnotationNode anno = (AnnotationNode) nodes[0]
        if (MY_TYPE != anno.getClassNode()) return

        if (parent instanceof ClassNode) {
            doAssembleCodec(parent, anno)
        }
    }

    static doAssembleCodec(ClassNode parent, AnnotationNode anno) {
        String fieldName = getMemberValue(anno, "property", DEFAULT_CODEC_PROPERTY)
        if (parent.getField(fieldName)?.static)
            throw new RuntimeException("Codec-serializable class ${parent.name} already defines a static field with the same name as the codec: ${fieldName}")

        ConstructorNode assembler
        if (parent.declaredConstructors.size()>=1)
            assembler = parent.declaredConstructors.sort(false) {
                -it.parameters.size()
            }.get(0)
        else
            throw new RuntimeException('Codec-serializable classes must have at least one constructor.')

        if (assembler.parameters.size() > 16) {
            throw new RuntimeException('RecordCodecBuilder only allows the assembly of codec with up to 16 fields; to use more, organize and nest your data structure.')
        }

        ClassNode resolvedCodec = makeWithoutCaching(CODEC)
        resolvedCodec.setGenericsTypes(new GenericsType(parent))

        Expression[] grouping = new Expression[assembler.parameters.size()]

        for (int i = 0; i < assembler.parameters.size(); i++) {
            FieldNode field = parent.getField(assembler.parameters[i].name)
            if (field == null || field.static)
                throw new RuntimeException('Codec-serializable classes must have a field matching each constructor parameter')
            grouping[i] = assembleExpression(parent, assembler.parameters[i], field)
        }

        Expression grouped = new MethodCallExpression(new VariableExpression('i',INSTANCE_NODE),'group',new ArgumentListExpression(grouping))
        Statement statement = new ReturnStatement(new MethodCallExpression(grouped, 'apply',
                new ArgumentListExpression(new VariableExpression('i',INSTANCE_NODE),
                        new MethodReferenceExpression(new ClassExpression(parent), new ConstantExpression('new')))))
        LambdaExpression function = new LambdaExpression(new Parameter[] {new Parameter(INSTANCE_NODE,'i')}, statement).tap {
            variableScope = new VariableScope()
        }
        Expression initialValue = new StaticMethodCallExpression(makeWithoutCaching(RECORD_CODEC_BUILDER),'create',new ArgumentListExpression(function))

        ClassNode wrappedNode = makeWithoutCaching(CODEC)
        wrappedNode.redirect = resolvedCodec
        parent.addField(fieldName, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, wrappedNode, initialValue)
    }

    static Object getMemberValue(AnnotationNode node, String name) {
        final Expression member = node.getMember(name)
        if (member instanceof ConstantExpression) return ((ConstantExpression) member).getValue()
        return null
    }

    static Object getMemberValue(AnnotationNode node, String name, Object defaultVal) {
        return getMemberValue(node, name)?:defaultVal
    }

    static Expression assembleExpression(ClassNode parent, Parameter parameter, FieldNode field) {
        if (unresolveOptional(field.type) != unresolveOptional(parameter.type) || (isOptional(field.type) && !isOptional(parameter.type)))
            throw new RuntimeException("Field and parameter types must match in codec-serializable classes. Got: ${parameter.type} and ${field.type}")

        Expression baseCodec = getCodecFromType(unresolveOptional(parameter.type))
        Expression fieldOf
        if (!isOptional(parameter.type))
            fieldOf = new MethodCallExpression(baseCodec, 'fieldOf', new ArgumentListExpression(new ConstantExpression(parameter.name)))
        else
            fieldOf = new MethodCallExpression(baseCodec, 'optionalFieldOf', new ArgumentListExpression(new ConstantExpression(parameter.name)))
        Expression forGetter

        ClassNode redirected = makeWithoutCaching(parent.name)
        redirected.redirect = parent

        if (isOptional(field.type) || !isOptional(parameter.type))
            forGetter = new MethodCallExpression(fieldOf, 'forGetter', new ArgumentListExpression(
                    new LambdaExpression(new Parameter[] {new Parameter(redirected, 'it')}, new ReturnStatement(
                            new PropertyExpression(new VariableExpression('it', redirected), parameter.name)
                    )).tap {
                        variableScope = new VariableScope()
                    }
            ))
        else
            forGetter = new MethodCallExpression(fieldOf, 'forGetter', new ArgumentListExpression(
                    new LambdaExpression(new Parameter[] {new Parameter(redirected, 'it')}, new ReturnStatement(
                            new StaticMethodCallExpression(OPTIONAL, 'ofNullable', new ArgumentListExpression(
                                    new PropertyExpression(new VariableExpression('it', redirected), parameter.name)
                            ))
                    )).tap {
                        variableScope = new VariableScope()
                    }
            ))
        return forGetter
    }

    static ClassNode unresolveOptional(ClassNode toResolve) {
        if (toResolve == OPTIONAL) {
            if (!toResolve.usingGenerics) {
                throw new RuntimeException('Constructor parameters and their matching fields in codec-serializable classes may not use a raw Optional')
            }
            return toResolve.genericsTypes[0].type
        }
        return toResolve
    }

    static boolean isOptional(ClassNode node) {
        return node == OPTIONAL
    }

    static Expression getCodecFromType(ClassNode clazz) {
        if (clazz == ClassHelper.boolean_TYPE || clazz == ClassHelper.Boolean_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'BOOL')
        if (clazz == ClassHelper.short_TYPE || clazz == ClassHelper.Short_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'SHORT')
        if (clazz == ClassHelper.byte_TYPE || clazz == ClassHelper.Byte_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'BYTE')
        if (clazz == ClassHelper.int_TYPE || clazz == ClassHelper.Integer_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'INT')
        if (clazz == ClassHelper.long_TYPE || clazz == ClassHelper.Long_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'LONG')
        if (clazz == ClassHelper.float_TYPE || clazz == ClassHelper.Float_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'FLOAT')
        if (clazz == ClassHelper.double_TYPE || clazz == ClassHelper.Double_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'DOUBLE')
        if (clazz == ClassHelper.STRING_TYPE)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'STRING')
        if (clazz == BYTE_BUFFER)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'BYTE_BUFFER')
        if (clazz == INT_STREAM)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'INT_STREAM')
        if (clazz == LONG_STREAM)
            return new PropertyExpression(new ClassExpression(CODEC_NODE), 'LONG_STREAM')
        if (clazz == ClassHelper.LIST_TYPE) {
            if (!clazz.usingGenerics) {
                throw new RuntimeException('Constructor parameters and their matching fields in codec-serializable classes may not use a raw List')
            }
            ClassNode child = clazz.genericsTypes[0].type
            Expression childExpression = getCodecFromType(child)
            return new MethodCallExpression(childExpression, 'listOf', new ArgumentListExpression())
        }
        if (clazz == ClassHelper.MAP_TYPE) {
            if (!clazz.usingGenerics) {
                throw new RuntimeException('Constructor parameters and their matching fields in codec-serializable classes may not use a raw Map')
            }
            ClassNode key = clazz.genericsTypes[0].type
            ClassNode value = clazz.genericsTypes[0].type
            Expression keyExpression = getCodecFromType(key)
            Expression valueExpression = getCodecFromType(value)
            return new StaticMethodCallExpression(CODEC_NODE, 'unboundedMap', new ArgumentListExpression(
                    keyExpression, valueExpression
            ))
        }
        if (clazz == PAIR) {
            if (!clazz.usingGenerics) {
                throw new RuntimeException('Constructor parameters and their matching fields in codec-serializable classes may not use a raw Pair')
            }
            ClassNode left = clazz.genericsTypes[0].type
            ClassNode right = clazz.genericsTypes[0].type
            Expression leftExpression = getCodecFromType(left)
            Expression rightExpression = getCodecFromType(right)
            return new StaticMethodCallExpression(CODEC_NODE, 'pair', new ArgumentListExpression(
                    leftExpression, rightExpression
            ))
        }
        if (clazz == EITHER) {
            if (!clazz.usingGenerics) {
                throw new RuntimeException('Constructor parameters and their matching fields in codec-serializable classes may not use a raw Pair')
            }
            ClassNode left = clazz.genericsTypes[0].type
            ClassNode right = clazz.genericsTypes[0].type
            Expression leftExpression = getCodecFromType(left)
            Expression rightExpression = getCodecFromType(right)
            return new StaticMethodCallExpression(CODEC_NODE, 'either', new ArgumentListExpression(
                    leftExpression, rightExpression
            ))
        }
        if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(clazz, STRING_REPRESENTABLE) && StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(clazz, ClassHelper.Enum_Type)) {
            return new StaticMethodCallExpression(STRING_REPRESENTABLE, 'fromEnum',
                    new MethodReferenceExpression(new ClassExpression(clazz), new ConstantExpression('values')))
        }
        List<String> givenFields = clazz.annotations.findAll {it.getClassNode() == MY_TYPE }.collect {(String) getMemberValue(it, 'property', DEFAULT_CODEC_PROPERTY)}
        if (givenFields.size() >= 1) {
            return new PropertyExpression(new ClassExpression(clazz), givenFields[0])
        }
        List<FieldNode> codecFields = clazz.fields.findAll {
            if (!it.static)
                return false
            if (it.type != CODEC_NODE)
                return false
            if (!it.type.usingGenerics)
                return false
            return it.type.genericsTypes[0].type == clazz
        }
        if (codecFields.size() == 1) {
            return new PropertyExpression(new ClassExpression(clazz), codecFields[0].name)
        }
        throw new RuntimeException("A codec cannot be found for type ${clazz.toString(false)}.")
    }

    @Override
    int priority() {
        return -1
    }
}
