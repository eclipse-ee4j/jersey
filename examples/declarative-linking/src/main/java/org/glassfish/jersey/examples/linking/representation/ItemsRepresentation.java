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

import java.util.ArrayList;
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
import org.glassfish.jersey.examples.linking.resources.ItemsResource;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

/**
 * JAXB representation of a sublist of items
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "items")
@InjectLinks({
        @InjectLink(
                resource = ItemsResource.class,
                style = Style.ABSOLUTE,
                method = "query",
                condition = "${instance.offset + instance.limit < instance.modelLimit}",
                bindings = {
                        @Binding(name = "offset", value = "${instance.offset + instance.limit}"),
                        @Binding(name = "limit", value = "${instance.limit}")
                },
                rel = "next"
        ),
        @InjectLink(
                resource = ItemsResource.class,
                style = Style.ABSOLUTE,
                method = "query",
                condition = "${instance.offset - instance.limit >= 0}",
                bindings = {
                        @Binding(name = "offset", value = "${instance.offset - instance.limit}"),
                        @Binding(name = "limit", value = "${instance.limit}")
                },
                rel = "prev"
        )})

public class ItemsRepresentation {

    @XmlElement(name = "items")
    private List<ItemRepresentation> items;

    @XmlTransient
    private int offset, limit;

    @XmlTransient
    private ItemsModel itemsModel;

    @InjectLink(
            resource = ItemsResource.class,
            method = "query",
            style = Style.ABSOLUTE,
            bindings = {@Binding(name = "offset", value = "${instance.offset}"),
                    @Binding(name = "limit", value = "${instance.limit}")
            },
            rel = "self"
    )
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    @XmlElement(name = "link")
    Link self;

    @InjectLinks({
            @InjectLink(
                    resource = ItemsResource.class,
                    style = Style.ABSOLUTE,
                    method = "query",
                    condition = "${instance.offset + instance.limit < instance.modelLimit}",
                    bindings = {
                            @Binding(name = "offset", value = "${instance.offset + instance.limit}"),
                            @Binding(name = "limit", value = "${instance.limit}")
                    },
                    rel = "next"
            ),
            @InjectLink(
                    resource = ItemsResource.class,
                    style = Style.ABSOLUTE,
                    method = "query",
                    condition = "${instance.offset - instance.limit >= 0}",
                    bindings = {
                            @Binding(name = "offset", value = "${instance.offset - instance.limit}"),
                            @Binding(name = "limit", value = "${instance.limit}")
                    },
                    rel = "prev"
            )})
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    List<Link> links;

    public ItemsRepresentation() {
        offset = 0;
        limit = 10;
    }

    public ItemsRepresentation(ItemsModel itemsModel, int offset, int limit) {

        this.offset = offset;
        this.limit = limit;
        this.itemsModel = itemsModel;

        items = new ArrayList<>();
        for (int i = offset; i < (offset + limit) && i < itemsModel.getSize(); i++) {
            items.add(new ItemRepresentation(
                    itemsModel,
                    Integer.toString(i),
                    itemsModel.getItem(Integer.toString(i)).getName()));
        }

    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getModelLimit() {
        return itemsModel.getSize();
    }
}
