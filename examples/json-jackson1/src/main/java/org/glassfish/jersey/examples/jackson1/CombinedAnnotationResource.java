/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("combinedAnnotations")
public class CombinedAnnotationResource {

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public CombinedAnnotationBean getAccount() {
        return new CombinedAnnotationBean(12);
    }
}
