/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.reload.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.SimpleJavaFileObject;

/**
 * Class file representation.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ClassFile extends SimpleJavaFileObject {

    private final String className;

    private final ByteArrayOutputStream byteCode = new ByteArrayOutputStream();

    /**
     * Creates a new class file holder.
     *
     * @param className class name.
     * @throws URISyntaxException in case given class name could not be represented as an URI.
     */
    public ClassFile(String className) throws URISyntaxException {
        super(new URI(className), Kind.CLASS);
        this.className = className;
    }

    /**
     * Getter for class name associated with this class file.
     *
     * @return class name.
     */
    public String getClassName() {
        return className;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return byteCode;
    }

    /**
     * Returns byte code representation of the class after the class has been compiled.
     *
     * @return compiled byte code of the class.
     */
    public byte[] getByteCode() {
        return byteCode.toByteArray();
    }
}
