/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;

/**
 * Binds {@link org.glassfish.jersey.examples.helloworld.CustomInterceptor} with resource methods that should return modified
 * entity than the one returned from the method.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@NameBinding
public @interface ResponseBodyFromCdiBean {
}
