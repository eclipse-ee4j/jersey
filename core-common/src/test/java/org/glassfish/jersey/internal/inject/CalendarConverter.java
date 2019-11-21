package org.glassfish.jersey.internal.inject;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarConverter implements ParamConverter<Calendar>, ParamConverterProvider {
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    public Calendar fromString(String value) {
        try {
            Date date = formatter.parse(value);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString(Calendar calendar) {
        return formatter.format(calendar.getTime());
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (Calendar.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) this;
        }
        return null;
    }
}
