/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

/**
 * @author Richard Obersheimer
 */
public class MyResourceWithBeanParam implements MyResourceWithBeanParamIfc {

    @Override
    public String echoQuery(MyGetBeanParam bean) {
        return bean.getQueryParam();
    }

    @Override
    public String echoHeader(@BeanParam MyGetBeanParam bean) {
        return bean.getHeaderParam();
    }

    @Override
    public String echoPath(@BeanParam MyGetBeanParam bean) {
        return bean.getPathParam();
    }

    @Override
    public String echoCookie(@BeanParam MyGetBeanParam bean) {
        return bean.getCookieParam().getValue();
    }

    @Override
    public String echoMatrix(@BeanParam MyGetBeanParam bean) {
        return bean.getMatrixParam().toString();
    }

    @Override
    public String echoSubBean(@BeanParam MyGetBeanParam bean) {
        return bean.getSubBeanParam().getSubQueryParam().toString();
    }

    @Override
    public String echoPrivateField(@BeanParam MyBeanParamWithPrivateField bean) {
        return bean.getPrivateFieldParam();
    }

    @Override
    public String echo(MyBeanParam bean) {
        return ("HEADER=" + bean.getHeaderParam() + ",PATH=" + bean.getPathParam() + ",FORM="
                + bean.getFormParam1() + "," + bean.getFormParam2() + ",QUERY=" + bean.getQueryParam()
                + ",MATRIX=" + bean.getMatrixParam().size() + ",COOKIE=" + bean.getCookieParam().getValue()
                + ",SUB=" + bean.getSubBeanParam().getSubQueryParam().size()
                + ",Q2=" + bean.getQueryParam2());
    }

    @Override
    public MyResourceWithBeanParamIfc getSubResource() {
        return new MyResourceWithBeanParam();
    }
}