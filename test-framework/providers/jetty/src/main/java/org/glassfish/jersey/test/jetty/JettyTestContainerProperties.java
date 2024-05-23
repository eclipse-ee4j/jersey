/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.jetty;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Properties which relates only to Jetty test container configuration
 *
 * @since 2.44
 */
@PropertiesClass
public class JettyTestContainerProperties {

    /**
     * Parameter which allows settings custom header size for request and response.
     *
     * @since 2.44
     */
    public static final String HEADER_SIZE = "jersey.test.jetty.container.header.size";

}
