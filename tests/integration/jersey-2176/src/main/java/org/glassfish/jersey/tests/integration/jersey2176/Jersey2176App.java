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

package org.glassfish.jersey.tests.integration.jersey2176;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/**
 * JAX-RS application for the JERSEY-2176 reproducer test.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
abstract class Jersey2176App extends ResourceConfig {

    private final boolean setStatusOverSendError;

    public Jersey2176App(boolean setStatusOverSendError) {
        this.setStatusOverSendError = setStatusOverSendError;

        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, setStatusOverSendError);
        register(MyWriterInterceptor.class);
        register(Issue2176ReproducerResource.class);
    }

    public boolean isSetStatusOverSendError() {
        return setStatusOverSendError;
    }
}
