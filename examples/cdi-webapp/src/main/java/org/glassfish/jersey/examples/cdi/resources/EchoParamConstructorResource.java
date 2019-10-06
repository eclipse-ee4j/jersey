/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi.resources;

import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;

import javax.inject.Inject;

/**
 * Shows constructor injection of a path parameter in a managed bean.
 *
 * @author Roberto Chinnici
 */
@ManagedBean
@Path("echoparamconstructor/{a}")
public class EchoParamConstructorResource {

    static final Logger LOGGER = Logger.getLogger(EchoParamConstructorResource.class.getName());

    String a;

    // no-arg ctor is required by WLS
    public EchoParamConstructorResource() {
    }

    @Inject
    public EchoParamConstructorResource(@PathParam("a") String a) {
        this.a = a;
    }

    @PostConstruct
    @SuppressWarnings("unused")
    private void postConstruct() {
        LOGGER.info(String.format("in post construct, a=%s", a));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "ECHO " + a;
    }
}
