package com.github.stefanbirkner.scroole;

import com.github.stefanbirkner.scroole.model.ClassSpecification;
import com.github.stefanbirkner.scroole.model.Field;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.ClassName.bestGuess;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Creates the Java code for a {@link ClassSpecification}.
 */
public class CodeGenerator {
    private static final FieldMapper FIELD_MAPPER = new FieldMapper();

    /**
     * Creates Java code for a class according to its
     * {@link ClassSpecification}.
     *
     * @param specification a {@code ClassSpecification} that specifies a
     *                      class.
     * @return the Java code of the class.
     */
    public String createCode(ClassSpecification specification) {
        TypeSpec typeSpec = getTypeSpec(specification);
        return JavaFile.builder(specification.packageName, typeSpec)
                .build().toString();
    }

    private TypeSpec getTypeSpec(ClassSpecification specification) {
        TypeSpec.Builder builder = classBuilder(specification.simpleName)
                .addModifiers(Modifier.PUBLIC);
        List<ExtendedFieldSpec> fields = FIELD_MAPPER.map(specification.fields);
        List<FieldSpec> fieldSpecs = extractFieldSpecs(fields);
        addFields(builder, fieldSpecs);
        addConstructor(builder, fieldSpecs);
        addGetters(builder, fields);
        addHashCode(builder, fields);
        addEquals(builder, specification.simpleName, fields);
        return builder.build();
    }

    private List<FieldSpec> extractFieldSpecs(List<ExtendedFieldSpec> fields) {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        for (ExtendedFieldSpec field : fields)
            fieldSpecs.add(field.fieldSpec);
        return fieldSpecs;
    }

    private void addFields(TypeSpec.Builder builder, List<FieldSpec> fields) {
        for (FieldSpec field : fields)
            builder.addField(field);
    }

    private void addConstructor(TypeSpec.Builder builder,
            List<FieldSpec> fields) {
        MethodSpec constructor = createConstructor(fields);
        builder.addMethod(constructor);
    }

    private MethodSpec createConstructor(List<FieldSpec> fields) {
        MethodSpec.Builder constructor = constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        for (FieldSpec field : fields)
            addParameter(constructor, field);
        return constructor.build();
    }

    private void addParameter(MethodSpec.Builder builder, FieldSpec field) {
        builder.addParameter(field.type, field.name);
        builder.addStatement("this.$L = $L", field.name, field.name);
    }

    private void addGetters(TypeSpec.Builder builder,
            List<ExtendedFieldSpec> fields) {
        for (ExtendedFieldSpec field : fields)
            builder.addMethod(createGetter(field));
    }

    private MethodSpec createGetter(ExtendedFieldSpec field) {
        String name = "get" + capitalize(field.fieldSpec.name);
        return MethodSpec.methodBuilder(name)
                .returns(field.fieldSpec.type)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $L", field.fieldSpec.name)
                .build();
    }

    private void addHashCode(TypeSpec.Builder builder,
            List<ExtendedFieldSpec> fields) {
        MethodSpec.Builder method = methodBuilder("hashCode")
                .returns(TypeName.INT)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("int prime = 31")
                .addStatement("int result = 1");
        for (ExtendedFieldSpec field : fields)
            field.type.addHashCodeStatement(method, field.fieldSpec);
        method.addStatement("return result");
        builder.addMethod(method.build());
    }

    private void addEquals(TypeSpec.Builder builder, String simpleName,
            List<ExtendedFieldSpec> fields) {
        builder.addMethod(methodBuilder("equals")
                .returns(TypeName.BOOLEAN)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(objectParameter("other"))
                .addStatement("if (other == this)\nreturn true")
                .addStatement("else if (other == null || getClass() != other.getClass())\nreturn false")
                .addStatement("$L that = ($L) other", simpleName, simpleName)
                .addStatement(createCompareFieldsStatement(fields))
                .build());
        if (hasAtLeastOneObjectField(fields)) {
            builder.addMethod(methodBuilder("equals")
                    .returns(TypeName.BOOLEAN)
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(objectParameter("left"))
                    .addParameter(objectParameter("right"))
                    .addStatement("if (left == null)\nreturn right == null")
                    .addStatement("else\nreturn left.equals(right)")
                    .build());
        }
    }

    private ParameterSpec objectParameter(String name) {
        return ParameterSpec.builder(TypeName.OBJECT, name).build();
    }

    private String createCompareFieldsStatement(
            List<ExtendedFieldSpec> fields) {
        List<String> comparisons = new ArrayList<>();
        for (ExtendedFieldSpec field : fields)
            comparisons.add(field.type.getEqualsStatement(field.fieldSpec));
        return "return " + join(comparisons, "\n    && ");
    }

    private boolean hasAtLeastOneObjectField(List<ExtendedFieldSpec> fields) {
        for (ExtendedFieldSpec field : fields)
            if (field.type == FieldType.OBJECT)
                return true;
        return false;
    }

    private enum FieldType {
        ARRAY {
            @Override
            String getEqualsStatement(FieldSpec fieldSpec) {
                return "java.util.Arrays.equals(" + fieldSpec.name + ", that."
                        + fieldSpec.name + ")";
            }

            @Override
            void addHashCodeStatement(MethodSpec.Builder method,
                    FieldSpec fieldSpec) {
                add(method, "java.util.Arrays.hashCode(" + fieldSpec.name
                        + ")");
            }
        },
        OBJECT {
            @Override
            String getEqualsStatement(FieldSpec fieldSpec) {
                return "equals(" + fieldSpec.name + ", that." + fieldSpec.name
                        + ")";
            }

            @Override
            void addHashCodeStatement(MethodSpec.Builder method,
                    FieldSpec fieldSpec) {
                add(method, "(" + fieldSpec.name + " == null ? 0 : "
                        + fieldSpec.name + ".hashCode())");
            }
        },
        PRIMITIVE {
            @Override
            String getEqualsStatement(FieldSpec fieldSpec) {
                TypeName type = fieldSpec.type;
                String name = fieldSpec.name;
                if (type.equals(TypeName.DOUBLE))
                    return "java.lang.Double.doubleToLongBits(" + name
                            + ") == java.lang.Double.doubleToLongBits(that."
                            + name + ")";
                else if (type.equals(TypeName.FLOAT))
                    return "java.lang.Float.floatToIntBits(" + name
                            + ") == java.lang.Float.floatToIntBits(that." + name
                            + ")";
                else
                    return name + " == that." + name;
            }

            @Override
            void addHashCodeStatement(MethodSpec.Builder method,
                    FieldSpec fieldSpec) {
                TypeName type = fieldSpec.type;
                String name = fieldSpec.name;
                if (type.equals(TypeName.BOOLEAN))
                    add(method, "(" + name + " ? 1231 : 1237)");
                else if (type.equals(TypeName.DOUBLE)) {
                    method.addStatement("long temp = Double.doubleToLongBits("
                            + name + ")");
                    add(method, "(int) (temp ^ (temp >>> 32))");
                } else if (type.equals(TypeName.FLOAT))
                    add(method, "Float.floatToIntBits(" + name
                            + ")");
                else if (type.equals(TypeName.LONG))
                    add(method, "(int) (" + name + " ^ (" + name + " >>> 32))");
                else
                    add(method, name);
            }
        };

        abstract String getEqualsStatement(FieldSpec fieldSpec);

        abstract void addHashCodeStatement(MethodSpec.Builder method,
                FieldSpec fieldSpec);

        void add(MethodSpec.Builder method, String summand) {
            method.addStatement("result = prime * result + " + summand);
        }
    }

    private static class FieldMapper {
        private static final Map<String, TypeName> PRIMITIVE_TYPES =
                new HashMap<String, TypeName>() {{
                    put("boolean", TypeName.BOOLEAN);
                    put("byte", TypeName.BYTE);
                    put("short", TypeName.SHORT);
                    put("int", TypeName.INT);
                    put("long", TypeName.LONG);
                    put("char", TypeName.CHAR);
                    put("float", TypeName.FLOAT);
                    put("double", TypeName.DOUBLE);
                }};

        List<ExtendedFieldSpec> map(List<Field> fields) {
            List<ExtendedFieldSpec> specs = new ArrayList<>();
            for (Field field : fields)
                specs.add(map(field));
            return specs;
        }

        private ExtendedFieldSpec map(Field field) {
            FieldType type = getType(field.type);
            TypeName typeName = getTypeName(field.type);
            FieldSpec fieldSpec = FieldSpec.builder(typeName, field.name,
                    Modifier.PRIVATE, Modifier.FINAL).build();
            return new ExtendedFieldSpec(fieldSpec, type);
        }

        private FieldType getType(String typeAsString) {
            if (PRIMITIVE_TYPES.containsKey(typeAsString))
                return FieldType.PRIMITIVE;
            else if (typeAsString.endsWith("[]"))
                return FieldType.ARRAY;
            else
                return FieldType.OBJECT;
        }

        private TypeName getTypeName(String type) {
            switch (getType(type)) {
                case ARRAY:
                    return ArrayTypeName.of(getTypeName(
                            substringBeforeLast(type, "[]")));
                case PRIMITIVE:
                    return PRIMITIVE_TYPES.get(type);
                case OBJECT:
                    return getTypeNameForClass(type);
                default:
                    throw new IllegalArgumentException("The type "
                            + getType(type) + " is not supported.");
            }
        }

        private TypeName getTypeNameForClass(String type) {
            if (type.endsWith(">"))
                return getTypeNameForParameterizedClass(type);
            else
                return bestGuess(type);
        }

        private TypeName getTypeNameForParameterizedClass(String type) {
            ClassName className = bestGuess(substringBefore(type, "<"));
            TypeName[] typeArguments = getTypeArguments(type);
            return ParameterizedTypeName.get(className, typeArguments);
        }

        private TypeName[] getTypeArguments(String type) {
            List<TypeName> typeArguments = new ArrayList<>();
            for (String typeArgument : substringAfter(
                    substringBeforeLast(type, ">"), "<").split(","))
                typeArguments.add(getTypeName(typeArgument.trim()));
            return typeArguments.toArray(new TypeName[typeArguments.size()]);
        }
    }

    private static class ExtendedFieldSpec {
        final FieldSpec fieldSpec;
        final FieldType type;

        ExtendedFieldSpec(FieldSpec fieldSpec,
                FieldType type) {
            this.fieldSpec = fieldSpec;
            this.type = type;
        }
    }
}
