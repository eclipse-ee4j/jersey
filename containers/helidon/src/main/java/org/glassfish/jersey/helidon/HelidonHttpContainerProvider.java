/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon;

import io.helidon.webserver.WebServer;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.spi.ContainerProvider;

public class HelidonHttpContainerProvider implements ContainerProvider {
    @Override
    public <T> T createContainer(Class<T> type, Application application) throws ProcessingException {
        if (type != WebServer.class && type != HelidonHttpContainer.class) {
            return null;
        }
        return type.cast(new HelidonHttpContainer(application, new HelidonJerseyBridge()));
    }
}
