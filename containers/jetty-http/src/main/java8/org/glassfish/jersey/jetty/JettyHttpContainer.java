/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

/**
 * Jersey {@code Container} stub based on Jetty {@link org.eclipse.jetty.server.Handler}.
 *
 * For JDK 1.8 only since Jetty 11 does not support JDKs below 11
 *
 */
public final class JettyHttpContainer implements Container {

    public JettyHttpContainer(Application application) {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public ResourceConfig getConfiguration() {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public void reload() {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    @Override
    public void reload(ResourceConfig configuration) {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

}
