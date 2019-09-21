/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking.resources;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.examples.linking.model.ItemModel;
import org.glassfish.jersey.examples.linking.model.ItemsModel;
import org.glassfish.jersey.examples.linking.representation.ItemRepresentation;

/**
 * Resource that provides access to one item from a set of items managed
 * by ItemsModel
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class ItemResource {

    private ItemsModel itemsModel;
    private ItemModel itemModel;
    private String id;

    public ItemResource() {
        throw new IllegalStateException("Only for JAX-B dressing");
    }

    public ItemResource(ItemsModel itemsModel, String id) {
        this.id = id;
        this.itemsModel = itemsModel;
        try {
            itemModel = itemsModel.getItem(id);
        } catch (IndexOutOfBoundsException ex) {
            throw new NotFoundException();
        }
    }

    @GET
    public ItemRepresentation get() {
        return new ItemRepresentation(itemsModel, id, itemModel.getName());
    }

    public String getId() {
        return id;
    }
}
