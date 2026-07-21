package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.json;

import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.MapperConfiguratorBase;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * Helper class used to encapsulate details of configuring an
 * {@link ObjectMapper} instance to be used for data binding, as
 * well as accessing it.
 */
public class JsonMapperConfigurator
        extends MapperConfiguratorBase<JsonMapperConfigurator, JsonMapper>
{
    public JsonMapperConfigurator(JsonMapper mapper,
                                  AnnotationIntrospector aiOverride)
    {
        super(mapper, aiOverride);
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    protected MapperBuilder<?,?> mapperBuilder() {
        return JsonMapper.builder();
    }
}

