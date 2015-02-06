package com.github.stefanbirkner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CheckListTest {
    @Test
    public void creates_object() {
        new CheckList("dummy title", asList(
            new CheckListItem("first item"),
            new CheckListItem("second item")));
    }

    @Test
    public void returns_field() {
        CheckListItem item = new CheckListItem("dummy name");
        assertEquals("dummy name", item.getName());
    }

    @Test
    public void creates_objects_that_are_equal() {
        CheckList firstList = new CheckList("dummy title", asList(
            new CheckListItem("first item"),
            new CheckListItem("second item")));
        CheckList secondList = new CheckList("dummy title", asList(
            new CheckListItem("first item"),
            new CheckListItem("second item")));
        assertEquals(firstList, secondList);
    }

    @Test
    public void creates_objects_that_have_the_same_hash_code_if_equal() {
        CheckList firstList = new CheckList("dummy title", asList(
            new CheckListItem("first item"),
            new CheckListItem("second item")));
        CheckList secondList = new CheckList("dummy title", asList(
            new CheckListItem("first item"),
            new CheckListItem("second item")));
        assertEquals(firstList.hashCode(), secondList.hashCode());

    }
}
