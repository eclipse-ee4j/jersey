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

package org.glassfish.jersey.tests.e2e.json.entity;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Formatter;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.json.Jersey1199Test;

/**
 * @author Michal Gajdos
 */
@SuppressWarnings({"UnusedDeclaration", "NumberEquality", "SimplifiableIfStatement"})
@XmlRootElement
public class Jersey1199List {

    public static Object createTestInstance() {
        final ColorHolder obj1 = new ColorHolder(EnumSet.of(Color.RED, Color.BLUE));
        final ColorHolder obj2 = new ColorHolder(EnumSet.of(Color.GREEN));

        return new Jersey1199List(new Object[]{obj1, obj2});
    }

    private Object[] objects;
    private Integer offset;
    private Integer total;

    public Jersey1199List() {
    }

    public Jersey1199List(final Object[] objects) {
        this.objects = objects;
        this.offset = 0;
        this.total = objects.length;
    }

    // Jackson 1
    @org.codehaus.jackson.annotate.JsonTypeInfo(
            use = org.codehaus.jackson.annotate.JsonTypeInfo.Id.NAME,
            include = org.codehaus.jackson.annotate.JsonTypeInfo.As.PROPERTY)
    @org.codehaus.jackson.annotate.JsonSubTypes({
            @org.codehaus.jackson.annotate.JsonSubTypes.Type(value = ColorHolder.class)
    })
    // Jackson 2
    @com.fasterxml.jackson.annotation.JsonTypeInfo(
            use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME,
            include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY)
    @com.fasterxml.jackson.annotation.JsonSubTypes({
            @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = ColorHolder.class)
    })
    // JSON-B
    @JsonbTypeAdapter(Jersey1199Test.JsonbObjectToColorHolderAdapter.class)
    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(final Object[] objects) {
        this.objects = objects;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(final Integer offset) {
        this.offset = offset;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(final Integer total) {
        this.total = total;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Jersey1199List)) {
            return false;
        }

        final Jersey1199List other = (Jersey1199List) obj;

        if (this.total != other.total && (this.total == null || !this.total.equals(other.total))) {
            return false;
        }
        if (this.offset != other.offset && (this.offset == null || !this.offset.equals(other.offset))) {
            return false;
        }

        return Arrays.equals(this.objects, other.objects);
    }

    @Override
    public String toString() {
        return new Formatter().format("Jersey1199List(%s, %d, %d)", Arrays.toString(objects), offset, total).toString();
    }

    @Override
    public int hashCode() {
        int hash = 43;
        hash += (offset != null ? 17 * offset : 0);
        hash += (total != null ? 17 * total : 0);
        hash += Arrays.hashCode(objects);
        return hash;
    }
}
