/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty11.http2;

import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.jetty11.Jetty11HttpContainer;
import org.glassfish.jersey.jetty11.Jetty11HttpContainerProvider;
import org.glassfish.jersey.jetty11.internal.LocalizationMessages;
import org.glassfish.jersey.server.spi.ContainerProvider;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Application;

import static org.glassfish.jersey.jetty11.Jetty11HttpContainerProvider.HANDLER_NAME;

public final class Jetty11Http2ContainerProvider implements ContainerProvider {

    @Override
    public <T> T createContainer(final Class<T> type, final Application application) throws ProcessingException {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        if (type != null && (HANDLER_NAME.equalsIgnoreCase(type.getCanonicalName()) || Jetty11HttpContainer.class == type)) {
            return type.cast(new Jetty11HttpContainerProvider().createContainer(Jetty11HttpContainer.class, application));
        }
        return null;
    }
}

