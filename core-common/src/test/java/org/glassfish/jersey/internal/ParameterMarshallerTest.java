package org.glassfish.jersey.internal;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.ws.rs.core.Configuration;
import org.glassfish.jersey.internal.inject.CalendarConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Calendar.NOVEMBER;
import static java.util.Collections.singleton;
import static org.glassfish.jersey.internal.ParameterMarshaller.NO_ANNOTATIONS;
import static org.glassfish.jersey.internal.ParameterMarshaller.parameterMarshaller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

// Reproducer JERSEY-4315
@RunWith(MockitoJUnitRunner.class)
public class ParameterMarshallerTest {
    @Mock
    private Configuration configuration;
    @Spy
    private CalendarConverter calendarConverter = new CalendarConverter();

    @Before
    public void setUp() {
        when(configuration.getInstances()).thenReturn(singleton(calendarConverter));
    }

    @Test
    public void marshall_whenNullIsPassed() {
        Object actual = parameterMarshaller(configuration).marshall(null);

        assertNull(actual);

        verifyZeroInteractions(calendarConverter);
    }

    @Test
    public void marshall_whenStringIsPassed() {
        Object actual = parameterMarshaller(configuration).marshall("15-11-2019");

        assertEquals("15-11-2019", actual);

        InOrder inOrder = inOrder(calendarConverter);
        inOrder.verify(calendarConverter).getConverter(String.class, String.class, NO_ANNOTATIONS);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void marshall_whenDateIsPassed() {
        Date date = new GregorianCalendar(2019, NOVEMBER, 15).getTime();
        Object actual = parameterMarshaller(configuration).marshall(date);

        assertEquals("Fri Nov 15 00:00:00 CET 2019", actual);

        InOrder inOrder = inOrder(calendarConverter);
        inOrder.verify(calendarConverter).getConverter(Date.class, Date.class, NO_ANNOTATIONS);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void marshall_whenGregorianCalendarIsPassed() {
        GregorianCalendar calendar = new GregorianCalendar(2019, NOVEMBER, 15);
        Object actual = parameterMarshaller(configuration).marshall(calendar);

        assertEquals("15-11-2019", actual);

        InOrder inOrder = inOrder(calendarConverter);
        inOrder.verify(calendarConverter).getConverter(GregorianCalendar.class, GregorianCalendar.class, NO_ANNOTATIONS);
        inOrder.verify(calendarConverter).toString(calendar);
        inOrder.verifyNoMoreInteractions();
    }
}