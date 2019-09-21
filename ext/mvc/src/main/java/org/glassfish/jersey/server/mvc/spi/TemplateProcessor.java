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

package org.glassfish.jersey.server.mvc.spi;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.spi.Contract;

/**
 * A view processor.
 * <p/>
 * Implementations of this interface shall be capable of resolving a
 * template name (+ media type) to a template reference that identifies a template supported
 * by the implementation. And, processing the template, identified by template
 * reference and media type, the results of which are written to an output stream.
 * <p/>
 * Implementations can register a view processor as a provider, for
 * example, annotating the implementation class with {@link javax.ws.rs.ext.Provider}
 * or registering an implementing class or instance as a singleton with
 * {@link org.glassfish.jersey.server.ResourceConfig} or {@link javax.ws.rs.core.Application}.
 * <p/>
 * Such view processors could be JSP view processors (supported by the
 * Jersey servlet and filter implementations) or say Freemarker or Velocity
 * view processors (not implemented).
 *
 * @param <T> the type of the template object.
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface TemplateProcessor<T> {

    /**
     * Resolve a template name to a template reference.
     *
     * @param name the template name.
     * @param mediaType requested media type of the template.
     * @return the template reference, otherwise {@code null} if the template name cannot be resolved.
     */
    public T resolve(String name, MediaType mediaType);

    /**
     * Process a template and write the result to an output stream.
     *
     * @param templateReference the template reference. This is obtained by calling the {@link #resolve(String,
     * javax.ws.rs.core.MediaType)} method with a template name and media type.
     * @param viewable the viewable that contains the model to be passed to the template.
     * @param mediaType media type the {@code templateReference} should be transformed into.
     * @param httpHeaders http headers that will be send in the response. Headers can be modified to
     *                    influence response headers before the the first byte is written
     *                    to the {@code out}. After the response buffer is committed the headers modification
     *                    has no effect. Template processor can for example set the content type of
     *                    the response.
     * @param out the output stream to write the result of processing the template.
     * @throws java.io.IOException if there was an error processing the template.
     *
     * @since 2.7
     */
    public void writeTo(T templateReference, Viewable viewable, MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException;

}
