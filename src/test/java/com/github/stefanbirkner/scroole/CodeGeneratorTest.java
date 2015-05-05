package com.github.stefanbirkner.scroole;

import com.github.stefanbirkner.scroole.model.ClassSpecification;
import com.github.stefanbirkner.scroole.model.Field;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.assertj.core.api.Assertions.assertThat;

public class CodeGeneratorTest {
    private static final List<Field> NO_FIELDS = emptyList();
    private static final String NO_JAVA_DOC = "";
    private final CodeGenerator codeGenerator = new CodeGenerator();

    @Test
    public void creates_empty_class_in_default_package() {
        ClassSpecification model = new ClassSpecification("DummyClass", NO_JAVA_DOC, NO_FIELDS);
        String code = codeGenerator.createCode(model);
        assertThat(code).startsWith("import java.lang.Object;\n"
                + "import java.lang.Override;\n\npublic class DummyClass {\n");
    }

    @Test
    public void adds_package() {
        ClassSpecification model = new ClassSpecification("a.b", "DummyClass", NO_JAVA_DOC,
                NO_FIELDS);
        String code = codeGenerator.createCode(model);
        assertThat(code).startsWith("package a.b;\n");
    }

    @Test
    public void adds_javadoc_for_class() {
        ClassSpecification model = new ClassSpecification("a.b", "DummyClass", "dummy javadoc",
                NO_FIELDS);
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "/**",
                " * dummy javadoc",
                " */",
                "public"));
    }

    @Test
    public void creates_constructor_with_fields() {
        ClassSpecification model = new ClassSpecification("DummyClass", NO_JAVA_DOC, asList(
                new Field("title", "String", NO_JAVA_DOC),
                new Field("text", "String", NO_JAVA_DOC)));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(
                "public DummyClass(String title, String text) {\n");
    }

    @Test
    public void creates_private_final_fields() {
        ClassSpecification model = classModelWithFields(
                new Field("title", "String", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains("private final String title;");
    }

    @Test
    public void creates_constructor_that_assigns_parameters_to_fields() {
        ClassSpecification model = classModelWithFields(
                new Field("title", "String", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains("this.title = title;");
    }

    @Test
    public void creates_getter_for_object() {
        ClassSpecification model = classModelWithFields(
                new Field("title", "String", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  public String getTitle() {",
                "    return title;",
                "  }"));
    }

    @Test
    public void creates_getter_for_primitive() {
        ClassSpecification model = classModelWithFields(
                new Field("count", "int", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  public int getCount() {",
                "    return count;",
                "  }"));
    }

    @Test
    public void creates_getter_for_array() {
        ClassSpecification model = classModelWithFields(
                new Field("names", "int[]", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  public int[] getNames() {",
                "    return names;",
                "  }"));
    }

    @Test
    public void creates_getter_for_generics() {
        ClassSpecification model = classModelWithFields(new Field("map",
                "java.util.Map<java.lang.String, java.lang.Integer>",
                NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  public Map<String, Integer> getMap() {",
                "    return map;",
                "  }"));
    }

    @Test
    public void adds_javadoc_for_getter() {
        ClassSpecification model = classModelWithFields(
                new Field("count", "int", "dummy javadoc"));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  /**",
                "   * dummy javadoc",
                "   */",
                "  public"));
    }

    @Test
    public void creates_equals_method_with_identity_check_for_primitive_field() {
        ClassSpecification model = classModelWithFields(
                new Field("count", "int", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  @Override",
                "  public boolean equals(Object other) {",
                "    if (other == this)",
                "        return true;",
                "    else if (other == null || getClass() != other.getClass())",
                "        return false;",
                "    DummyClass that = (DummyClass) other;",
                "    return count == that.count;",
                "  }"));
    }

    @Test
    public void creates_equals_method_with_equality_check_for_object_field() {
        ClassSpecification model = classModelWithFields(
                new Field("text", "java.lang.String", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  @Override",
                "  public boolean equals(Object other) {",
                "    if (other == this)",
                "        return true;",
                "    else if (other == null || getClass() != other.getClass())",
                "        return false;",
                "    DummyClass that = (DummyClass) other;",
                "    return equals(text, that.text);",
                "  }",
                "",
                "  private boolean equals(Object left, Object right) {",
                "    if (left == null)",
                "        return right == null;",
                "    else",
                "        return left.equals(right);",
                "  }"));
    }

    @Test
    public void creates_equals_method_that_uses_arrays_equals_for_arrays() {
        ClassSpecification model = classModelWithFields(
                new Field("numbers", "int[]", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  @Override",
                "  public boolean equals(Object other) {",
                "    if (other == this)",
                "        return true;",
                "    else if (other == null || getClass() != other.getClass())",
                "        return false;",
                "    DummyClass that = (DummyClass) other;",
                "    return java.util.Arrays.equals(numbers, that.numbers);",
                "  }"));
    }

    @Test
    public void creates_equals_method_that_checks_the_bits_of_floats() {
        ClassSpecification model = classModelWithFields(
                new Field("value", "float", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  @Override",
                "  public boolean equals(Object other) {",
                "    if (other == this)",
                "        return true;",
                "    else if (other == null || getClass() != other.getClass())",
                "        return false;",
                "    DummyClass that = (DummyClass) other;",
                "    return java.lang.Float.floatToIntBits(value) == java.lang.Float.floatToIntBits(that.value);",
                "  }"));
    }

    @Test
    public void creates_equals_method_that_checks_the_bits_of_doubles() {
        ClassSpecification model = classModelWithFields(
                new Field("value", "double", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  @Override",
                "  public boolean equals(Object other) {",
                "    if (other == this)",
                "        return true;",
                "    else if (other == null || getClass() != other.getClass())",
                "        return false;",
                "    DummyClass that = (DummyClass) other;",
                "    return java.lang.Double.doubleToLongBits(value) == java.lang.Double.doubleToLongBits(that.value);",
                "  }"));
    }

    @Test
    public void creates_equals_method_that_checks_every_field() {
        ClassSpecification model = classModelWithFields(
                new Field("count", "int", NO_JAVA_DOC),
                new Field("text", "java.lang.String", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  @Override",
                "  public boolean equals(Object other) {",
                "    if (other == this)",
                "        return true;",
                "    else if (other == null || getClass() != other.getClass())",
                "        return false;",
                "    DummyClass that = (DummyClass) other;",
                "    return count == that.count",
                "            && equals(text, that.text);",
                "  }"));
    }

    @Test
    public void creates_hashCode_method_that_considers_every_field() {
        ClassSpecification model = classModelWithFields(
                new Field("a", "boolean", NO_JAVA_DOC),
                new Field("b", "byte", NO_JAVA_DOC),
                new Field("c", "short", NO_JAVA_DOC),
                new Field("d", "int", NO_JAVA_DOC),
                new Field("e", "long", NO_JAVA_DOC),
                new Field("f", "char", NO_JAVA_DOC),
                new Field("g", "float", NO_JAVA_DOC),
                new Field("h", "double", NO_JAVA_DOC),
                new Field("i", "int[]", NO_JAVA_DOC),
                new Field("j", "java.lang.String", NO_JAVA_DOC));
        String code = codeGenerator.createCode(model);
        assertThat(code).contains(multipleRows(
                "  @Override",
                "  public int hashCode() {",
                "    int prime = 31;",
                "    int result = 1;",
                "    result = prime * result + (a ? 1231 : 1237);",
                "    result = prime * result + b;",
                "    result = prime * result + c;",
                "    result = prime * result + d;",
                "    result = prime * result + (int) (e ^ (e >>> 32));",
                "    result = prime * result + f;",
                "    result = prime * result + Float.floatToIntBits(g);",
                "    long temp = Double.doubleToLongBits(h);",
                "    result = prime * result + (int) (temp ^ (temp >>> 32));",
                "    result = prime * result + java.util.Arrays.hashCode(i);",
                "    result = prime * result + (j == null ? 0 : j.hashCode());",
                "    return result;",
                "  }"));
    }

    private ClassSpecification classModelWithFields(Field... fields) {
        return new ClassSpecification("DummyClass", NO_JAVA_DOC, asList(fields));
    }

    private String multipleRows(String... lines) {
        return join(lines, "\n");
    }
}
