/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookstore.webapp.resource;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CD extends Item {

    private Track[] tracks;

    public CD() {
    }

    public CD(final String title, final String author, final Track[] tracks) {
        super(title, author);
        this.tracks = tracks;
    }

    public Track[] getTracks() {
        return tracks;
    }

    @Path("tracks/{num}/")
    public Track getTrack(@PathParam("num") int num) {
        if (num >= tracks.length) {
            throw new NotFoundException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Track, " + num + ", of CD, " + getTitle() + ", is not found")
                    .build());
        }
        return tracks[num];
    }
}
