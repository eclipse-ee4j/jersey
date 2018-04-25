/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.glassfish.jersey.server.JSONP;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path(App.ROOT_PATH)
@Produces({"application/x-javascript", "application/json", "application/xml"})
public class ChangeListResource {

    static final List<ChangeRecordBean> changes = new LinkedList<ChangeRecordBean>();

    static {
        changes.add(new ChangeRecordBean(false, 2, "title \"User Guide\" updated"));
        changes.add(new ChangeRecordBean(true, 1, "fixed metadata"));
        changes.add(new ChangeRecordBean(false, 91, "added index"));
        changes.add(new ChangeRecordBean(false, 650, "\"Troubleshoothing\" chapter"));
        changes.add(new ChangeRecordBean(false, 1, "fixing typo"));
    }

    @GET
    @JSONP(queryParam = JSONP.DEFAULT_QUERY)
    public List<ChangeRecordBean> getChanges(@QueryParam(JSONP.DEFAULT_QUERY) String callback, @QueryParam("type") int type) {
        return changes;
    }

    @GET
    @Path("latest")
    @JSONP
    public ChangeRecordBean getLastChange(@QueryParam("callback") String callback, @QueryParam("type") int type) {
        return changes.get(changes.size() - 1);
    }
}
