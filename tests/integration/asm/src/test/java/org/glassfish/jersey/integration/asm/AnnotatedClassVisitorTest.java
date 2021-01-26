/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.integration.asm;

import jersey.repackaged.org.objectweb.asm.ClassVisitor;
import jersey.repackaged.org.objectweb.asm.Opcodes;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class AnnotatedClassVisitorTest {

    @Test
    public void testInheritedMethodsFromClassVisitor() {
        Class<?> annotatedClassVisitorClass = null;
        final Class<?> classVisitorClass = ClassVisitor.class;

        final Class<?>[] listenerClasses = AnnotationAcceptingListener.class.getDeclaredClasses();

        for (Class<?> c : listenerClasses) {
            if (c.getName().contains("AnnotatedClassVisitor")) {
                annotatedClassVisitorClass = c;
                break;
            }
        }

        final List<Method> classVisitorMethods = Arrays.asList(classVisitorClass.getDeclaredMethods());
        final List<Method> annotatedClassVisitorMethods = Arrays.asList(annotatedClassVisitorClass.getDeclaredMethods());
        boolean containsAllMethods = true;
        for (Method classVisitorMethod : classVisitorMethods) {
            boolean foundClassVisitorMethod = false;
            for (Method annotatedClassVisitorMethod : annotatedClassVisitorMethods) {
                if (annotatedClassVisitorMethod.getName().equals(classVisitorMethod.getName())
                        && annotatedClassVisitorMethod.getReturnType() == classVisitorMethod.getReturnType()
                        && annotatedClassVisitorMethod.getParameterCount() == classVisitorMethod.getParameterCount()) {
                    final Class<?>[] annotatedClassVisitorTypes = annotatedClassVisitorMethod.getParameterTypes();
                    final Class<?>[] classVisitorTypes = classVisitorMethod.getParameterTypes();
                    boolean typesMatch = true;
                    for (int i = 0; i != annotatedClassVisitorTypes.length; i++) {
                        if (annotatedClassVisitorTypes[i] != classVisitorTypes[i]) {
                            typesMatch = false;
                            break;
                        }
                    }
                    if (typesMatch) {
                        foundClassVisitorMethod = true;
                        //System.out.println("found method " + classVisitorMethod.getName());
                        break;
                    }
                }
            }
            if (!foundClassVisitorMethod) {
                containsAllMethods = false;
                System.out.append("Method ")
                        .append(classVisitorMethod.getName())
                        .println(" not implemented by AnnotationAcceptingListener.AnnotatedClassVisitor");
            }
        }
        Assert.assertThat(containsAllMethods, Matchers.is(true));
    }

    @Test
    public void testCorrectOpcodeAsmIsUsedInAnnotationAcceptingListener() {
        final int asmOpcode = getMaxValueOfField("ASM", 6);
        final AnnotationAcceptingListener aal = new AnnotationAcceptingListener();

        String aalOpcode = null;
        try {
            final Field classVisitorField = aal.getClass().getDeclaredField("classVisitor");
            classVisitorField.setAccessible(true);
            final Object classVisitor = classVisitorField.get(aal);
            final Field opcodeField = classVisitor.getClass().getSuperclass().getDeclaredField("api");
            opcodeField.setAccessible(true);
            aalOpcode = String.valueOf(((Integer) opcodeField.get(classVisitor)) >> 16);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
                "You need to set: \nAnnotatedClassVisitor() {\n    super(Opcodes.ASM" + asmOpcode + ");\n}",
                String.valueOf(asmOpcode), aalOpcode
        );
    }

    @Test
    public void testWarningOpcodeInClassReaderWrapperSetCorrectly() {
        final Integer jdkVersion = getMaxValueOfField("V", 13);

        Class<?> classReaderWrapper = null;
        for (Class<?> innerClass : AnnotationAcceptingListener.class.getDeclaredClasses()) {
            if (innerClass.getName().contains("ClassReaderWrapper")) {
                classReaderWrapper = innerClass;
                break;
            }
        }

        Integer warnFieldValue = 0;
        try {
            final Field warnField = classReaderWrapper.getDeclaredField("WARN_VERSION");
            warnField.setAccessible(true);
            warnFieldValue = (Integer) warnField.get(null) - (Opcodes.V1_1 & 0x00FF) + 1;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
                "You need to set ClassReaderWrapper.WARN_VERSION=Opcodes.V" + jdkVersion,
                jdkVersion, warnFieldValue
        );
    }

    @Test
    public void testLoggerInClassReaderWrapper() throws IOException {
        final String warningMsg = "Unsupported class file major version";

        final Integer maxOpcode = getMaxValueOfField("V", 13);
        final byte[] array = new byte[10];
        array[7] = (byte) ((maxOpcode.byteValue() + Opcodes.V1_1) & 0x00FF);

        final ByteArrayOutputStream log = new ByteArrayOutputStream(500);
        final PrintStream saveErr = System.err;

        try {
            System.setErr(new PrintStream(log));
            try {
                new AnnotationAcceptingListener().process("", new ByteArrayInputStream(array));
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                //expected, given array is too small for a class file
            }
        } finally {
            System.setErr(saveErr);
        }

        final String message = new String(log.toByteArray());
        Assert.assertTrue(
                "The WARNING `" + warningMsg + "` has not been printed for a class with byte code version " + array[7],
                message.contains(warningMsg)
        );
    }

    private static int getMaxValueOfField(String fieldPrefix, int initialValue) {
        int value = initialValue;
        do {
            try {
                value++;
                Field field = Opcodes.class.getField(fieldPrefix + value);
            } catch (NoSuchFieldException e) {
                value--;
                break;
            }
        } while (true);
        return value;
    }
}
