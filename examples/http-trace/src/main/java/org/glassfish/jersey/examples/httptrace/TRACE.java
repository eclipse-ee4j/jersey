/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httptrace;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.HttpMethod;

/**
 * HTTP TRACE method annotation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@HttpMethod(TRACE.NAME)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TRACE {
    public static final String NAME = "TRACE";
}
