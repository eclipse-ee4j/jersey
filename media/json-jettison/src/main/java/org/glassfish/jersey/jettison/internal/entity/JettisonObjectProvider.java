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

package org.glassfish.jersey.jettison.internal.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.jettison.internal.LocalizationMessages;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Low-level JSON media type message entity provider (reader & writer) for
 * {@link JSONObject}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JettisonObjectProvider extends JettisonLowLevelProvider<JSONObject> {

    @Produces("application/json")
    @Consumes("application/json")
    public static final class App extends JettisonObjectProvider {
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JettisonObjectProvider {

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+json");
        }
    }

    JettisonObjectProvider() {
        super(JSONObject.class);
    }

    @Override
    public JSONObject readFrom(
            Class<JSONObject> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException {
        try {
            return new JSONObject(readFromAsString(entityStream, mediaType));
        } catch (JSONException je) {
            throw new WebApplicationException(
                    new Exception(LocalizationMessages.ERROR_PARSING_JSON_OBJECT(), je),
                    400);
        }
    }

    @Override
    public void writeTo(
            JSONObject t,
            Class<?> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(entityStream,
                    getCharset(mediaType));
            t.write(writer);
            writer.flush();
        } catch (JSONException je) {
            throw new WebApplicationException(
                    new Exception(LocalizationMessages.ERROR_WRITING_JSON_OBJECT(), je),
                    500);
        }
    }
}
