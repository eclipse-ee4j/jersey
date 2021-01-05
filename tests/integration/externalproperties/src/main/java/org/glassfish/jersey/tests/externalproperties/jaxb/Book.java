/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.externalproperties.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Book")
public class Book implements Serializable {

    @XmlElement
    private String title;
    @XmlElement
    private String author;
    @XmlElement
    private int id;
    @XmlElement
    private int price;

    public Book() {
    }

    public Book(String title, String author, int id, int price) {
        setTitle(title);
        setAuthor(author);
        setId(id);
        setPrice(price);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Book [Title=" + getTitle()
                + ", Author=" + getAuthor()
                + ", ID=" + getId()
                + ", Price=" + getPrice()
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Book)) {
            return false;
        }

        return ((Book) obj).getTitle().equals(getTitle())
                && ((Book) obj).getAuthor().equals(getAuthor())
                && ((Book) obj).getId().equals(getId())
                && ((Book) obj).getPrice().equals(getPrice());
    }
}
