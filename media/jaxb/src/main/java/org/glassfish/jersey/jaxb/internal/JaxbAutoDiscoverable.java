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

package org.glassfish.jersey.jaxb.internal;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;

/**
 * JAXB {@link ForcedAutoDiscoverable} that registers all necessary JAXB features
 * into the injection manager directly.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public final class JaxbAutoDiscoverable implements ForcedAutoDiscoverable {

    @Override
    public void configure(final FeatureContext context) {
        context.register(new JaxbMessagingBinder());

        if (RuntimeType.SERVER == context.getConfiguration().getRuntimeType()) {
            context.register(new JaxbParamConverterBinder());
        }
    }
}
