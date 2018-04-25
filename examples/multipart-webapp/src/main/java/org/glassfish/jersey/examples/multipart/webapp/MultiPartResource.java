/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.multipart.webapp;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

/**
 * @author Michal Gajdos
 */
@Path("/form")
public class MultiPartResource {

    @POST
    @Path("part")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@FormDataParam("part") String s) {
        return s;
    }

    @POST
    @Path("part-file-name")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(
            @FormDataParam("part") String s,
            @FormDataParam("part") FormDataContentDisposition d) {
        return s + ":" + d.getFileName();
    }

    @POST
    @Path("xml-jaxb-part")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(
            @FormDataParam("string") String s,
            @FormDataParam("string") FormDataContentDisposition sd,
            @FormDataParam("bean") Bean b,
            @FormDataParam("bean") FormDataContentDisposition bd) {
        return s + ":" + sd.getFileName() + "," + b.value + ":" + bd.getFileName();
    }
}
