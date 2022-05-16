/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.AbstractObjectProvider;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityGraphProvider;
import org.glassfish.jersey.message.filtering.spi.EntityInspector;
import org.glassfish.jersey.message.filtering.spi.EntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityProcessorContext;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;
import org.glassfish.jersey.message.filtering.spi.ObjectGraphTransformer;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;
import org.glassfish.jersey.message.filtering.spi.ScopeProvider;
import org.glassfish.jersey.message.filtering.spi.ScopeResolver;

module org.glassfish.jersey.ext.entity.filtering {
    requires java.logging;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.message.filtering;
    exports org.glassfish.jersey.message.filtering.spi;

    uses AbstractEntityProcessor;
    uses AbstractObjectProvider;
    uses EntityGraph;
    uses EntityGraphProvider;
    uses EntityInspector;
    uses EntityProcessor;
    uses EntityProcessorContext;
    uses ObjectGraph;
    uses ObjectGraphTransformer;
    uses ObjectProvider;
    uses ScopeProvider;
    uses ScopeResolver;

}