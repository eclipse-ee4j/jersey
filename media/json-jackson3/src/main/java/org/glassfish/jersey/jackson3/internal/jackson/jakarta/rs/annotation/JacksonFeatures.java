package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;

/**
 * Annotation that can be used enable and/or disable various
 * features for <code>ObjectReader</code>s and <code>ObjectWriter</code>s.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@com.fasterxml.jackson.annotation.JacksonAnnotation
public @interface JacksonFeatures
{
    /**
     * Deserialization features to enable.
     */
    public DeserializationFeature[] deserializationEnable() default { };

    /**
     * Deserialization features to disable.
     */
    public DeserializationFeature[] deserializationDisable() default { };

    /**
     * Serialization features to enable.
     */
    public SerializationFeature[] serializationEnable() default { };

    /**
     * Serialization features to disable.
     */
    public SerializationFeature[] serializationDisable() default { };
}
