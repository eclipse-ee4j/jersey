/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking.representation;

import java.util.List;

import javax.ws.rs.core.Link;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.glassfish.jersey.examples.linking.model.ItemsModel;
import org.glassfish.jersey.examples.linking.resources.ItemResource;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

/**
 * JAXB representation of an item
 *
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "item")
@InjectLinks({
        @InjectLink(
                resource = ItemResource.class,
                style = Style.ABSOLUTE,
                condition = "${instance.next}",
                bindings = @Binding(name = "id", value = "${instance.nextId}"),
                rel = "next"
        ),
        @InjectLink(
                resource = ItemResource.class,
                style = Style.ABSOLUTE,
                condition = "${instance.prev}",
                bindings = @Binding(name = "id", value = "${instance.prevId}"),
                rel = "prev"
        )
})
public class ItemRepresentation {

    @XmlElement
    private String name;

    @XmlTransient
    private String id;
    @XmlTransient
    private ItemsModel itemsModel;

    @InjectLink(
            resource = ItemResource.class,
            style = Style.ABSOLUTE,
            bindings = @Binding(name = "id", value = "${instance.id}"),
            rel = "self"
    )
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    @XmlElement(name = "link")
    Link self;

    @InjectLinks({
            @InjectLink(
                    resource = ItemResource.class,
                    style = Style.ABSOLUTE,
                    condition = "${instance.next}",
                    bindings = @Binding(name = "id", value = "${instance.nextId}"),
                    rel = "next"
            ),
            @InjectLink(
                    resource = ItemResource.class,
                    style = Style.ABSOLUTE,
                    condition = "${instance.prev}",
                    bindings = @Binding(name = "id", value = "${instance.prevId}"),
                    rel = "prev"
            )})
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    List<Link> links;

    public ItemRepresentation() {

    }

    public ItemRepresentation(ItemsModel itemsModel, String id, String name) {
        this.itemsModel = itemsModel;
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isNext() {
        return itemsModel.hasNext(id);
    }

    public boolean isPrev() {
        return itemsModel.hasPrev(id);
    }

    public String getNextId() {
        return itemsModel.getNextId(id);
    }

    public String getPrevId() {
        return itemsModel.getPrevId(id);
    }

}
