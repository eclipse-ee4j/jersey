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

package org.glassfish.jersey.tests.e2e.entity.filtering.domain;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.glassfish.jersey.tests.e2e.entity.filtering.PrimaryDetailedView;
import org.glassfish.jersey.tests.e2e.entity.filtering.SecondaryDetailedView;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Michal Gajdos
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.ANY)
@XmlAccessorType(XmlAccessType.PROPERTY)
// Entity Filtering.
@PrimaryDetailedView
@SecondaryDetailedView
public class ManyFilteringsOnClassEntity {

    public static final ManyFilteringsOnClassEntity INSTANCE;

    static {
        INSTANCE = new ManyFilteringsOnClassEntity();
        INSTANCE.field = 50;
        INSTANCE.property = "property";
        INSTANCE.defaultEntities = Collections.singletonList(DefaultFilteringSubEntity.INSTANCE);
        INSTANCE.oneEntities = Collections.singletonList(OneFilteringSubEntity.INSTANCE);
        INSTANCE.manyEntities = Collections.singletonList(ManyFilteringsSubEntity.INSTANCE);
        INSTANCE.filtered = FilteredClassEntity.INSTANCE;
    }

    @XmlElement
    public int field;
    private String property;

    @XmlElement
    public List<DefaultFilteringSubEntity> defaultEntities;

    @XmlElement
    public List<OneFilteringSubEntity> oneEntities;
    @XmlElement
    public List<ManyFilteringsSubEntity> manyEntities;

    @XmlElement
    public FilteredClassEntity filtered;

    @XmlTransient
    @JsonIgnore
    public String accessorTransient;

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    public String getAccessor() {
        return accessorTransient == null ? property + property : accessorTransient;
    }

    public void setAccessor(final String accessor) {
        accessorTransient = accessor;
    }
}
