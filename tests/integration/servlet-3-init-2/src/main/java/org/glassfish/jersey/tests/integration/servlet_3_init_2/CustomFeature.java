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

package org.glassfish.jersey.tests.integration.servlet_3_init_2;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.tests.integration.servlet_3_init_2.ext.Ext1WriterInterceptor;
import org.glassfish.jersey.tests.integration.servlet_3_init_2.ext.Ext2WriterInterceptor;
import org.glassfish.jersey.tests.integration.servlet_3_init_2.ext.Ext3WriterInterceptor;
import org.glassfish.jersey.tests.integration.servlet_3_init_2.ext.Ext4WriterInterceptor;

/**
 * @author Michal Gajdos
 */
@Provider
public class CustomFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(Ext3WriterInterceptor.class, 1000);
        context.register(Ext2WriterInterceptor.class, 100);
        context.register(Ext1WriterInterceptor.INSTANCE, 500);
        context.register(Ext4WriterInterceptor.INSTANCE, 1);
        return true;
    }
}
