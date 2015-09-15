package com.sparkweb.binding.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sparkweb.binding.AnnotationHelper;
import com.sparkweb.binding.TypeBinder;

/**
 * Binder that support Date class.
 */
public class DateBinder implements TypeBinder<Date> 
{
    public static final String ISO8601 = "'ISO8601:'yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String DEF_DATE_FORMAT = "yyyy-MM-dd";

    public Date bind(String name, Annotation[] annotations, String value, Class<?> actualClass, Type genericType) throws Exception 
    {
        if (value == null || value.trim().length() == 0) {
            return null;
        }

        Date date = AnnotationHelper.getDateAs(annotations, value);
        if (date != null) {
            return date;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DEF_DATE_FORMAT);
            sdf.setLenient(false);
            return sdf.parse(value);
        } catch (ParseException e) {
            // Ignore
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO8601);
            sdf.setLenient(false);
            return sdf.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert [" + value + "] to a Date: " + e.toString());
        }

    }
}
