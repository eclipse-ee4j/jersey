/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ParamConverterDateTest extends AbstractTest {

    @Path("/")
    public static class DateResource {

        @GET
        public String doGet(@QueryParam("d") final Date d) {
            return "DATE";
        }

    }

    @Test
    public void testDateResource() throws ExecutionException, InterruptedException {
        initiateWebApplication(getBinder(), ParamConverterDateTest.DateResource.class);
        final ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/")
                .queryParam("d", new Date()).build().toString());

        assertEquals(200, responseContext.getStatus());
    }

    private Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new ParamConverterProvider() {

                    @Override
                    public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                              final Type genericType,
                                                              final Annotation[] annotations) {
                        return (rawType != Date.class) ? null : new ParamConverter<T>() {

                            @Override
                            public T fromString(final String value) {
                                if (value == null) {
                                    throw new IllegalArgumentException(
                                            LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                                    );
                                }
                                try {
                                    final String format = "EEE MMM dd HH:mm:ss Z yyyy";
                                    final SimpleDateFormat formatter = new SimpleDateFormat(format, new Locale("US"));
                                    return rawType.cast(formatter.parse(value));
                                } catch (final ParseException ex) {
                                    throw new ExtractorException(ex);
                                }
                            }

                            @Override
                            public String toString(final T value) throws IllegalArgumentException {
                                if (value == null) {
                                    throw new IllegalArgumentException(
                                            LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                                    );
                                }
                                return value.toString();
                            }
                        };
                    }
                }).to(ParamConverterProvider.class);
            }
        };
    }
}
