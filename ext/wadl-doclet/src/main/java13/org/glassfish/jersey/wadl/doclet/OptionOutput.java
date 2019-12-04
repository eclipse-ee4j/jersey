package org.glassfish.jersey.wadl.doclet;

import java.util.Arrays;
import java.util.List;

import jdk.javadoc.doclet.Doclet.Option;

class OptionOutput implements Option {

    private final List<String> argNames = Arrays.asList("-output");
    private String value;

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Specifies the output for resourcedoc.xml";
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
        return "output";
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        value = arguments.get(0);
        return true;
    }

    public String getValue() {
        return value;
    }

}
