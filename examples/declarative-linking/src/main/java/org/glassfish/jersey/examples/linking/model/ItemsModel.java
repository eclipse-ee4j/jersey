/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a three entry list of items and provides a simple means of navigating
 * the list.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class ItemsModel {

    private List<ItemModel> items;
    private static ItemsModel instance;

    public static synchronized ItemsModel getInstance() {
        if (instance == null) {
            instance = new ItemsModel();
        }
        return instance;
    }

    private ItemsModel() {
        items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new ItemModel("Item " + i));
        }
    }

    public boolean hasNext(String currentId) {
        return getIndex(currentId) < items.size() - 1;
    }

    public boolean hasPrev(String currentId) {
        return getIndex(currentId) > 0;
    }

    public ItemModel getItem(String id) {
        return items.get(getIndex(id));
    }

    public String getNextId(String id) {
        return Integer.toString(getIndex(id) + 1);
    }

    public String getPrevId(String id) {
        return Integer.toString(getIndex(id) - 1);
    }

    private int getIndex(String id) {
        return Integer.parseInt(id);
    }

    public int getSize() {
        return items.size();
    }
}
