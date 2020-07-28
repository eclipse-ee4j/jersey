package org.glassfish.jersey.example.bookshop.microprofile;

import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.example.bookshop.microprofile.server.BookingFeatures;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

public class BookingTest extends TestSupport {

    @Test
    @Ignore
    public void reserveBookTwiceTest() throws URISyntaxException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JULY, 1);
        Date fromDate = calendar.getTime();

        calendar.set(2020, Calendar.JULY, 2);
        Date toDate = calendar.getTime();

        BookingFeatures bookingClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/booking"))
                .build(BookingFeatures.class);

        Response response = bookingClient.reserveBookByName("Harry", "Harry Potter", fromDate, toDate);

        assertEquals("Book : Harry Potter is successfully booked from "
                + fromDate.toString() + " to " + toDate.toString(), response.readEntity(String.class));

        Response response1 = bookingClient.reserveBookByName("Harry", "Harry Potter", fromDate, toDate);

        assertEquals("Book : Harry Potter is already booked by someone else ... ", response1.readEntity(String.class));
    }

    @Test
    public void reserveWrongBookName() throws URISyntaxException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JULY, 1);
        Date fromDate = calendar.getTime();

        calendar.set(2020, Calendar.JULY, 2);
        Date toDate = calendar.getTime();

        String wrongName = "wrongName";

        BookingFeatures bookingClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/booking"))
                .build(BookingFeatures.class);

        Response response = bookingClient.reserveBookByName("Harry", wrongName, fromDate, toDate);

        assertEquals(wrongName + " is not at the library", response.readEntity(String.class));
    }

    @Test
    public void reserveWrongCustomerName() throws URISyntaxException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JULY, 1);
        Date fromDate = calendar.getTime();

        calendar.set(2020, Calendar.JULY, 2);
        Date toDate = calendar.getTime();

        String wrongName = "wrongName";

        BookingFeatures bookingClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/booking"))
                .build(BookingFeatures.class);

        Response response = bookingClient.reserveBookByName(wrongName, "Harry Potter", fromDate, toDate);

        assertEquals(wrongName + " is not a customer of the library", response.readEntity(String.class));
    }
}
