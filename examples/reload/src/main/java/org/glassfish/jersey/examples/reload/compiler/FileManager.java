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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * File manager delegator to control our source and class files.
 *
 * @author Jakub Podlesak (jakub.podlesak @ oracle.com)
 */
public class FileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, ClassFile> classFiles;
    private final AppClassLoader cl;

    /**
     * Creates a new instance of FileManager.
     *
     * @param fileManager delegate to this file manager
     * @param cl
     */
    protected FileManager(JavaFileManager fileManager, List<ClassFile> classFiles, AppClassLoader cl) {
        super(fileManager);
        this.classFiles = new HashMap<>();
        this.cl = cl;
        for (ClassFile classFile : classFiles) {
            this.classFiles.put(classFile.getClassName(), classFile);
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling) throws IOException {

        final ClassFile classFile = classFiles.get(className);
        if (classFile != null) {
            this.cl.setCode(classFile);
        }

        return classFile;
    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return cl;
    }
}
