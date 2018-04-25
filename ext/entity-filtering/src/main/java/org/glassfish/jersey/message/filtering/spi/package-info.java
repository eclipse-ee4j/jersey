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
 * SPI for Entity Data Filtering in Jersey.
 * <p/>
 * To create a custom entity-filtering annotation with special handling (e.g. field aggregator annotation used to annotate
 * classes), refer to:
 * <ul>
 * <li>{@link org.glassfish.jersey.message.filtering.spi.EntityProcessor}</li>
 * <li>{@link org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor}</li>
 * <li>{@link org.glassfish.jersey.message.filtering.spi.ScopeResolver}</li>
 * </ul>
 * <p/>
 * To support Entity Data Filtering in custom providers (e.g. message body workers), refer to:
 * <ul>
 * <li>{@link org.glassfish.jersey.message.filtering.spi.ObjectProvider}</li>
 * <li>{@link org.glassfish.jersey.message.filtering.spi.ObjectGraphTransformer}</li>
 * <li>{@link org.glassfish.jersey.message.filtering.spi.AbstractObjectProvider}</li>
 * </ul>
 * <p/>
 *
 * @since 2.3
 */
package org.glassfish.jersey.message.filtering.spi;
