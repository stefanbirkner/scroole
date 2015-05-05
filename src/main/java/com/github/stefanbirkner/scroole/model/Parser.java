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
        boolean classCommentSet = false;
        StringBuilder comment = new StringBuilder();

        EventHandler(ClassModelBuilder builder) {
            this.builder = builder;
        }

        void line(String line) {
            if (line.trim().isEmpty())
                handleSeparator();
            else if (line.startsWith("#"))
                handleCommentLine(line);
            else
                handleFieldLine(line);
        }

        private void handleSeparator() {
            if (!classCommentSet)
                builder.setJavadoc(comment.toString());
            classCommentSet = true;
            clearComment();
        }

        private void handleCommentLine(String line) {
            if (comment.length() != 0) {
                comment.append("\n");
            }
            //skip # symbol and following whitespace separator
            String lineOfComment = line.startsWith("# ") ? line.substring(2)
                    : line.substring(1);
            comment.append(lineOfComment);
        }

        void handleFieldLine(String line) {
            String[] nameAndType = line.split(":");
            Field field = new Field(nameAndType[0].trim(),
                    nameAndType[1].trim(), comment.toString());
            builder.addField(field);
            clearComment();
        }

        private void clearComment() {
            comment = new StringBuilder();
        }
    }

    private static class ClassModelBuilder {
        String packageName;
        String simpleName;
        String javadoc = "";
        List<Field> fields = new ArrayList<>();

        void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        void setSimpleName(String simpleName) {
            this.simpleName = simpleName;
        }

        void setJavadoc(String javadoc) {
            this.javadoc = javadoc;
        }

        void addField(Field field) {
            fields.add(field);
        }

        ClassSpecification toClassModel() {
            return new ClassSpecification(
                    packageName, simpleName, javadoc, fields);
        }
    }
}
