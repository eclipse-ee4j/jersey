/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate JAX-RS resources and resource methods to provide reference to a template for MVC support.
 * <p/>
 * In case a resource class is annotated with {@link Template} annotation then an instance of this class is considered to be
 * the model. Producible {@link javax.ws.rs.core.MediaType media types} are determined from the resource classes
 * {@link javax.ws.rs.Produces} annotation.
 * <p/>
 * In case a resource method is annotated with {@link Template} annotation then the return value of the method is the model.
 * Otherwise the processing of such a method is the same as if the  return type of the method was {@link Viewable} class.
 * Producible {@link javax.ws.rs.core.MediaType media types} are determined from the method's {@link javax.ws.rs.Produces}
 * annotation.
 * <p/>
 * To see how templates are being resolved, see {@link Viewable viewable}.
 *
 * @author Michal Gajdos
 * @see Viewable
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Template {

    /**
     * The template name that should be used to output the entity. The template name may be declared as absolute template name
     * if the name begins with a '/', otherwise the template name is declared as a relative template name.
     */
    String name() default "";
}
