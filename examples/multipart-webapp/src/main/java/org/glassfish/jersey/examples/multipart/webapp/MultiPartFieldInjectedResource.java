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

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * Field-injected version of the {@link FormDataParam} injection testing resource.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("/form-field-injected")
public class MultiPartFieldInjectedResource {
    @FormDataParam("string") private String s;
    @FormDataParam("string") private FormDataContentDisposition sd;
    @FormDataParam("bean") private Bean b;
    @FormDataParam("bean") private FormDataContentDisposition bd;

    @POST
    @Path("xml-jaxb-part")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post() {
        return s + ":" + sd.getFileName() + "," + b.value + ":" + bd.getFileName();
    }
}
