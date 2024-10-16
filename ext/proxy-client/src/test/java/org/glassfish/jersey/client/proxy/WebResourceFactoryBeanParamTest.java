/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.core.Cookie;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Richard Obersheimer
 */
public class WebResourceFactoryBeanParamTest  extends JerseyTest {

    private MyResourceWithBeanParamIfc resourceWithBeanParam;

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(MyResourceWithBeanParam.class);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        resourceWithBeanParam = WebResourceFactory.newResource(MyResourceWithBeanParamIfc.class, target());
    }

    @Test
    public void testBeanParamQuery() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setQueryParam("query");

        String response = resourceWithBeanParam.echoQuery(myGetBeanParam);

        assertEquals("query", response);
    }

    @Test
    public void testBeanParamHeader() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setHeaderParam("header");

        String response = resourceWithBeanParam.echoHeader(myGetBeanParam);

        assertEquals("header", response);
    }

    @Test
    public void testBeanParamPath() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setPathParam("path");

        String response = resourceWithBeanParam.echoPath(myGetBeanParam);

        assertEquals("path", response);
    }

    @Test
    public void testBeanParamCookie() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        Cookie cookie = new Cookie("cName", "cValue");
        myGetBeanParam.setCookieParam(cookie);

        String response = resourceWithBeanParam.echoCookie(myGetBeanParam);

        assertEquals("cValue", response);
    }

    @Test
    public void testBeanParamMatrix() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        List<String> matrixParam = Arrays.asList("1", "2", "3");
        myGetBeanParam.setMatrixParam(matrixParam);

        String response = resourceWithBeanParam.echoMatrix(myGetBeanParam);

        assertEquals(matrixParam.toString(), response);
    }

    @Test
    public void testBeanParamSubBean() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        List<String> subQueryParam = Arrays.asList("1", "2", "3");
        MySubBeanParam subBeanParam = new MySubBeanParam(subQueryParam);
        myGetBeanParam.setSubBeanParam(subBeanParam);

        String response = resourceWithBeanParam.echoSubBean(myGetBeanParam);

        assertEquals(subQueryParam.toString(), response);
    }

    @Test
    public void testBeanParam() {
        List<String> matrixParam = Arrays.asList("1", "2", "3");
        Cookie cookieParam = new Cookie("cookie1", "value1");
        List<String> subQueryParam = Arrays.asList("subQuery1", "subQuery2");
        MySubBeanParam subBeanParam = new MySubBeanParam(subQueryParam);
        MyBeanParam myBeanParam = new MyBeanParam("header", "path", "query",
                "form1", "form2", matrixParam, cookieParam, subBeanParam);
        myBeanParam.setQueryParam2("q2");

        String response = resourceWithBeanParam.echo(myBeanParam);

        assertEquals("HEADER=header,PATH=path,FORM=form1,form2,QUERY=query,MATRIX=3,COOKIE=value1,SUB=2"
                + ",Q2=q2", response);
    }

    @Test
    public void testSubResource() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setQueryParam("query");

        String response = resourceWithBeanParam.getSubResource().echoQuery(myGetBeanParam);

        assertEquals("query", response);
    }

    @Test
    public void testBeanParamPrivateFieldQuery() {
        MyBeanParamWithPrivateField myGetBeanParam = new MyBeanParamWithPrivateField();
        myGetBeanParam.setPrivateFieldParam("query");

        String response = resourceWithBeanParam.echoPrivateField(myGetBeanParam);

        assertEquals("query", response);
    }
}