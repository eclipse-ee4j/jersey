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

package org.glassfish.jersey.test.jetty;

import jakarta.ws.rs.ProcessingException;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import java.net.URI;

/**
 * Jetty test factory stub for JDK 1.8 only
 *
 * since Jetty 11+ does not support JDKs below 11
 */
public class JettyTestContainerFactory implements TestContainerFactory {

    @Override
    public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }
}
