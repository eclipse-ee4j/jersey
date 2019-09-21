/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import javax.inject.Singleton;

import org.glassfish.jersey.Beta;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.linking.contributing.NaiveResourceLinkContributionContext;
import org.glassfish.jersey.linking.contributing.ResourceLinkContributionContext;
import org.glassfish.jersey.linking.mapping.NaiveResourceMappingContext;
import org.glassfish.jersey.linking.mapping.ResourceMappingContext;

/**
 * A feature to enable the declarative linking functionality.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
@Beta
public class DeclarativeLinkingFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {

        Configuration config = context.getConfiguration();
        if (!config.isRegistered(ResponseLinkFilter.class)) {
            context.register(new AbstractBinder() {

                @Override
                protected void configure() {
                    bindAsContract(NaiveResourceMappingContext.class)
                            .to(ResourceMappingContext.class).in(Singleton.class);
                }
            });
            context.register(new AbstractBinder() {

                @Override
                protected void configure() {
                    bindAsContract(NaiveResourceLinkContributionContext.class)
                            .to(ResourceLinkContributionContext.class).in(Singleton.class);
                }
            });

            context.register(ResponseLinkFilter.class);

            // TODO: map values back?
            // context.register(RequestLinkFilter.class);
            return true;
        }
        return false;
    }
}
