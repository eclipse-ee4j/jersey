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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

/**
 * Note: With MOXy we need to ensure that collections (a list in this case) with predefined values (assigned to the list during
 * object initialization) are either uninitialized or empty during the object creation, otherwise there is a possibility that
 * these default values are doubled in the list (list is filled with default values when a new instance is created and after
 * unmarshalling XML/JSON stream additional elements are added to this list - MOXy doesn't override the existing list with a
 * new one created during unmarshalling).
 * <p/>
 * Workaround: Set {@link javax.xml.bind.annotation.XmlAccessorType} to {@link javax.xml.bind.annotation.XmlAccessType#FIELD},
 * do not initialize the list in the default constructor
 * (field initializer) and assign the value to the list that should contain predefined values manually (in this case the value
 * object is represented by {@code #DEFAULT_HEADERS}).
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserTable {

    @SuppressWarnings("RedundantIfStatement")
    public static class JMakiTableHeader {

        public String id;
        public String label;

        public JMakiTableHeader() {
        }

        public JMakiTableHeader(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public int hashCode() {
            int hash = 13;
            hash = id != null ? 29 * id.hashCode() : hash;
            hash = label != null ? 29 * label.hashCode() : hash;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof JMakiTableHeader)) {
                return false;
            }
            JMakiTableHeader that = (JMakiTableHeader) obj;

            if ((id != null && !id.equals(that.id)) && that.id != null) {
                return false;
            }
            if ((label != null && !label.equals(that.label)) && that.label != null) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return "JMakiTableHeader(id = " + id + ", label = " + label + ")";
        }
    }

    public static Object createTestInstance() {
        UserTable instance = new UserTable();
        instance.rows = new LinkedList<User>();
        instance.rows.add(JsonTestHelper.createTestInstance(User.class));
        instance.columns = DEFAULT_HEADERS;
        return instance;
    }

    public static Object createTestInstance2() {
        UserTable instance = new UserTable();
        instance.rows = new LinkedList<User>();
        instance.rows.add(JsonTestHelper.createTestInstance(User.class));
        instance.addColumn(new JMakiTableHeader("password", "Password"));
        return instance;
    }

    static List<JMakiTableHeader> initHeaders() {
        List<JMakiTableHeader> headers = new LinkedList<JMakiTableHeader>();
        headers.add(new JMakiTableHeader("userid", "UserID"));
        headers.add(new JMakiTableHeader("name", "User Name"));
        return Collections.unmodifiableList(headers);
    }

    public static final List<JMakiTableHeader> DEFAULT_HEADERS = initHeaders();

    private List<JMakiTableHeader> columns;
    private List<User> rows;

    public UserTable() {
    }

    public UserTable(List<User> users) {
        this.rows = new LinkedList<User>();
        this.rows.addAll(users);
        this.columns = DEFAULT_HEADERS;
    }

    public void addColumn(final JMakiTableHeader column) {
        getColumns().add(column);
    }

    public List<JMakiTableHeader> getColumns() {
        if (columns == null) {
            columns = new LinkedList<JMakiTableHeader>(DEFAULT_HEADERS);
        }
        return columns;
    }

    public void setColumns(final List<JMakiTableHeader> columns) {
        this.columns = columns;
    }

    public List<User> getRows() {
        return rows;
    }

    public void setRows(final List<User> rows) {
        this.rows = rows;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserTable)) {
            return false;
        }
        final UserTable other = (UserTable) obj;

        return JsonTestHelper.areCollectionsEqual(this.rows, other.rows)
                && JsonTestHelper.areCollectionsEqual(this.columns, other.columns);
    }

    @Override
    public int hashCode() {
        int hash = 16;
        if (null != rows) {
            for (User u : rows) {
                hash = 17 * hash + u.hashCode();
            }
        }
        if (null != columns) {
            for (JMakiTableHeader u : columns) {
                hash = 17 * hash + u.hashCode();
            }
        }
        return hash;
    }

    @Override
    public String toString() {
        return String.format("UserTable(%s,%s)", rows, columns);
    }
}
