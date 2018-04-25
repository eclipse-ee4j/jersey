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

package org.glassfish.jersey.server.monitoring;

/**
 * MXBean interface of resource method MXBeans.
 *
 * @author Miroslav Fuksa
 */
public interface ResourceMethodMXBean {

    /**
     * Get the name of the Java method.
     *
     * @return Name of method.
     */
    public String getMethodName();

    /**
     * Get the sub resource method path of the method. This field is non-null only for
     * sub resource methods and contains path relative to resource in which the method is defined.
     *
     * @return Sub resource method path or null if the method is not a sub resource method.
     */
    public String getPath();

    /**
     * Get the HTTP method of the method.
     *
     * @return HTTP method (e.g. GET, POST, ...)
     */
    public String getHttpMethod();

    /**
     * Get the full class name of the class that declares the handling method.
     *
     * @return Full class name.
     */
    public String getDeclaringClassName();

    /**
     * Get the string with media types consumed by this method, enclosed in double quotas and
     * separated by a comma (e.g. "text/plain","text/html").
     *
     * @return Consumed media types.
     */
    public String getConsumesMediaType();

    /**
     * Get the string with media types produced by this method, enclosed in double quotas and
     * separated by a comma (e.g. "text/plain","text/html").
     *
     * @return Produced media types.
     */
    public String getProducesMediaType();

}
