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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.server.mvc.Viewable;

/**
 * A resolved {@link org.glassfish.jersey.server.mvc.Viewable viewable}.
 * <p/>
 * A resolved viewable is obtained from the resolving methods on {@link org.glassfish.jersey.server.mvc.spi.ViewableContext}
 * and has associated with it a {@link TemplateProcessor} that is capable of processing a template identified by a template
 * reference.
 *
 * @param <T> the type of the resolved template object.
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public final class ResolvedViewable<T> extends Viewable {

    private final TemplateProcessor<T> viewProcessor;

    private final T templateReference;

    private final MediaType mediaType;

    private final Class<?> resolvingClass;


    /**
     * Create a resolved viewable.
     *
     * @param viewProcessor the view processor that resolved a template name to a template reference.
     * @param templateReference the template reference.
     * @param viewable the viewable that is resolved.
     * @param mediaType media type the {@code templateReference} should be transformed into.
     */
    public ResolvedViewable(TemplateProcessor<T> viewProcessor, T templateReference, Viewable viewable, MediaType mediaType) {
        this(viewProcessor, templateReference, viewable, null, mediaType);
    }

    /**
     * Create a resolved viewable.
     *
     * @param viewProcessor the view processor that resolved a template name to a template reference.
     * @param templateReference the template reference.
     * @param viewable the viewable that is resolved.
     * @param resolvingClass the resolving class that was used to resolve a relative template name into an absolute template name.
     * @param mediaType media type the {@code templateReference} should be transformed into.
     */
    public ResolvedViewable(TemplateProcessor<T> viewProcessor, T templateReference, Viewable viewable,
                            Class<?> resolvingClass, MediaType mediaType) {
        super(viewable.getTemplateName(), viewable.getModel());

        this.viewProcessor = viewProcessor;
        this.templateReference = templateReference;
        this.mediaType = mediaType;
        this.resolvingClass = resolvingClass;
    }

    /**
     * Write the resolved viewable.
     * <p/>
     * This method defers to
     * {@link TemplateProcessor#writeTo(Object, org.glassfish.jersey.server.mvc.Viewable, javax.ws.rs.core.MediaType,
     * javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * to write the viewable utilizing the template reference.
     *
     * @param out the output stream that the view processor writes to.
     * @throws java.io.IOException if there was an error processing the template.
     */
    public void writeTo(OutputStream out, final MultivaluedMap<String, Object> httpHeaders) throws IOException {
        viewProcessor.writeTo(templateReference, this, mediaType, httpHeaders, out);
    }

    /**
     * Get the media type for which the {@link TemplateProcessor view processor} resolved the template reference.
     *
     * @return final {@link javax.ws.rs.core.MediaType media type} of the resolved viewable.
     */
    public MediaType getMediaType() {
        return mediaType;
    }


    /**
     * Get resolving class.
     *
     * @return Resolving class.
     */
    public Class<?> getResolvingClass() {
        return resolvingClass;
    }
}
