package org.glassfish.jersey.example.bookshop.microprofile.ressources;

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
}
