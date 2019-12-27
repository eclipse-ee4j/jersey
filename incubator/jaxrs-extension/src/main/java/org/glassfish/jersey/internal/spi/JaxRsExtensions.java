/*
 * Copyright (c) 2019 Markus KARG. All rights reserved.
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

package org.glassfish.jersey.internal.spi;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Extension;

import org.glassfish.jersey.internal.ServiceFinder;

/**
 * This component enables support for {@link Extension JAX-RS extensions}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public final class JaxRsExtensions implements ForcedAutoDiscoverable {

    @Override
    public final void configure(final FeatureContext context) {
        for (final Class<?> serviceClass : ServiceFinder.find(Extension.class, true).toClassArray()) {
            if (!context.getConfiguration().isRegistered(serviceClass)) {
                context.register(serviceClass);
            }
        }
    }

}
