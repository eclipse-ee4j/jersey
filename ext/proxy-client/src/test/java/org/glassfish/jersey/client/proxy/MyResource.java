/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class MyResource implements MyResourceIfc {

    @Context HttpHeaders headers;

    @Override
    public String getIt() {
        return "Got it!";
    }

    @Override
    public List<MyBean> postIt(List<MyBean> entity) {
        return entity;
    }

    @Override
    public MyBean postValid(@Valid MyBean entity) {
        return entity;
    }

    @Override
    public String getId(String id) {
        return id;
    }

    @Override
    public String getByName(String name) {
        return name;
    }

    @Override
    public String getByNameCookie(String name) {
        return name;
    }

    @Override
    public String getByNameHeader(String name) {
        return name;
    }

    @Override
    public String getByNameMatrix(String name) {
        return name;
    }

    @Override
    public String postByNameFormParam(String name) {
        return name;
    }

    @Override
    public String getByNameList(List<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameSet(Set<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameSortedSet(SortedSet<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameCookieList(List<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameCookieSet(Set<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameCookieSortedSet(SortedSet<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameHeaderList(List<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameHeaderSet(Set<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameHeaderSortedSet(SortedSet<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameMatrixList(List<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameMatrixSet(Set<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String getByNameMatrixSortedSet(SortedSet<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String postByNameFormList(List<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String postByNameFormSet(Set<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public String postByNameFormSortedSet(SortedSet<String> name) {
        return name.size() + ":" + name;
    }

    @Override
    public MySubResourceIfc getSubResource() {
        return new MySubResource();
    }

    @Override
    public boolean isAcceptHeaderValid(HttpHeaders headers) {
        List<MediaType> accepts = headers.getAcceptableMediaTypes();
        return accepts.contains(MediaType.TEXT_PLAIN_TYPE) && accepts.contains(MediaType.TEXT_XML_TYPE);
    }

    @Override
    public String putIt(MyBean dummyBean) {
        return headers.getHeaderString(HttpHeaders.CONTENT_TYPE);
    }
}
