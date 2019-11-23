package org.glassfish.jersey.client;

import org.glassfish.jersey.internal.inject.CalendarConverter;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static java.util.Calendar.NOVEMBER;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.ParameterMarshaller.parameterMarshaller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

// Reproducer JERSEY-4315
public class ParameterMarshallerTest {
    private CalendarConverter calendarConverter = new CalendarConverter();
    private ClientConfig clientConfig;

    @Before
    public void setUp() throws Exception {
        clientConfig = (ClientConfig) newClient(new ClientConfig().register(calendarConverter)).getConfiguration();
    }

    @Test
    public void marshall_whenNullIsPassed() {
        assertNull(parameterMarshaller(clientConfig).marshall(null));
    }

    @Test
    public void marshall_whenStringIsPassed() {
        Object actual = parameterMarshaller(clientConfig).marshall("15-11-2019");

        assertEquals("15-11-2019", actual);
    }

    @Test
    public void marshall_whenDateIsPassed() {
        Date date = new GregorianCalendar(2019, NOVEMBER, 15).getTime();
        Object actual = parameterMarshaller(clientConfig).marshall(date);

        assertEquals("Fri Nov 15 00:00:00 CET 2019", actual);
    }

    @Test
    public void marshall_whenGregorianCalendarIsPassed() {
        GregorianCalendar calendar = new GregorianCalendar(2019, NOVEMBER, 15);
        Object actual = parameterMarshaller(clientConfig).marshall(calendar);

        assertEquals("15-11-2019", actual);
    }
}