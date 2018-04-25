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

import java.util.HashMap;
import java.util.Map;

/**
 * In order to load re-compiled classes we need
 * to have a separate class-loader for each reload.
 *
 * Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class AppClassLoader extends ClassLoader {

    private final Map<String, ClassFile> classFiles = new HashMap<>();

    public AppClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void setCode(ClassFile cc) {
        classFiles.put(cc.getName(), cc);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ClassFile cc = classFiles.get(name);
        if (cc == null) {
            return super.findClass(name);
        }
        byte[] byteCode = cc.getByteCode();
        return defineClass(name, byteCode, 0, byteCode.length);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // we are cheating here, the parent already has the class, but we prefer our bytecode to be used.
        ClassFile cc = classFiles.get(name);
        if (cc == null) {
            return super.loadClass(name);
        }
        byte[] byteCode = cc.getByteCode();
        return defineClass(name, byteCode, 0, byteCode.length);
    }
}
