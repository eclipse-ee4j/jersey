/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.library.microprofile.ressources;

import java.util.Date;

/**
 * BookingInfo contains all information requested to make a book reservation
 */
public class BookingInfo {

    private String customerName;
    private String bookName;
    private Date fromDate;
    private Date toDate;

    public BookingInfo(){
    }

    public BookingInfo(String customerName, String bookName, Date fromDate, Date toDate) {
        this.customerName = customerName;
        this.bookName = bookName;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public String getBookName() {
        return this.bookName;
    }

    public Date getFromDate() {
        return this.fromDate;
    }

    public Date getToDate() {
        return this.toDate;
    }

    public void setCustomerName(String pCustomerName){
        this.customerName = pCustomerName;
    }

    public void setBookName(String pBookName) {
        this.bookName = pBookName;
    }

    public void setFromDate(Date pFromDate) {
        this.fromDate = pFromDate;
    }

    public void setToDate(Date pToDate) {
        this.toDate =  pToDate;
    }

}
