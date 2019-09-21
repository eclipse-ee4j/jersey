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

package org.glassfish.jersey.oauth1.signature;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;

/**
 * Feature enabling OAuth signature support. If the feature is registered the
 * {@link OAuth1Signature} can be injected to JAX-RS resources and providers
 * and used to sign and verify OAuth requests.
 *
 * @author Miroslav Fuksa
 */
public class OAuth1SignatureFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(Binder.class)) {
            context.register(new Binder());
        }
        return true;
    }

    /**
     * Binder that binds {@link OAuth1SignatureMethod signature methods}
     * and {@link OAuth1Signature}.
     * <p>
     * The class is package private for testing purposes.
     * <p/>
     *
     */
    static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(PlaintextMethod.class).to(OAuth1SignatureMethod.class).in(Singleton.class);
            bind(RsaSha1Method.class).to(OAuth1SignatureMethod.class).in(Singleton.class);
            bind(HmaSha1Method.class).to(OAuth1SignatureMethod.class).in(Singleton.class);
            bindAsContract(OAuth1Signature.class).in(Singleton.class);
        }
    }
}
