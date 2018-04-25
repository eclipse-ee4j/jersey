/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json.entity;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

/**
 * @author mchenryc
 * @author Michal Gajdos
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "listEmptyBean")
public class ListEmptyBean {

    private List<String> empty;

    public static Object createTestInstance() {
        ListEmptyBean instance = new ListEmptyBean();
        instance.empty = new LinkedList<>();
        return instance;
    }

    public List<String> getEmpty() {
        return empty;
    }

    public void setEmpty(List<String> empty) {
        this.empty = empty;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ListEmptyBean other = (ListEmptyBean) obj;
        return this.empty == other.empty
                || (JsonTestHelper.isCollectionEmpty(this.empty) && JsonTestHelper.isCollectionEmpty(other.empty))
                || (this.empty != null && this.empty.equals(other.empty));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.empty != null ? this.empty.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return (new Formatter()).format("LwNB(n=%d,isNull:%s)", (empty != null) ? empty.size() : 0, (empty == null)).toString();
    }
}
