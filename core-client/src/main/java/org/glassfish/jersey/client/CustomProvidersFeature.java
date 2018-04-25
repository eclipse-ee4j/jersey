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

package org.glassfish.jersey.client;

import java.util.Collection;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Feature to provide the single-line registration of custom providers.
 *
 * @author Stepan Kopriva
 */
public class CustomProvidersFeature implements Feature {

    private final Collection<Class<?>> providers;

    /**
     * Constructs Feature which is used to register providers as providers in Configuration.
     *
     * @param providers collection of providers which are going to be registered
     */
    public CustomProvidersFeature(Collection<Class<?>> providers) {
        this.providers = providers;
    }

    @Override
    public boolean configure(FeatureContext context) {
        for (Class<?> provider : providers) {
            context.register(provider);
        }
        return true;
    }
}
