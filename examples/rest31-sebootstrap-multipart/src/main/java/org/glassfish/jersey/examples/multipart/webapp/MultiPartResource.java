/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.multipart.webapp;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;

@Path("/form")
public class MultiPartResource {

    @GET
    @Path("test")
    public String test() {
        return "Test successful";
    }

    @POST
    @Path("part")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@FormParam("part") String s) {
        return s;
    }

    @POST
    @Path("part-file-name")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@FormParam("part")EntityPart entityPart) throws IOException {
        return entityPart.getContent(String.class) + ":" + entityPart.getFileName().get();
    }

    @POST
    @Path("xml-jaxb-part")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(
            @FormParam("string") EntityPart stringEntityPart,
            @FormParam("bean") EntityPart beanEntityPart) throws IOException {
        return stringEntityPart.getContent(String.class) + ":" + stringEntityPart.getFileName().get() + ","
                + beanEntityPart.getContent(Bean.class).value + ":" + beanEntityPart.getFileName().get();
    }
}
