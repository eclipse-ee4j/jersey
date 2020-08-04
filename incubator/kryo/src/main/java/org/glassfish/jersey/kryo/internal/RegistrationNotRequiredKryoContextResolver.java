/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.kryo.internal;

import com.esotericsoftware.kryo.Kryo;

import javax.ws.rs.ext.ContextResolver;

/**
 * Backwards compatibility ContextResolver.
 * It should only be used when the user specifically agrees on a vulnerability provided when the
 * <code>KryoFeature#registrationRequired(false)</code> is used.
 * The default behaviour is demanded to require {@code ContextResolver} with
 * <pre>
 * public Kryo getContext(Class<?> type) {
 *      ...
 *      Kryo kryo = new Kryo();
 *      kryo.setRegistrationRequired(true);
 *      kryo.register(The_class_for_which_the_KryoMessageBodyProvider_should_be_allowed);
 *      ...
 *      return kryo;
 * }
 * </pre>
 */
public class RegistrationNotRequiredKryoContextResolver implements ContextResolver<Kryo> {
    @Override
    public Kryo getContext(Class<?> type) {
        return new Kryo();
    }
}
