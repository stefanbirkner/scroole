package com.github.stefanbirkner.scroole.model;

import java.util.List;

/**
 * Specification of a class for value objects. This model stores all
 * information that is needed for creating the class' code.
 */
public class ClassSpecification {
    /**
     * The class' package (empty string if the class has no package).
     */
    public final String packageName;

    /**
     * The class' name.
     */
    public final String simpleName;

    /**
     * The class' Javadoc.
     */
    public final String javadoc;

    /**
     * Specications of the class' fields.
     */
    public final List<Field> fields;

    /**
     * Create the specification for a class inside a package.
     * @param packageName the class' package.
     * @param simpleName the class' name.
     * @param javadoc the class' Javadoc.
     * @param fields specications of the class' fields.
     */
    public ClassSpecification(String packageName, String simpleName,
            String javadoc, List<Field> fields) {
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.javadoc = javadoc;
        this.fields = fields;
    }

    /**
     * Create the specification for a class wihtout a package (aka the default
     * package).
     * @param simpleName the class' name.
     * @param javadoc the class' Javadoc.
     * @param fields specications of the class' fields.
     */
    public ClassSpecification(String simpleName, String javadoc,
            List<Field> fields) {
        this("", simpleName, javadoc, fields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassSpecification that = (ClassSpecification) o;

        if (fields != null ? !fields.equals(that.fields) : that.fields != null)
            return false;
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null)
            return false;
        if (javadoc != null ? !javadoc.equals(that.javadoc) : that.javadoc != null)
            return false;
        if (simpleName != null ? !simpleName.equals(that.simpleName) : that.simpleName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + (simpleName != null ? simpleName.hashCode() : 0);
        result = 31 * result + (javadoc != null ? javadoc.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClassSpecification{" +
                "packageName='" + packageName + '\'' +
                ", simpleName='" + simpleName + '\'' +
                ", javadoc='" + javadoc + '\'' +
                ", fields=" + fields +
                '}';
    }
}
