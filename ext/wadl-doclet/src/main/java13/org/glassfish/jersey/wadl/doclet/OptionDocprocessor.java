package org.glassfish.jersey.wadl.doclet;

import java.util.Arrays;
import java.util.List;

import jdk.javadoc.doclet.Doclet.Option;

class OptionDocprocessor implements Option {

    private final List<String> argNames = Arrays.asList("-processors");
    private String[] docProcessors = new String[0];

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Specifies the document processors split by :";
    }

    @Override
    public Kind getKind() {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
        return argNames;
    }

    @Override
    public String getParameters() {
        return "processors";
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        if (!arguments.isEmpty()) {
            docProcessors = arguments.get(0).split(":");
        }
        return true;
    }

    public String[] getDocProcessors() {
        return docProcessors;
    }

}
