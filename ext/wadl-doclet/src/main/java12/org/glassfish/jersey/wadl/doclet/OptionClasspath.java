/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

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
