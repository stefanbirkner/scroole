package com.github.stefanbirkner.scroole.model;

/**
 * Specification of a single field.
 */
public class Field {
    /**
     * The field's name.
     */
    public final String name;

    /**
     * The field's type.
     */
    public final String type;

    /**
     * The Javadoc of the field's getter.
     */
    public final String javadoc;

    /**
     * Creates the specification of a field.
     * @param name the field's name.
     * @param type the field's type.
     * @param javadoc the Javadoc of the field's getter.
     */
    public Field(String name, String type, String javadoc) {
        this.name = name;
        this.type = type;
        this.javadoc = javadoc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (name != null ? !name.equals(field.name) : field.name != null)
            return false;
        if (type != null ? !type.equals(field.type) : field.type != null)
            return false;
        if (javadoc != null ? !javadoc.equals(field.javadoc) : field.javadoc != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (javadoc != null ? javadoc.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", javadoc='" + javadoc + '\'' +
                '}';
    }
}
