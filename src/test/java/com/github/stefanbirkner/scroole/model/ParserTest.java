package com.github.stefanbirkner.scroole.model;

import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {
    private static final List<Field> NO_FIELDS = emptyList();
    private static final String NO_JAVA_DOC = "";
    private final Parser parser = new Parser();

    @Test
    public void creates_model_for_class_in_default_package() {
        ClassSpecification model = parser.parse("dummyClass", "");
        assertThat(model).isEqualTo(
                new ClassSpecification("dummyClass", NO_JAVA_DOC, NO_FIELDS));
    }

    @Test
    public void creates_model_for_class_with_package() {
        ClassSpecification model = parser.parse("dummy.package.dummyClass", "");
        assertThat(model).isEqualTo(new ClassSpecification(
                "dummy.package", "dummyClass", NO_JAVA_DOC, NO_FIELDS));
    }

    @Test
    public void creates_model_for_class_with_javadoc() {
        ClassSpecification model = parser.parse(
                "dummyClass", "#some javadoc\n#@since 0.1.0\n\ntitle:String");
        assertThat(model.javadoc).isEqualTo("some javadoc\n@since 0.1.0");
    }

    @Test
    public void creates_model_for_class_with_field() {
        ClassSpecification model = parser.parse("dummyClass", "title:String");
        assertThat(model.fields).containsExactly(
                new Field("title", "String", NO_JAVA_DOC));
    }

    @Test
    public void creates_model_for_field_with_multiline_javadoc() {
        ClassSpecification model = parser.parse(
                "dummyClass", "#some javadoc\n#@since 0.1.0\ntitle:String");
        assertThat(model.fields).containsExactly(
                new Field("title", "String", "some javadoc\n@since 0.1.0"));
    }

    @Test
    public void does_not_add_leading_spaces_to_field_name() {
        ClassSpecification model = parser.parse("dummyClass", "  title:String");
        assertThat(model.fields.get(0).name).isEqualTo("title");
    }

    @Test
    public void does_not_add_trailing_spaces_to_field_name() {
        ClassSpecification model = parser.parse("dummyClass", "title  :String");
        assertThat(model.fields.get(0).name).isEqualTo("title");
    }

    @Test
    public void does_not_add_leading_spaces_to_type() {
        ClassSpecification model = parser.parse("dummyClass", "title:  String");
        assertThat(model.fields.get(0).type).isEqualTo("String");
    }

    @Test
    public void does_not_add_trailing_spaces_to_type() {
        ClassSpecification model = parser.parse("dummyClass", "title:String  ");
        assertThat(model.fields.get(0).type).isEqualTo("String");
    }

    @Test
    public void removes_first_whitespace_from_javadoc() {
        ClassSpecification model = parser.parse(
                "dummyClass", "# some javadoc\n\ntitle:String");
        assertThat(model.javadoc).isEqualTo("some javadoc");
    }

    @Test
    public void handles_linux_new_line() {
        parser.parse("dummyClass", "\n");
        //everything is ok if no exception is thrown
    }

    @Test
    public void handles_windows_new_line() {
        parser.parse("dummyClass", "\r\n");
        //everything is ok if no exception is thrown
    }
}
