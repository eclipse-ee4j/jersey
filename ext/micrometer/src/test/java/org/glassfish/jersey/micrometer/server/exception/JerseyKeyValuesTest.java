/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.micrometer.server.exception;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.glassfish.jersey.micrometer.server.DefaultJerseyObservationConvention;
import org.glassfish.jersey.micrometer.server.JerseyContext;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.internal.monitoring.RequestEventImpl;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.NotFoundException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Optional;

public class JerseyKeyValuesTest {
    @Test
    public void testOnException() {
        ExtendedUriInfo uriInfo = (ExtendedUriInfo) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{ExtendedUriInfo.class},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                switch (method.getName()) {
                    case "getMatchedTemplates":
                        return Collections.emptyList();
                }
                return null;
            }
        });
        RequestEventImpl event = new RequestEventImpl.Builder()
                .setExtendedUriInfo(uriInfo)
                .setException(new NotFoundException(), RequestEvent.ExceptionCause.ORIGINAL)
                .build(RequestEvent.Type.ON_EXCEPTION);
        JerseyContext context = new JerseyContext(event);
        DefaultJerseyObservationConvention convention = new DefaultJerseyObservationConvention("Test-Metric");
        KeyValues values = convention.getLowCardinalityKeyValues(context);
        Optional<KeyValue> kv = values.stream().filter(p -> p.getValue().equals("NOT_FOUND")).findFirst();
        MatcherAssert.assertThat(kv.isPresent(), Matchers.equalTo(true));
    }
}
