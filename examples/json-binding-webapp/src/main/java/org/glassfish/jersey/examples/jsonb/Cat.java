/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonb;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;

/**
 * Example cat POJO for JSONB (un)marshalling.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@JsonbPropertyOrder({"color", "sort", "name", "domesticated"})
public class Cat {
    @JsonbProperty("catName")
    private String name;
    private String sort;
    private String color;
    private boolean domesticated;

    // json-b needs the default constructor
    public Cat() {
        super();
    }

    public Cat(String name, String sort, String color, boolean domesticated) {
        this.name = name;
        this.sort = sort;
        this.color = color;
        this.domesticated = domesticated;
    }

    public String getName() {
        return name;
    }

    public Cat setName(String name) {
        this.name = name;
        return this;
    }

    @JsonbProperty("catSort")
    public String getSort() {
        return sort;
    }

    public Cat setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public String getColor() {
        return color;
    }

    public Cat setColor(String color) {
        this.color = color;
        return this;
    }

    public boolean isDomesticated() {
        return domesticated;
    }

    public Cat setDomesticated(boolean domesticated) {
        this.domesticated = domesticated;
        return this;
    }
}
