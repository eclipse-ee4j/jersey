/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.scanning;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import jersey.repackaged.org.objectweb.asm.RecordComponentVisitor;
import org.glassfish.jersey.internal.OsgiRegistry;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.internal.LocalizationMessages;

import jersey.repackaged.org.objectweb.asm.AnnotationVisitor;
import jersey.repackaged.org.objectweb.asm.Attribute;
import jersey.repackaged.org.objectweb.asm.ClassReader;
import jersey.repackaged.org.objectweb.asm.ClassVisitor;
import jersey.repackaged.org.objectweb.asm.FieldVisitor;
import jersey.repackaged.org.objectweb.asm.MethodVisitor;
import jersey.repackaged.org.objectweb.asm.ModuleVisitor;
import jersey.repackaged.org.objectweb.asm.Opcodes;
import jersey.repackaged.org.objectweb.asm.TypePath;

/**
 * A scanner listener that processes Java class files (resource names
 * ending in ".class") annotated with one or more of a set of declared
 * annotations.
 * <p>
 * Java classes of a Java class file are processed, using ASM, to ascertain
 * if those classes are annotated with one or more of the set of declared
 * annotations.
 * <p>
 * Such an annotated Java class of a Java class file is loaded if the class
 * is public or is an inner class that is static and public.
 *
 * @author Paul Sandoz
 */
public final class AnnotationAcceptingListener implements ResourceProcessor {

    private final ClassLoader classloader;

    private final Set<Class<?>> classes;

    private final Set<String> annotations;

    private final AnnotatedClassVisitor classVisitor;

    /**
     * Create a scanning listener to check for Java classes in Java
     * class files annotated with {@link javax.ws.rs.Path} or {@link javax.ws.rs.ext.Provider}.
     *
     * @return new instance of {@link AnnotationAcceptingListener} which looks for
     * {@link javax.ws.rs.Path} or {@link javax.ws.rs.ext.Provider} annotated classes.
     *
     */
    @SuppressWarnings({"unchecked"})
    public static AnnotationAcceptingListener newJaxrsResourceAndProviderListener() {
        return new AnnotationAcceptingListener(Path.class, Provider.class);
    }

    /**
     * Create a scanning listener to check for Java classes in Java
     * class files annotated with {@link Path} or {@link Provider}.
     *
     * @param classLoader the class loader to use to load Java classes that
     *        are annotated with any one of the annotations.
     * @return new instance of {@link AnnotationAcceptingListener} which looks for
     * {@link javax.ws.rs.Path} or {@link javax.ws.rs.ext.Provider} annotated classes.
     */
    @SuppressWarnings({"unchecked"})
    public static AnnotationAcceptingListener newJaxrsResourceAndProviderListener(final ClassLoader classLoader) {
        return new AnnotationAcceptingListener(classLoader, Path.class, Provider.class);
    }

    /**
     * Create a scanner listener to check for annotated Java classes in Java
     * class files.
     *
     * @param annotations the set of annotation classes to check on Java class
     *        files.
     */
    public AnnotationAcceptingListener(final Class<? extends Annotation>... annotations) {
        this(AccessController.doPrivileged(ReflectionHelper.getContextClassLoaderPA()), annotations);
    }

    /**
     * Create a scanner listener to check for annotated Java classes in Java
     * class files.
     *
     * @param classloader the class loader to use to load Java classes that
     *        are annotated with any one of the annotations.
     * @param annotations the set of annotation classes to check on Java class
     *        files.
     */
    public AnnotationAcceptingListener(final ClassLoader classloader,
                                       final Class<? extends Annotation>... annotations) {
        this.classloader = classloader;
        this.classes = new LinkedHashSet<Class<?>>();
        this.annotations = getAnnotationSet(annotations);
        this.classVisitor = new AnnotatedClassVisitor();
    }

    /**
     * Get the set of annotated classes.
     *
     * @return the set of annotated classes.
     */
    public Set<Class<?>> getAnnotatedClasses() {
        return classes;
    }

    private Set<String> getAnnotationSet(final Class<? extends Annotation>... annotations) {
        final Set<String> a = new HashSet<String>();
        for (final Class c : annotations) {
            a.add("L" + c.getName().replaceAll("\\.", "/") + ";");
        }
        return a;
    }

    // ScannerListener
    public boolean accept(final String name) {
        return !(name == null || name.isEmpty()) && name.endsWith(".class");

    }

    public void process(final String name, final InputStream in) throws IOException {
        new ClassReaderWrapper(in).accept(classVisitor, 0);
    }

    //

    private final class AnnotatedClassVisitor extends ClassVisitor {

        /**
         * The name of the visited class.
         */
        private String className;
        /**
         * True if the class has the correct scope
         */
        private boolean isScoped;
        /**
         * True if the class has the correct declared annotations
         */
        private boolean isAnnotated;

        private AnnotatedClassVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(final int version, final int access, final String name,
                          final String signature, final String superName, final String[] interfaces) {
            className = name;
            isScoped = (access & Opcodes.ACC_PUBLIC) != 0;
            isAnnotated = false;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            isAnnotated |= annotations.contains(desc);
            return null;
        }

        @Override
        public void visitInnerClass(final String name, final String outerName,
                                    final String innerName, final int access) {
            // If the name of the class that was visited is equal
            // to the name of this visited inner class then
            // this access field needs to be used for checking the scope
            // of the inner class
            if (className.equals(name)) {
                isScoped = (access & Opcodes.ACC_PUBLIC) != 0;

                // Inner classes need to be statically scoped
                isScoped &= (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
            }
        }

        @Override
        public void visitEnd() {
            if (isScoped && isAnnotated) {
                // Correctly scoped and annotated
                // add to the set of matching classes.
                classes.add(getClassForName(className.replaceAll("/", ".")));
            }
        }

        @Override
        public void visitOuterClass(final String string, final String string0,
                                    final String string1) {
            // Do nothing
        }

        @Override
        public FieldVisitor visitField(final int i, final String string,
                                       final String string0, final String string1,
                                       final Object object) {
            // Do nothing
            return null;
        }

        @Override
        public void visitSource(final String string, final String string0) {
            // Do nothing
        }

        @Override
        public void visitAttribute(final Attribute attribute) {
            // Do nothing
        }

        @Override
        public MethodVisitor visitMethod(final int i, final String string,
                                         final String string0, final String string1,
                                         final String[] string2) {
            // Do nothing
            return null;
        }

        @Override
        public ModuleVisitor visitModule(final String name, final int access, final String version) {
            // Do nothing
            return null;
        }

        @Override
        public void visitNestHost(final String nestHost) {
            // do nothing
        }

        @Override
        public void visitNestMember(final String nestMember) {
            // do nothing
        }

        @Override
        public void visitPermittedSubclass(final String permittedSubclass) {
            // do nothing
        }

        @Override
        public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
            // do nothing
            return null;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
                final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
            //do nothing
            return null;
        }

        private Class getClassForName(final String className) {
            try {
                final OsgiRegistry osgiRegistry = ReflectionHelper.getOsgiRegistryInstance();

                if (osgiRegistry != null) {
                    return osgiRegistry.classForNameWithException(className);
                } else {
                    return AccessController.doPrivileged(ReflectionHelper.classForNameWithExceptionPEA(className, classloader));
                }
            } catch (final ClassNotFoundException ex) {
                throw new RuntimeException(LocalizationMessages.ERROR_SCANNING_CLASS_NOT_FOUND(className), ex);
            } catch (final PrivilegedActionException pae) {
                final Throwable cause = pae.getCause();
                if (cause instanceof ClassNotFoundException) {
                    throw new RuntimeException(LocalizationMessages.ERROR_SCANNING_CLASS_NOT_FOUND(className), cause);
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        }

    }

    private static class ClassReaderWrapper {
        private static final Logger LOGGER = Logger.getLogger(ClassReader.class.getName());
        private static final int WARN_VERSION = Opcodes.V17;
        private static final int INPUT_STREAM_DATA_CHUNK_SIZE = 4096;

        private final byte[] b;
        private ClassReaderWrapper(InputStream inputStream) throws IOException {
            this.b = readStream(inputStream);
        }

        private void accept(final ClassVisitor classVisitor, final int parsingOptions) {
            final int originalVersion = getMajorVersion(b);
            if (originalVersion == WARN_VERSION + 1) {
                // temporarily downgrade version to bypass check in ASM
                setMajorVersion(WARN_VERSION, b);
                LOGGER.warning("Unsupported class file major version " + originalVersion);
            }
            final ClassReader classReader = new ClassReader(b);
            setMajorVersion(originalVersion, b);
            classReader.accept(classVisitor, parsingOptions);
        }

        /**
         * Sets major version number in given bytes of class (unsigned two bytes at
         * offset 6).
         *
         * @param majorVersion
         *            major version of bytecode to set
         * @param b
         *            bytes of class
         * @see #getMajorVersion(byte[])
         */
        private static void setMajorVersion(final int majorVersion, final byte[] b) {
            b[6] = (byte) (majorVersion >>> 8);
            b[7] = (byte) majorVersion;
        }

        /**
         * Gets major version number from given bytes of class (unsigned two bytes
         * at offset 6).
         *
         * @param b
         *            bytes of class
         * @return major version of bytecode
         * @see <a href=
         *      "https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.1">JVMS 4.1 - The class File Format</a>
         */
        private static int getMajorVersion(final byte[] b) {
            return ((b[6] & 0xFF) << 8) | (b[7] & 0xFF);
        }

        /**
         * Reads the given input stream and returns its content as a byte array.
         *
         * @param inputStream an input stream.
         * @return the content of the given input stream.
         * @throws IOException if a problem occurs during reading.
         */
        private static byte[] readStream(final InputStream inputStream) throws IOException {
            if (inputStream == null) {
                throw new IOException("Class not found");
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] data = new byte[INPUT_STREAM_DATA_CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, bytesRead);
                }
                outputStream.flush();
                return outputStream.toByteArray();
            } finally {
                inputStream.close();
            }
        }
    }
}
