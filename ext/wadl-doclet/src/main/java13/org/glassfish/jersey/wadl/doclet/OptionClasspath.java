package org.glassfish.jersey.wadl.doclet;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import jdk.javadoc.doclet.Doclet.Option;

class OptionClasspath implements Option {

    private final List<String> argNames = Arrays.asList("-classpath");
    private String[] classpathElements;

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Specifies classpath split by :";
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
        return "classpath";
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        classpathElements =  arguments.get(0).split(File.pathSeparator);
        return true;
    }

    public String[] getClasspathElements() {
        return classpathElements;
    }

}
