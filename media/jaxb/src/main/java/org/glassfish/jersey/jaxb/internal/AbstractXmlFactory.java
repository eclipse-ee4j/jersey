/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jaxb.internal;

import java.util.function.Supplier;

import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.MessageProperties;

/**
 * Gathers common functionality for {@link Supplier} instances handling XML data.
 *
 * @author Michal Gajdos
 */
abstract class AbstractXmlFactory<T> implements Supplier<T> {

    private final Configuration config;

    protected AbstractXmlFactory(final Configuration config) {
        this.config = config;
    }

    /**
     * Determines whether the {@value MessageProperties#XML_SECURITY_DISABLE} property is disabled for this factory.
     *
     * @return {@code true} if the xml security is disabled for this factory, {@code false} otherwise.
     */
    boolean isXmlSecurityDisabled() {
        return PropertiesHelper.isProperty(config.getProperty(MessageProperties.XML_SECURITY_DISABLE));
    }
}
