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

/**
 * Support for Entity Data Filtering in Jersey.
 * <p/>
 * To use Entity Data Filtering one of the provided {@link javax.ws.rs.core.Feature features} has to be registered in an
 * application:
 * <ul>
 *     <li>{@link org.glassfish.jersey.message.filtering.EntityFilteringFeature} - adds support for entity-filtering
 *     annotations based on {@link org.glassfish.jersey.message.filtering.EntityFiltering} meta-annotation.</li>
 *     <li>{@link org.glassfish.jersey.message.filtering.SecurityEntityFilteringFeature} - add support for entity-filtering using
 *     Java Security annotations (<code>javax.annotation.security</code>).</li>
 * </ul>
 * <p/>
 * To define own entity-filtering annotations, refer to the {@link org.glassfish.jersey.message.filtering.EntityFiltering}
 * meta-annotation.
 *
 * @since 2.3
 */
package org.glassfish.jersey.message.filtering;
