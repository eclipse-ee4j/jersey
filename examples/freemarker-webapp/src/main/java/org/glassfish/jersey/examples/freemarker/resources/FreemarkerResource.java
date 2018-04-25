/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.freemarker.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Path("/")
public class FreemarkerResource {

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getHello() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("user", "Pavel");
        final List<String> list = new ArrayList<String>();
        list.add("item1");
        list.add("item2");
        list.add("item3");
        map.put("items", list);

        return new Viewable("/hello.ftl", map);
    }

    @GET
    @Path("hello-default-model")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getHelloWithDefaultModel() {
        return new Viewable("/hello-default-model.ftl", "Pavel");
    }

    @GET
    @Path("autoTemplate")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAutoTemplate() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("user", "Pavel");

        // template name is derived from resource class name
        return new Viewable("/org/glassfish/jersey/examples/freemarker/resources/FreemarkerResource.ftl",
                map);
    }

    @GET
    @Path("helloWithoutSuffix")
    @Produces(MediaType.TEXT_HTML)
    public Viewable getHelloWithoutSuffix() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("user", "Pavel");
        final List<String> list = new ArrayList<String>();
        list.add("item1");
        list.add("item2");
        list.add("item3");
        map.put("items", list);

        return new Viewable("/hello", map);
    }
}
