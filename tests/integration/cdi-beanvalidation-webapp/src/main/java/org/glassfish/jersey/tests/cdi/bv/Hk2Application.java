/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.bv;

import javax.ws.rs.ApplicationPath;

import javax.enterprise.inject.Vetoed;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS application to configure resources.
 * This one will get fully managed by HK2.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@ApplicationPath("/hk2")
@Vetoed
public class Hk2Application extends ResourceConfig {

    public Hk2Application() {
        super(Hk2ParamInjectedResource.class,
                Hk2FieldInjectedResource.class,
                Hk2PropertyInjectedResource.class,
                Hk2OldFashionedResource.class);

        register(new Hk2ValidationInterceptor.Binder());
        register(new AbstractBinder(){

            @Override
            protected void configure() {
                bindAsContract(Hk2ValidationResult.class).to(ValidationResult.class).in(RequestScoped.class);
            }
        });
    }

}
