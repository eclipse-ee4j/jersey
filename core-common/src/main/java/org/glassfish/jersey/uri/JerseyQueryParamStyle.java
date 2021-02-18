/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.uri;

/**
 * JerseyQueryParamStyle is used to specify the desired format of query param
 * when multiple values are sent for the same parameter.
 */
public enum JerseyQueryParamStyle {

    /**
     * Multiple parameter instances, e.g.:
     * <code>foo=v1&amp;foo=v2&amp;foo=v3</code>
     *
     * This is the default query format.
     */
    MULTI_PAIRS,

    /**
     * A single parameter instance with multiple, comma-separated values, e.g.:
     * <code>foo=v1,v2,v3</code>
     */
    COMMA_SEPARATED,

    /**
     * Multiple parameter instances with square brackets for each parameter, e.g.:
     * <code>foo[]=v1&amp;foo[]=v2&amp;foo[]=v3</code>
     */
    ARRAY_PAIRS
}