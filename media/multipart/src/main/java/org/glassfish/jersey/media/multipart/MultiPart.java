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

package org.glassfish.jersey.media.multipart;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * A mutable model representing a MIME MultiPart entity.  This class extends
 * {@link BodyPart} because MultiPart entities can be nested inside other
 * MultiPart entities to an arbitrary depth.
 *
 * @author Craig McClanahan
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class MultiPart extends BodyPart implements Closeable {

    private BodyPartsList bodyParts = new BodyPartsList(this);

    /**
     * Instantiates a new {@link MultiPart} with a {@code mediaType} of
     * {@code multipart/mixed}.
     */
    public MultiPart() {
        super(new MediaType("multipart", "mixed"));
    }


    /**
     * Instantiates a new {@link MultiPart} with the specified characteristics.
     *
     * @param mediaType the {@link MediaType} for this multipart.
     */
    public MultiPart(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Return a mutable list of {@link BodyPart}s nested in this
     * {@link MultiPart}.
     */
    public List<BodyPart> getBodyParts() {
        return this.bodyParts;
    }

    /**
     * Disables access to the entity for a {@link MultiPart}. Use the list
     * returned by {@code getBodyParts()} to access the relevant
     * {@link BodyPart} instead.
     *
     * @throws IllegalStateException thrown unconditionally.
     */
    @Override
    public Object getEntity() {
        throw new IllegalStateException("Cannot get entity on a MultiPart instance");
    }

    /**
     * Disables access to the entity for a {@link MultiPart}. Use the list
     * returned by {@code getBodyParts()} to access the relevant
     * {@link BodyPart} instead.
     *
     * @param entity
     */
    @Override
    public void setEntity(Object entity) {
        throw new IllegalStateException("Cannot set entity on a MultiPart instance");
    }

    /**
     * Sets the {@link MediaType} for this {@link MultiPart}. If never set,
     * the default {@link MediaType} MUST be {@code multipart/mixed}.
     *
     * @param mediaType the new {@link MediaType}.
     * @throws IllegalArgumentException if the {@code type} property is not set to {@code multipart}.
     */
    @Override
    public void setMediaType(MediaType mediaType) {
        if (!"multipart".equals(mediaType.getType())) {
            throw new IllegalArgumentException(mediaType.toString());
        }
        super.setMediaType(mediaType);
    }

    /**
     * Builder pattern method to add the specified {@link BodyPart} to this
     * {@link MultiPart}.
     *
     * @param bodyPart {@link BodyPart} to be added.
     */
    public MultiPart bodyPart(BodyPart bodyPart) {
        getBodyParts().add(bodyPart);
        return this;
    }

    /**
     * Builder pattern method to add a newly configured {@link BodyPart}
     * to this {@link MultiPart}.
     *
     * @param entity entity object for this body part.
     * @param mediaType content type for this body part.
     */
    public MultiPart bodyPart(Object entity, MediaType mediaType) {
        BodyPart bodyPart = new BodyPart(entity, mediaType);
        return bodyPart(bodyPart);
    }

    /**
     * Override the entity set operation on a {@link MultiPart} to throw
     * {@code IllegalArgumentException}.
     *
     * @param entity entity to set for this {@link BodyPart}.
     */
    @Override
    public BodyPart entity(Object entity) {
        setEntity(entity);
        return this;
    }

    /**
     * Builder pattern method to return this {@link MultiPart} after
     * additional configuration.
     *
     * @param type media type to set for this {@link MultiPart}.
     */
    @Override
    public MultiPart type(MediaType type) {
        setMediaType(type);
        return this;
    }

    /**
     * Performs any necessary cleanup at the end of processing this
     * {@link MultiPart}.
     */
    @Override
    public void cleanup() {
        for (BodyPart bp : getBodyParts()) {
            bp.cleanup();
        }
    }

    public void close() throws IOException {
        cleanup();
    }

}
