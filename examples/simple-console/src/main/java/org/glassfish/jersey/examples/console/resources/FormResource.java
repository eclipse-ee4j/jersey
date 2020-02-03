/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.console.resources;

import java.io.InputStream;
import java.util.Date;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

/**
 * A Web form resource, produces the form and processes the results of
 * submitting it.
 *
 */
@Path("/form")
@Produces("text/html")
public class FormResource {

    private static final Colours coloursResource = new Colours();

    @Context
    HttpHeaders headers;

    @Path("colours")
    public Colours getColours() {
        return coloursResource;
    }

    /**
     * Produce a form from a static HTML file packaged with the compiled class
     * @return a stream from which the HTML form can be read.
     */
    @GET
    public Response getForm() {
        Date now = new Date();

        InputStream entity = this.getClass().getClassLoader().getResourceAsStream("form.html");
        return Response.ok(entity)
                .cookie(new NewCookie("date", now.toString())).build();
    }

    /**
     * Process the form submission. Produces a table showing the form field
     * values submitted.
     * @return a dynamically generated HTML table.
     * @param formData the data from the form submission
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public String processForm(MultivaluedMap<String, String> formData) {
        StringBuilder buf = new StringBuilder();
        buf.append("<html><head><title>Form results</title></head><body>");
        buf.append("<p>Hello, you entered the following information: </p><table border='1'>");
        for (String key : formData.keySet()) {
            if (key.equals("submit")) {
                continue;
            }
            buf.append("<tr><td>");
            buf.append(key);
            buf.append("</td><td>");
            buf.append(formData.getFirst(key));
            buf.append("</td></tr>");
        }
        for (Cookie c : headers.getCookies().values()) {
            buf.append("<tr><td>Cookie: ");
            buf.append(c.getName());
            buf.append("</td><td>");
            buf.append(c.getValue());
            buf.append("</td></tr>");
        }

        buf.append("</table></body></html>");
        return buf.toString();
    }

}
