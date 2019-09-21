/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.JsonObject;

/**
 * Storage of documents.
 *
 * @author Michal Gajdos
 */
public final class DocumentStorage {

    private static final Map<Integer, JsonObject> storage = new LinkedHashMap<>();
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static int store(final JsonObject document) {
        final int id = counter.addAndGet(1);
        storage.put(id, document);
        return id;
    }

    public static JsonObject get(final int id) {
        return storage.get(id);
    }

    public static Collection<JsonObject> getAll() {
        return storage.values();
    }

    public static JsonObject remove(final int id) {
        return storage.remove(id);
    }

    public static void removeAll() {
        storage.clear();
    }

    /**
     * Prevent initialization.
     */
    private DocumentStorage() {
    }
}
