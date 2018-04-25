/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.maven.runner;

import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Mojo Execution exception that contains additional information regarding an error from an executed shell script.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class ShellMojoExecutionException extends MojoExecutionException {

    /**
     * The error code the shell script exited with.
     */
    private final int errorCode;

    /**
     * The last lines of the output of the executed shell script.
     */
    private final Collection<String> lastLines;

    /**
     * Constructs shell mojo exection exception.
     *
     * @param message The message that will be prepended to a default mojo exectuion exception message.
     * @param errorCode The error code.
     * @param lastLines The collection of last lines that will be also part of the exception message.
     */
    public ShellMojoExecutionException(final String message, final int errorCode, final Collection<String> lastLines) {
        super(message
                + "\nError exit code: " + errorCode + "."
                + "\nThe last " + lastLines.size() + " lines of stderr/stdout output are: "
                + "\n" + lastLinesToString(lastLines));
        this.errorCode = errorCode;
        this.lastLines = lastLines;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Collection<String> getLastLines() {
        return lastLines;
    }

    private static String lastLinesToString(Collection<String> collection) {
        StringBuilder sb = new StringBuilder();
        int lineNumber = 1;
        for (String string : collection) {
            sb.append("Line ").append(lineNumber++).append(": ");
            sb.append(string).append("\n");
        }
        return sb.toString();
    }
}
