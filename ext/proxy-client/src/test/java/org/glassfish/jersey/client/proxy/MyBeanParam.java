/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;

import java.util.List;

/**
 * @author Richard Obersheimer
 */
public class MyBeanParam extends MyGetBeanParam {

    @FormParam("formParam1")
    String formParam1;

    @FormParam("formParam2")
    String formParam2;

    String queryParam2;

    public MyBeanParam(String headerParam, String pathParam, String queryParam, String formParam1, String formParam2,
                       List<String> matrixParam, Cookie cookieParam, MySubBeanParam subBeanParam) {
        this.headerParam = headerParam;
        this.pathParam = pathParam;
        this.queryParam = queryParam;
        this.formParam1 = formParam1;
        this.formParam2 = formParam2;
        this.matrixParam = matrixParam;
        this.cookieParam = cookieParam;
        this.subBeanParam = subBeanParam;
    }

    public MyBeanParam() {}

    public String getFormParam1() {
        return formParam1;
    }

    public void setFormParam1(String formParam1) {
        this.formParam1 = formParam1;
    }

    public String getFormParam2() {
        return formParam2;
    }

    public void setFormParam2(String formParam2) {
        this.formParam2 = formParam2;
    }

    @QueryParam("queryParam2")
    public String getQueryParam2() {
        return queryParam2;
    }

    @QueryParam("queryParam2")
    public void setQueryParam2(String queryParam2) {
        this.queryParam2 = queryParam2;
    }

}
