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

package org.glassfish.jersey.server.internal;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Jersey internal server-side configuration properties.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@PropertiesClass
public final class InternalServerProperties {


    /**
     * Used internally for storing {@link javax.ws.rs.core.Form} instance with original (not url-decoded) values in
     * {@link org.glassfish.jersey.server.ContainerRequest} properties.
     */
    public static final String FORM_PROPERTY = "jersey.config.server.representation.form";

    /**
     * Used internally for storing {@link javax.ws.rs.core.Form} instance with url-decoded values in
     * {@link org.glassfish.jersey.server.ContainerRequest} properties.
     */
    public static final String FORM_DECODED_PROPERTY = "jersey.config.server.representation.decoded.form";

    private InternalServerProperties() {
        // prevents instantiation
    }
}
