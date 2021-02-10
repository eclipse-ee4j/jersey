/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

import java.net.URI;
import java.util.concurrent.ThreadFactory;

/**
 * Jersey {@code Container} stub.
 *
 * For JDK 1.8 only since Jetty 11 does not support JDKs below 11
 *
 */
public final class JettyHttpContainerFactory {

    private JettyHttpContainerFactory() {
    }

    public static Server createServer(final URI uri) throws ProcessingException {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8
    }

    public static Server createServer(final URI uri, final boolean start) throws ProcessingException {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8
    }

    public static Server createServer(final URI uri, final ResourceConfig config)
            throws ProcessingException {

        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8
    }

    public static Server createServer(final URI uri, final ResourceConfig configuration, final boolean start)
            throws ProcessingException {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8
    }

    public static Server createServer(final URI uri, final ResourceConfig config, final boolean start,
                                      final Object parentContext) {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8
    }

    public static Server createServer(final URI uri, final ResourceConfig config, final Object parentContext) {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8
    }

    public static Server createServer(final URI uri, final SslContextFactory.Server sslContextFactory,
                                      final ResourceConfig config)
            throws ProcessingException {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8    }
    }

    public static Server createServer(final URI uri,
                                      final SslContextFactory.Server sslContextFactory,
                                      final JettyHttpContainer handler,
                                      final boolean start) {
        if (JdkVersion.getJdkVersion().getMajor() < 11) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK 1.8
    }
}
