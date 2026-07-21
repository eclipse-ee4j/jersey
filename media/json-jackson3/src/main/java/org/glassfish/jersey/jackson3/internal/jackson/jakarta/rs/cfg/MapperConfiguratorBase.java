package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperBuilder;

/**
 * Helper class used to encapsulate details of configuring an
 * {@link ObjectMapper} instance to be used for data binding, as
 * well as accessing it.
 */
public abstract class MapperConfiguratorBase<IMPL extends MapperConfiguratorBase<IMPL,MAPPER>,
        MAPPER extends ObjectMapper
>
{
    private final ReentrantLock _lock = new ReentrantLock();

    /*
    /**********************************************************************
    /* Configuration, simple features
    /**********************************************************************
     */

    /**
     * {@link DeserializationFeature}s to explicitly enable or disable
     */
    protected EnumMap<MapperFeature, Boolean> _mapperFeatures;

    /**
     * {@link DeserializationFeature}s to explicitly enable or disable
     */
    protected EnumMap<DeserializationFeature, Boolean> _deserFeatures;

    /**
     * {@link SerializationFeature}s to explicitly enable or disable
     */
    protected EnumMap<SerializationFeature, Boolean> _serFeatures;

    /*
    /**********************************************************************
    /* Configuration, other
    /**********************************************************************
     */

    /**
     * {@code AnnotationIntrospector} to use as an override over default
     * {@code JacksonAnnotationIntrospector}, if any.
     *
     * @since 3.0
     */
    protected AnnotationIntrospector _instropectorOverride;

    /*
    /**********************************************************************
    /* Lazily constructed Mapper instance(s)
    /**********************************************************************
     */

    /**
     * Mapper provider was constructed with if any, or that was constructed
     * due to a call to explicitly configure mapper.
     * If defined (explicitly or implicitly) it will be used, instead
     * of using provider-based lookup.
     */
    protected MAPPER _mapper;

    /**
     * If no mapper was specified when constructed, and no configuration
     * calls are made, a default mapper is constructed. The difference
     * between default mapper and regular one is that default mapper
     * is only used if no mapper is found via provider lookup.
     */
    protected MAPPER _defaultMapper;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public MapperConfiguratorBase(MAPPER mapper,
                                  AnnotationIntrospector instropectorOverride)
    {
        _mapper = mapper;
        _instropectorOverride = instropectorOverride;
    }

    public MAPPER getDefaultMapper() {
        if (_defaultMapper == null) {
            _lock.lock();
            try {
                if (_defaultMapper == null) {
                    _defaultMapper = _mapperWithConfiguration(mapperBuilder());
                }
            } finally {
                _lock.unlock();
            }        }
        return _defaultMapper;
    }

    /**
     * Helper method that will ensure that there is a configurable non-default
     * mapper (constructing an instance if one didn't yet exit), and return
     * that mapper.
     */
    protected MAPPER mapper()
    {
        if (_mapper == null) {
            _lock.lock();
            try {
                if (_mapper == null) {
                    _mapper = _mapperWithConfiguration(mapperBuilder());
                }
            } finally {
                _lock.unlock();
            }
        }
        return _mapper;
    }

    /*
    /**********************************************************************
    /* Abstract methods to implement
    /**********************************************************************
     */

    /**
     * @since 3.0
     */
    protected abstract MapperBuilder<?,?> mapperBuilder();

    /*
    /**********************************************************************
    /* Configuration methods
    /**********************************************************************
     */

    /**
     * Method that locates, configures and returns {@link ObjectMapper} to use
     */
    public MAPPER getConfiguredMapper() {
        // important: should NOT call mapper(); needs to return null
        // if no instance has been passed or constructed
        return _mapper;
    }

    public final void setMapper(MAPPER m) {
        _mapper = m;
    }

    public final void setAnnotationIntrospector(AnnotationIntrospector aiOverride) {
        _instropectorOverride = aiOverride;
    }

    public final void configure(DeserializationFeature f, boolean state) {
        if (_deserFeatures == null) {
            _deserFeatures = new EnumMap<>(DeserializationFeature.class);
        }
        _deserFeatures.put(f, state);
    }

    public final void configure(SerializationFeature f, boolean state) {
        if (_serFeatures == null) {
            _serFeatures = new EnumMap<>(SerializationFeature.class);
        }
        _serFeatures.put(f, state);
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */

    /**
     * Helper method that will configure given builder using configured overrides.
     */
    @SuppressWarnings("unchecked")
    protected MAPPER _mapperWithConfiguration(MapperBuilder<?,?> mapperBuilder)
    {
        return (MAPPER) _builderWithConfiguration(mapperBuilder)
                .build();
    }

    /**
     * Overridable helper method that applies all configuration on given builder.
     */
    protected MapperBuilder<?,?> _builderWithConfiguration(MapperBuilder<?,?> mapperBuilder)
    {
        // First, AnnotationIntrospector settings
        if (_instropectorOverride != null) {
            mapperBuilder = mapperBuilder.annotationIntrospector(_instropectorOverride);
        }

        // Features?
        if (_mapperFeatures != null) {
            for (Map.Entry<MapperFeature,Boolean> entry : _mapperFeatures.entrySet()) {
                mapperBuilder = mapperBuilder.configure(entry.getKey(), entry.getValue());
            }
        }
        if (_serFeatures != null) {
            for (Map.Entry<SerializationFeature,Boolean> entry : _serFeatures.entrySet()) {
                mapperBuilder = mapperBuilder.configure(entry.getKey(), entry.getValue());
            }
        }
        if (_deserFeatures != null) {
            for (Map.Entry<DeserializationFeature,Boolean> entry : _deserFeatures.entrySet()) {
                mapperBuilder = mapperBuilder.configure(entry.getKey(), entry.getValue());
            }
        }

        // anything else?
        return mapperBuilder;
    }
}
