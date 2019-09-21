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

package org.glassfish.jersey.tests.e2e.server.mvc.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.glassfish.jersey.server.validation.ValidationError;

/**
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class TestViewProcessor implements TemplateProcessor<String> {

    @Override
    public String resolve(String path, final MediaType mediaType) {
        final String extension = getExtension();

        if (!path.endsWith(extension)) {
            path = path + extension;
        }

        final URL u = this.getClass().getResource(path);
        if (u == null || !acceptMediaType(mediaType)) {
            return null;
        }
        return path;
    }

    protected boolean acceptMediaType(final MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(String templateReference, Viewable viewable, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {

        final PrintStream ps = new PrintStream(out);
        ps.print("name=");
        ps.print(getViewProcessorName());
        ps.println();
        ps.print("path=");
        ps.print(templateReference);
        ps.println();
        ps.print("model=");
        ps.print(getModel(viewable.getModel()));
        ps.println();
    }

    private String getModel(final Object model) {
        if (model instanceof Collection) {
            StringBuilder builder = new StringBuilder();
            for (final Object object : (Collection) model) {
                builder.append(getModel(object)).append(',');
            }
            return builder.delete(builder.length() - 1, builder.length()).toString();
        } else if (model instanceof ValidationError) {
            final ValidationError error = (ValidationError) model;
            return error.getMessageTemplate() + "_" + error.getPath() + "_" + error.getInvalidValue();
        }
        return model.toString();
    }

    protected String getExtension() {
        return ".testp";
    }

    protected String getViewProcessorName() {
        return "TestViewProcessor";
    }

}
