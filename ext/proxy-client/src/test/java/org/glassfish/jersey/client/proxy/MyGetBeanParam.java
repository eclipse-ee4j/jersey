/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;

import java.util.List;

/**
 * @author Richard Obersheimer
 */
public class MyGetBeanParam {

    @HeaderParam("headerParam")
    String headerParam;

    @PathParam("pathParam")
    String pathParam;

    @QueryParam("queryParam")
    String queryParam;

    @MatrixParam("matrixParam")
    List<String> matrixParam;

    @CookieParam("cookieParam")
    Cookie cookieParam;

    @BeanParam
    MySubBeanParam subBeanParam;

    public MyGetBeanParam() {}

    public String getHeaderParam() {
        return headerParam;
    }

    public void setHeaderParam(String headerParam) {
        this.headerParam = headerParam;
    }

    public String getPathParam() {
        return pathParam;
    }

    public void setPathParam(String pathParam) {
        this.pathParam = pathParam;
    }

    public String getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(String queryParam) {
        this.queryParam = queryParam;
    }

    public List<String> getMatrixParam() {
        return matrixParam;
    }

    public void setMatrixParam(List<String> matrixParam) {
        this.matrixParam = matrixParam;
    }

    public Cookie getCookieParam() {
        return cookieParam;
    }

    public void setCookieParam(Cookie cookieParam) {
        this.cookieParam = cookieParam;
    }

    public MySubBeanParam getSubBeanParam() {
        return subBeanParam;
    }

    public void setSubBeanParam(MySubBeanParam subBeanParam) {
        this.subBeanParam = subBeanParam;
    }
}
