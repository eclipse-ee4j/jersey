/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.integration.jersey2031;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

/**
 * Test resource.
 *
 * @author Michal Gajdos
 */
@Path("/")
public class Issue2031Resource {

    private static final Model model;
    private static final String absolutePath;

    static {
        absolutePath = "/" + Issue2031Resource.class.getName().replaceAll("\\.", "/").replace('$', '/') + "/index.jsp";
        model = new Model();
    }

    @GET
    @Path("viewable-relative")
    @Produces("text/html")
    public Viewable viewableRelative() {
        return new Viewable("index", model);
    }

    @GET
    @Path("viewable-absolute")
    @Produces("text/html")
    public Viewable viewableAbsolute() {
        return new Viewable(absolutePath, model);
    }

    @GET
    @Path("template-relative")
    @Produces("text/html")
    @Template(name = "index")
    public Model templateRelative() {
        return model;
    }

    @GET
    @Path("template-absolute")
    @Produces("text/html")
    @Template(
            name = "/org/glassfish/jersey/tests/integration/jersey2031/Issue2031Resource/index.jsp")
    public Model templateAbsolute() {
        return model;
    }

    public static class Model {

        private String index = "index";
        private String include = "include";

        public String getIndex() {
            return index;
        }

        public String getInclude() {
            return include;
        }
    }
}
