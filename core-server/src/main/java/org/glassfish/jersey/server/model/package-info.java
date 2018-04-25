/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Jersey server-side application & resource modeling classes.
 * <p/>
 * The classes from this package provide means to model and build Jersey applications
 * based on both declarative and programmatic approach. An application could be
 * built based on a set of JAX-RS annotated classes (standard, declarative way)
 * or using Jersey specific programmatic API, where you are not constrained
 * to Java reflection API and can freely bind Java code to serve a HTTP method
 * for a given URI. Both methods could be combined, so that you can e.g. dynamically
 * add a new resource method to an existing JAX-RS resource class.
 */
package org.glassfish.jersey.server.model;
