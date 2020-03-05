/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson1;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * @author Jakub Podlesak
 */
@Path("combinedAnnotations")
public class CombinedAnnotationResource {

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public CombinedAnnotationBean getAccount() {
        return new CombinedAnnotationBean(12);
    }
}
