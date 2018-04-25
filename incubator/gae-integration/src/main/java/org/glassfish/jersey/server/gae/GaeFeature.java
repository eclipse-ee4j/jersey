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

package org.glassfish.jersey.server.gae;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Google App Engine integration for Jersey server runtime.
 * <p>
 * Register this feature in your {@link org.glassfish.jersey.server.ResourceConfig} subclass in order to be able to run
 * your Jersey application on Google App Engine.
 * </p>
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@ConstrainedTo(RuntimeType.SERVER)
public final class GaeFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(GaeBackgroundExecutorProvider.class)) {
            context.register(new GaeBackgroundExecutorProvider());
        }
        return true;
    }

}
