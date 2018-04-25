/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Encoded;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;

/**
 * Resource method handler model.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class MethodHandler implements ResourceModelComponent {

    private final Collection<Parameter> handlerParameters;

    /**
     * Create new instance of a resource method handler model.
     */
    protected MethodHandler() {
        this.handlerParameters = Collections.emptyList();
    }

    /**
     * Create new instance of a resource method handler model.
     *
     * @param parameters handler parameters associated directly with the resource method handler
     *                   (e.g. class-level property setters and fields). May be {@code null}.
     * @since 2.20
     */
    protected MethodHandler(final Collection<Parameter> parameters) {

        if (parameters != null) {
            this.handlerParameters = Collections.unmodifiableCollection(new ArrayList<>(parameters));
        } else {
            this.handlerParameters = Collections.emptyList();
        }

    }

    /**
     * Create a class-based method handler from a class.
     *
     * @param handlerClass method handler class.
     * @return new class-based method handler.
     */
    public static MethodHandler create(final Class<?> handlerClass) {
        return new ClassBasedMethodHandler(handlerClass, null);
    }

    /**
     * Create a class-based method handler from a class.
     *
     * @param handlerClass                 method handler class.
     * @param keepConstructorParamsEncoded if set to {@code true}, any injected constructor
     *                                     parameters must be kept encoded and must not be automatically decoded.
     * @return new class-based method handler.
     */
    public static MethodHandler create(final Class<?> handlerClass, final boolean keepConstructorParamsEncoded) {
        return new ClassBasedMethodHandler(handlerClass, keepConstructorParamsEncoded, null);
    }

    /**
     * Create a instance-based (singleton) method handler from a class.
     *
     * @param handlerInstance method handler instance (singleton).
     * @return new instance-based method handler.
     */
    public static MethodHandler create(final Object handlerInstance) {
        return new InstanceBasedMethodHandler(handlerInstance, null);
    }

    /**
     * Create a instance-based (singleton) method handler from a class.
     *
     * @param handlerInstance method handler instance (singleton).
     * @param handlerClass    declared handler class.
     * @return new instance-based method handler.
     */
    public static MethodHandler create(final Object handlerInstance, Class<?> handlerClass) {
        return new InstanceBasedMethodHandler(handlerInstance, handlerClass, null);
    }

    /**
     * Create a class-based method handler from a class.
     *
     * @param handlerClass      method handler class.
     * @param handlerParameters method handler parameters (e.g. class-level property setters and fields).
     * @return new class-based method handler.
     */
    public static MethodHandler create(final Class<?> handlerClass, final Collection<Parameter> handlerParameters) {
        return new ClassBasedMethodHandler(handlerClass, handlerParameters);
    }

    /**
     * Create a class-based method handler from a class.
     *
     * @param handlerClass                 method handler class.
     * @param keepConstructorParamsEncoded if set to {@code true}, any injected constructor
     *                                     parameters must be kept encoded and must not be automatically decoded.
     * @param handlerParameters            method handler parameters (e.g. class-level property setters and fields).
     * @return new class-based method handler.
     * @since 2.20
     */
    public static MethodHandler create(final Class<?> handlerClass,
                                       final boolean keepConstructorParamsEncoded,
                                       final Collection<Parameter> handlerParameters) {
        return new ClassBasedMethodHandler(handlerClass, keepConstructorParamsEncoded, handlerParameters);
    }

    /**
     * Create a instance-based (singleton) method handler from a class.
     *
     * @param handlerInstance   method handler instance (singleton).
     * @param handlerParameters method handler parameters (e.g. class-level property setters and fields).
     * @return new instance-based method handler.
     * @since 2.20
     */
    public static MethodHandler create(final Object handlerInstance, final Collection<Parameter> handlerParameters) {
        return new InstanceBasedMethodHandler(handlerInstance, handlerParameters);
    }

    /**
     * Create a instance-based (singleton) method handler from a class.
     *
     * @param handlerInstance   method handler instance (singleton).
     * @param handlerClass      declared handler class.
     * @param handlerParameters method handler parameters (e.g. class-level property setters and fields).
     * @return new instance-based method handler.
     * @since 2.20
     */
    public static MethodHandler create(final Object handlerInstance,
                                       Class<?> handlerClass,
                                       final Collection<Parameter> handlerParameters) {
        return new InstanceBasedMethodHandler(handlerInstance, handlerClass, handlerParameters);
    }

    /**
     * Get the resource method handler class.
     *
     * @return resource method handler class.
     */
    public abstract Class<?> getHandlerClass();

    /**
     * Get the resource method handler constructors.
     * <p/>
     * The returned is empty by default. Concrete implementations may override
     * the method to return the actual list of constructors that will be used
     * for the handler initialization.
     *
     * @return resource method handler constructors.
     */
    public List<HandlerConstructor> getConstructors() {
        return Collections.emptyList();
    }

    /**
     * Get the injected resource method handler instance.
     *
     * @param injectionManager injection manager that can be used to inject get the instance.
     * @return injected resource method handler instance.
     */
    public abstract Object getInstance(final InjectionManager injectionManager);

    /**
     * Return whether the method handler {@link InjectionManager creates instances}
     * based on {@link Class classes}.
     *
     * @return True is instances returned by this method handler are created from {@link Class classes} given to
     * {@code InjectionManager}, false otherwise (for example when method handler was initialized from instance)
     */
    public abstract boolean isClassBased();

    /**
     * Get the parameters associated directly with the resource method handler, if any
     * (e.g. class-level property setters and fields).
     * <p>
     * Note that this method does not return any parameters associated with
     * {@link #getConstructors() method handler constructors}.
     * </p>
     *
     * @return parameters associated with the resource method handler. May return an empty collection
     * but does not return {@code null}.
     * @since 2.20
     */
    public Collection<Parameter> getParameters() {
        return handlerParameters;
    }

    @Override
    public List<? extends ResourceModelComponent> getComponents() {
        return null;
    }

    @Override
    public void accept(final ResourceModelVisitor visitor) {
        visitor.visitMethodHandler(this);
    }

    private static class ClassBasedMethodHandler extends MethodHandler {

        private final Class<?> handlerClass;
        private final List<HandlerConstructor> handlerConstructors;

        public ClassBasedMethodHandler(final Class<?> handlerClass, final Collection<Parameter> handlerParameters) {
            this(handlerClass, handlerClass.isAnnotationPresent(Encoded.class), handlerParameters);
        }

        public ClassBasedMethodHandler(final Class<?> handlerClass,
                                       final boolean disableParamDecoding,
                                       final Collection<Parameter> handlerParameters) {
            super(handlerParameters);

            this.handlerClass = handlerClass;

            List<HandlerConstructor> constructors = new LinkedList<HandlerConstructor>();
            for (Constructor<?> constructor : handlerClass.getConstructors()) {
                constructors.add(new HandlerConstructor(
                        constructor, Parameter.create(handlerClass, handlerClass, constructor, disableParamDecoding)));
            }
            this.handlerConstructors = Collections.unmodifiableList(constructors);
        }

        @Override
        public Class<?> getHandlerClass() {
            return handlerClass;
        }

        @Override
        public List<HandlerConstructor> getConstructors() {
            return handlerConstructors;
        }

        @Override
        public Object getInstance(final InjectionManager injectionManager) {
            return Injections.getOrCreate(injectionManager, handlerClass);
        }

        @Override
        public boolean isClassBased() {
            return true;
        }

        @Override
        protected Object getHandlerInstance() {
            return null;
        }

        @Override
        public List<? extends ResourceModelComponent> getComponents() {
            return handlerConstructors;
        }

        @Override
        public String toString() {
            return "ClassBasedMethodHandler{"
                    + "handlerClass=" + handlerClass
                    + ", handlerConstructors=" + handlerConstructors + '}';
        }
    }

    private static class InstanceBasedMethodHandler extends MethodHandler {

        private final Object handler;
        private final Class<?> handlerClass;

        public InstanceBasedMethodHandler(final Object handler, final Collection<Parameter> handlerParameters) {
            super(handlerParameters);

            this.handler = handler;
            this.handlerClass = handler.getClass();
        }

        public InstanceBasedMethodHandler(final Object handler,
                                          final Class<?> handlerClass,
                                          final Collection<Parameter> handlerParameters) {
            super(handlerParameters);

            this.handler = handler;
            this.handlerClass = handlerClass;
        }

        @Override
        public Class<?> getHandlerClass() {
            return handlerClass;
        }

        @Override
        protected Object getHandlerInstance() {
            return handler;
        }

        @Override
        public Object getInstance(final InjectionManager injectionManager) {
            return handler;
        }

        @Override
        public boolean isClassBased() {
            return false;
        }

        @Override
        public String toString() {
            return "InstanceBasedMethodHandler{"
                    + "handler=" + handler
                    + ", handlerClass=" + handlerClass
                    + '}';
        }
    }

    /**
     * Get the raw handler instance that is backing this method handler.
     *
     * @return raw handler instance. May return {@code null} if the handler is
     * {@link #isClassBased() class-based}.
     */
    protected abstract Object getHandlerInstance();
}
