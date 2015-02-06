package com.github.stefanbirkner.scroole.model;

import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {
    private static final List<Field> NO_FIELDS = emptyList();
    private final Parser parser = new Parser();

    @Test
    public void creates_model_for_class_in_default_package() {
        ClassSpecification model = parser.parse("dummyClass", "");
        assertThat(model).isEqualTo(
                new ClassSpecification("dummyClass", NO_FIELDS));
    }

    @Test
    public void creates_model_for_class_with_package() {
        ClassSpecification model = parser.parse("dummy.package.dummyClass", "");
        assertThat(model).isEqualTo(new ClassSpecification(
                "dummy.package", "dummyClass", NO_FIELDS));
    }

    @Test
    public void creates_model_for_class_with_field() {
        ClassSpecification model = parser.parse("dummyClass", "title:String");
        assertThat(model.fields).containsExactly(
                new Field("title", "String"));
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
