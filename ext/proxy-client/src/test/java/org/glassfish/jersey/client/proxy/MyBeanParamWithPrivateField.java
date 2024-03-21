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


import jakarta.ws.rs.QueryParam;

/**
 * @author Divyansh Shekhar Gaur
 */
public class MyBeanParamWithPrivateField {

    @QueryParam("privateFieldParam")
    private String privateFieldParam;

    private static String privateStaticField;

    static String staticField;

    public MyBeanParamWithPrivateField() {}

    public String getPrivateFieldParam() {
        return privateFieldParam;
    }

    public void setPrivateFieldParam(String privateFieldParam) {
        this.privateFieldParam = privateFieldParam;
    }

    public static String getPrivateStaticField() {
        return privateStaticField;
    }

    public static void setPrivateStaticField(String privateStaticField) {
        MyBeanParamWithPrivateField.privateStaticField = privateStaticField;
    }

    public static String getStaticField() {
        return staticField;
    }

    public static void setStaticField(String staticField) {
        MyBeanParamWithPrivateField.staticField = staticField;
    }
}
