/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.exceptions;

/**
 * This exceptions will get mapped to a 404 response with the application exception mapper
 * implemented by {@link NotFoundExceptionMapper} class.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class CustomNotFoundException extends Exception {

}
