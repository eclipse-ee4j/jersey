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

package org.glassfish.jersey.tests.performance.mbw.custom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Custom message body worker.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Produces("application/person")
@Consumes("application/person")
public class PersonProvider implements MessageBodyWriter<Person>, MessageBodyReader<Person> {

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return type == Person.class;
    }

    @Override
    public long getSize(Person t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return getByteRepresentation(t).length;
    }

    @Override
    public void writeTo(Person t,
                        Class<?> type,
                        Type type1,
                        Annotation[] antns,
                        MediaType mt,
                        MultivaluedMap<String, Object> mm,
                        OutputStream out) throws IOException, WebApplicationException {
        out.write(getByteRepresentation(t));
    }

    @Override
    public boolean isReadable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return type == Person.class;
    }

    @Override
    public Person readFrom(Class<Person> type,
                           Type type1,
                           Annotation[] antns,
                           MediaType mt,
                           MultivaluedMap<String, String> mm,
                           InputStream in) throws IOException, WebApplicationException {
        Person result = new Person();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final String nameLine = reader.readLine();
        result.name = nameLine.substring(nameLine.indexOf(": ") + 2);
        final String ageLine = reader.readLine();
        result.age = Integer.parseInt(ageLine.substring(ageLine.indexOf(": ") + 2));
        final String addressLine = reader.readLine();
        result.address = addressLine.substring(addressLine.indexOf(": ") + 2);

        return result;
    }

    private byte[] getByteRepresentation(Person t) {
        return String.format("name: %s\nage: %d\naddress: %s", t.name, t.age, t.address).getBytes();
    }
}
