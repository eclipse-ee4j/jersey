/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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

module org.glassfish.jersey.media.multipart {

    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.inject;

    requires org.jvnet.mimepull;

    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.media.multipart;
    exports org.glassfish.jersey.media.multipart.file;
    exports org.glassfish.jersey.media.multipart.internal;
    exports org.glassfish.jersey.media.multipart.internal.l10n;

    opens org.glassfish.jersey.media.multipart;
    opens org.glassfish.jersey.media.multipart.file;
    opens org.glassfish.jersey.media.multipart.internal;
    opens org.glassfish.jersey.media.multipart.internal.l10n;

    provides org.glassfish.jersey.innate.spi.EntityPartBuilderProvider with
            org.glassfish.jersey.media.multipart.JerseyEntityPartBuilderProvider;
    provides org.glassfish.jersey.internal.spi.AutoDiscoverable with
            org.glassfish.jersey.media.multipart.MultiPartFeatureAutodiscoverable;
}