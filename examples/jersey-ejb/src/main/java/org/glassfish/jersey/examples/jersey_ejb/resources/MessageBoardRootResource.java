/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.resources;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

/**
 * Message board root resource. The main message board resource
 * gets injected as an EJB stateless bean
 * and provided via a sub-resource locator for further processing.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Stateless
@Path("/")
public class MessageBoardRootResource {

    @EJB MessageBoardResourceBean r;

    @Path("messages")
    public MessageBoardResourceBean getMessageBoardResourceBean() {
        return r;
    }
}

