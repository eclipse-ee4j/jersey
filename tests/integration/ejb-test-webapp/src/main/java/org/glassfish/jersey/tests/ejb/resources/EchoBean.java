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

package org.glassfish.jersey.tests.ejb.resources;

import javax.ejb.Stateful;

/**
 * Session bean capable of returning an echoed message back.
 * This is to prove EJB container is used in {@link EchoResource}
 * and {@link RawEchoResource} resources.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Stateful
public class EchoBean {

    /**
     * Prefix, {@value}, to be attached to each message processed by this bean.
     */
    public static final String PREFIX = "ECHOED: ";

    /**
     * Echo message.
     *
     * @param message to be echoed.
     * @return incoming message prefixed with {@link #PREFIX}.
     */
    public String echo(final String message) {
        return String.format("%s%s", PREFIX, message);
    }
}
