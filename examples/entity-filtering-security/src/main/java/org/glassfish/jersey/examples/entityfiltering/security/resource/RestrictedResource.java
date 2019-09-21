/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.security.resource;

import java.lang.annotation.Annotation;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.glassfish.jersey.examples.entityfiltering.security.domain.RestrictedEntity;
import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.message.filtering.SecurityAnnotations;

/**
 * Resource restricted with security annotations. Security restrictions are defined by resource methods and
 * {@link RestrictedEntity}.
 *
 * @author Michal Gajdos
 */
@Path("restricted-resource")
@Produces("application/json")
public class RestrictedResource {

    @GET
    @Path("denyAll")
    @DenyAll
    public RestrictedEntity denyAll() {
        return RestrictedEntity.instance();
    }

    @GET
    @Path("permitAll")
    @PermitAll
    public RestrictedEntity permitAll() {
        return RestrictedEntity.instance();
    }

    @GET
    @Path("rolesAllowed")
    @RolesAllowed({"manager"})
    public RestrictedEntity rolesAllowed() {
        return RestrictedEntity.instance();
    }

    @GET
    @Path("runtimeRolesAllowed")
    public Response runtimeRolesAllowed(@QueryParam("roles") @DefaultValue("") final String roles) {
        return Response
                .ok()
                .entity(RestrictedEntity.instance(),
                        new Annotation[] {SecurityAnnotations.rolesAllowed(Tokenizer.tokenize(roles))})
                .build();
    }
}
