package com.github.stefanbirkner.scroole.model;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

/**
 * Creates a {@link ClassSpecification} from Scroole code.
 */
public class Parser {
    /**
     * Create a {@link ClassSpecification} from Scroole code.
     *
     * @param canonicalName the canonical name of the class.
     * @param scrooleCode   the class' Scroole code.
     * @return the {@link ClassSpecification} defined by the Scroole code.
     */
    public ClassSpecification parse(String canonicalName, String scrooleCode) {
        ClassModelBuilder builder = new ClassModelBuilder();
        builder.setPackageName(getPackageForCanonicalName(canonicalName));
        builder.setSimpleName(getSimpleNameForCanonicalName(canonicalName));
        parseScrooleCode(scrooleCode, builder);
        return builder.toClassModel();
    }

    private String getPackageForCanonicalName(String canonicalName) {
        if (isClassWithPackage(canonicalName))
            return substringBeforeLast(canonicalName, ".");
        else
            return "";
    }

    private String getSimpleNameForCanonicalName(String canonicalName) {
        if (isClassWithPackage(canonicalName))
            return substringAfterLast(canonicalName, ".");
        else
            return canonicalName;
    }

    private boolean isClassWithPackage(String canonicalName) {
        return canonicalName.contains(".");
    }

    private void parseScrooleCode(String scrooleCode,
            ClassModelBuilder builder) {
        EventHandler handler = new EventHandler(builder);
        for (String line : scrooleCode.split("\n"))
            handler.line(line);
    }

    private static class EventHandler {
        ClassModelBuilder builder;

        EventHandler(ClassModelBuilder builder) {
            this.builder = builder;
        }

        void line(String line) {
            if (!line.trim().isEmpty())
                handleFieldLine(line);
        }

        void handleFieldLine(String line) {
            String[] nameAndType = line.split(":");
            Field field = new Field(nameAndType[0].trim(),
                    nameAndType[1].trim());
            builder.addField(field);
        }
    }

    private static class ClassModelBuilder {
        String packageName;
        String simpleName;
        List<Field> fields = new ArrayList<>();

        void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        void setSimpleName(String simpleName) {
            this.simpleName = simpleName;
        }

        void addField(Field field) {
            fields.add(field);
        }

        ClassSpecification toClassModel() {
            return new ClassSpecification(
                    packageName, simpleName, fields);
        }
    }
}
