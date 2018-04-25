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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.tools.SimpleJavaFileObject;

/**
 * Java source file representation.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JavaFile extends SimpleJavaFileObject {

    private final String className;
    private final String path;

    public JavaFile(String className, String path) throws Exception {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.className = className;
        this.path = path;
    }

    /**
     * Class name getter.
     *
     * @return class name.
     */
    public String getClassName() {
        return className;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {

        String filePath = path + File.separator + className.replace('.', '/') + Kind.SOURCE.extension;
        final byte[] bytes = Files.readAllBytes(Paths.get(filePath));

        return new String(bytes);
    }
}
