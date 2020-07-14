/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.example.bookshop.microprofile.ressources;

/**
 * Book object contains its name and its number of page
 */
public class Book {

    private int number_of_page;
    private String name;

    public Book(String name, int nbp){
        this.number_of_page = nbp;
        this.name = name;
    }

    public Book(){
    }

    public int getNumberOfPage(){
        return this.number_of_page;
    }

    public void setNumberOfPage(int nbp){
        this.number_of_page = nbp;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String pName){
        this.name = pName;
    }

}
