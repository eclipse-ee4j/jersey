/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.library.microprofile.ressources;

/**
 * Book object contains its name and its number of page
 */
public class Book {

    private int numberOfPages;
    private String name;

    public Book(String name, int nbp){
        this.numberOfPages = nbp;
        this.name = name;
    }

    public Book(){
    }

    public int getNumberOfPage(){
        return this.numberOfPages;
    }

    public String getName(){
        return this.name;
    }

    public void setNumberOfPage(int nbp){
        this.numberOfPages = nbp;
    }

    public void setName(String pName){
        this.name = pName;
    }

}
