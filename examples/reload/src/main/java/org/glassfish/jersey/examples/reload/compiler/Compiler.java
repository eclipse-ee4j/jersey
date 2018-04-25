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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Java compiler utility.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class Compiler {

    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    /**
     * Compiler classpath.
     */
    public static String classpath;

    /**
     * Compiles a single class and loads the class using a new class loader.
     *
     * @param className class to compile.
     * @param sourceCode source code of the class to compile.
     * @return loaded class
     * @throws Exception
     */
    public static Class<?> compile(String className, SimpleJavaFileObject sourceCode) throws Exception {
        ClassFile classFile = new ClassFile(className);

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceCode);
        AppClassLoader cl = new AppClassLoader(Thread.currentThread().getContextClassLoader());
        FileManager fileManager = new FileManager(javac.getStandardFileManager(null, null, null), Arrays.asList(classFile), cl);
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, null, getClOptions(), null, compilationUnits);
        task.call();
        return cl.loadClass(className);
    }


    /**
     * Compiles multiple source files at once.
     *
     * @param appClassLoader common class loader for the classes.
     * @param javaFiles source files to compile.
     * @throws Exception in case something goes wrong.
     */
    public static void compile(AppClassLoader appClassLoader, List<JavaFile> javaFiles) throws Exception {

        List<ClassFile> classes = new LinkedList<>();

        for (JavaFile javaFile : javaFiles) {
            classes.add(new ClassFile(javaFile.getClassName()));
        }
        Iterable<? extends JavaFileObject> compilationUnits = javaFiles;

        FileManager fileManager = new FileManager(javac.getStandardFileManager(null, null, null), classes, appClassLoader);
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, null, getClOptions(), null, compilationUnits);
        task.call();
    }

    private static List<String> getClOptions() {
        List<String> optionList = new ArrayList<>();
        optionList.addAll(Arrays.asList("-classpath", classpath + File.pathSeparator + "target/classes"));
        return optionList;
    }
}
