/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.inject;

import java.lang.annotation.Annotation;

/**
 * This class allows users to provide a custom injection target for any annotation (including {@code &#64;Inject}). The user would
 * usually only provide a resolver for {@code &#64;Inject} if it were specializing the system provided resolver for
 * {@code &#64;Inject}. Otherwise, this resolver can be used to provide injection points for any annotation.
 * <p>
 * Jersey provides all {@code InjectionResolvers} for JAX-RS annotation and {@code org.glassfish.jersey.server.Uri} apart from
 * {@link javax.ws.rs.core.Context} which must be implemented and registered directly as a part of DI integration because of
 * many optimization which cannot be implemented on Jersey side.
 * <p>
 * The {@code InjectionResolvers} are delivered to DI integration using {@link InjectionManager#register(Binder)} and DI provider
 * just filter {@link InjectionResolverBinding} and internally register the annotation handling using its own mechanism.
 *
 * @param <T> This must be the annotation class of the injection annotation that this resolver will handle.
 */
public interface InjectionResolver<T extends Annotation> {

    /**
     * This method will return the object that should be injected into the given injection point.  It is the responsibility of the
     * implementation to ensure that the object returned can be safely injected into the injection point.
     * <p>
     * This method should not do the injection themselves.
     *
     * @param injectee The injection point this value is being injected into
     * @return A possibly null value to be injected into the given injection point
     */
    Object resolve(Injectee injectee);

    /**
     * This method should return true if the annotation that indicates that this is an injection point can appear in the parameter
     * list of a constructor.
     *
     * @return true if the injection annotation can appear in the parameter list of a constructor.
     */
    boolean isConstructorParameterIndicator();

    /**
     * This method should return true if the annotation that indicates that this is an injection point can appear in the parameter
     * list of a method.
     *
     * @return true if the injection annotation can appear in the parameter list of a method.
     */
    boolean isMethodParameterIndicator();

    /**
     * This method returns the annotation for what the injection resolver is implemented.
     *
     * @return handled annotation by injection resolver.
     */
    Class<T> getAnnotation();

}
