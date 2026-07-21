package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NoContentException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.AnnotationBundleKey;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.EndpointConfigBase;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.JakartaRSFeature;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.MapperConfiguratorBase;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectReaderInjector;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectReaderModifier;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectWriterInjector;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectWriterModifier;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.base.util.ClassKey;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.Versioned;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.util.LookupCache;
import tools.jackson.databind.type.TypeFactory;
import tools.jackson.databind.util.SimpleLookupCache;

public abstract class ProviderBase<
    THIS extends ProviderBase<THIS, MAPPER, EP_CONFIG, MAPPER_CONFIG>,
    MAPPER extends ObjectMapper,
    EP_CONFIG extends EndpointConfigBase<EP_CONFIG>,
    MAPPER_CONFIG extends MapperConfiguratorBase<MAPPER_CONFIG,MAPPER>
>
    implements
        MessageBodyReader<Object>,
        MessageBodyWriter<Object>,
        Versioned
{
    /**
     * This header is useful on Windows, trying to deal with potential XSS attacks.
     */
    public final static String HEADER_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    private final static String NO_CONTENT_MESSAGE = "No content (empty input stream)";

    /**
     * Looks like we need to worry about accidental
     *   data binding for types we shouldn't be handling. This is
     *   probably not a very good way to do it, but let's start by
     *   blacklisting things we are not to handle.
     *<p>
     *  (why ClassKey? since plain old Class has no hashCode() defined,
     *  lookups are painfully slow)
     */
    protected final static HashSet<ClassKey> DEFAULT_UNTOUCHABLES = new HashSet<>();
    static {
        // First, I/O things (direct matches)
        DEFAULT_UNTOUCHABLES.add(new ClassKey(java.io.InputStream.class));
        DEFAULT_UNTOUCHABLES.add(new ClassKey(java.io.Reader.class));
        DEFAULT_UNTOUCHABLES.add(new ClassKey(java.io.OutputStream.class));
        DEFAULT_UNTOUCHABLES.add(new ClassKey(java.io.Writer.class));

        // then some primitive types
        DEFAULT_UNTOUCHABLES.add(new ClassKey(char[].class));

        // 27-Apr-2012, tatu: Ugh. As per issue #12 (in old tracker)
        //  better revert this back, to make them untouchable again.
        DEFAULT_UNTOUCHABLES.add(new ClassKey(String.class));
        DEFAULT_UNTOUCHABLES.add(new ClassKey(byte[].class));
    }

    /**
     * These are classes that we never use for reading
     * (never try to deserialize instances of these types).
     */
    public final static Class<?>[] DEFAULT_UNREADABLES = new Class<?>[] {
        InputStream.class, Reader.class
    };

    /**
     * These are classes that we never use for writing
     * (never try to serialize instances of these types).
     */
    public final static Class<?>[] DEFAULT_UNWRITABLES = new Class<?>[] {
        InputStream.class, // as per [Issue#19]
        OutputStream.class, Writer.class,
        StreamingOutput.class, Response.class
    };

    protected final static int JAKARTA_RS_FEATURE_DEFAULTS = JakartaRSFeature.collectDefaults();

    /*
    /**********************************************************************
    /* General configuration
    /**********************************************************************
     */

    /**
     * Helper object used for encapsulating configuration aspects
     * of {@link ObjectMapper}
     */
    protected final MAPPER_CONFIG _mapperConfig;

    /**
     * Map that contains overrides to default list of untouchable
     * types: <code>true</code> meaning that entry is untouchable,
     * <code>false</code> that is is not.
     */
    protected HashMap<ClassKey,Boolean> _cfgCustomUntouchables;

    /**
     * Feature flags set.
     */
    protected int _jakartaRSFeatures;

    /**
     * View to use for reading if none defined for the end point.
     */
    protected Class<?> _defaultReadView;

    /**
     * View to use for writing if none defined for the end point.
     */
    protected Class<?> _defaultWriteView;

    /*
    /**********************************************************************
    /* Excluded types
    /**********************************************************************
     */

    public final static HashSet<ClassKey> _untouchables = DEFAULT_UNTOUCHABLES;

    public final static Class<?>[] _unreadableClasses = DEFAULT_UNREADABLES;

    public final static Class<?>[] _unwritableClasses = DEFAULT_UNWRITABLES;

    /*
    /**********************************************************************
    /* Bit of caching
    /**********************************************************************
     */

    /**
     * Cache for resolved endpoint configurations when reading JSON data
     */
    protected final LookupCache<AnnotationBundleKey, EP_CONFIG> _readers;

    /**
     * Cache for resolved endpoint configurations when writing JSON data
     */
    protected final LookupCache<AnnotationBundleKey, EP_CONFIG> _writers;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected ProviderBase(MAPPER_CONFIG mconfig) {
        this(mconfig,
                new SimpleLookupCache<>(16, 120),
                new SimpleLookupCache<>(16, 120));
    }

    /**
     * Constructor that is only added to resolve problems
     * with combination of RESTeasy and CDI.
     * Should NOT be used by any code explicitly; only exists
     * for proxy support.
     */
    @Deprecated // just to denote it should NOT be directly called; will NOT be removed
    protected ProviderBase() {
        this(null);
    }

    /**
     * @since 2.17
     */
    protected ProviderBase(MAPPER_CONFIG mconfig,
                           LookupCache<AnnotationBundleKey, EP_CONFIG> readerCache,
                           LookupCache<AnnotationBundleKey, EP_CONFIG> writerCache
    ) {
        _mapperConfig = mconfig;
        _jakartaRSFeatures = JAKARTA_RS_FEATURE_DEFAULTS;
        _readers = readerCache;
        _writers = writerCache;
    }

    /*
    /**********************************************************************
    /* Configuring
    /**********************************************************************
     */

    /**
     * Method for marking specified type as "untouchable", meaning that provider
     * will not try to read or write values of this type (or its subtypes).
     *
     * @param type Type to consider untouchable; can be any kind of class,
     *   including abstract class or interface. No instance of this type
     *   (including subtypes, i.e. types assignable to this type) will
     *   be read or written by provider
     */
    public void addUntouchable(Class<?> type)
    {
        if (_cfgCustomUntouchables == null) {
            _cfgCustomUntouchables = new HashMap<ClassKey,Boolean>();
        }
        _cfgCustomUntouchables.put(new ClassKey(type), Boolean.TRUE);
    }

    /**
     * Method for removing definition of specified type as untouchable:
     * usually only
     */
    public void removeUntouchable(Class<?> type)
    {
        if (_cfgCustomUntouchables == null) {
            _cfgCustomUntouchables = new HashMap<ClassKey,Boolean>();
        }
        _cfgCustomUntouchables.put(new ClassKey(type), Boolean.FALSE);
    }

    /**
     * Method for overriding {@link AnnotationIntrospector} to use instead of
     * default {@code JacksonAnnotationIntrospector}: often used to add
     * JAXB-backed introspector.
     *
     * @param aiOverride AnnotationIntrospector to configure mapper to use
     *    ({@code null} to leave it as default one)
     */
    public void setAnnotationsToUse(AnnotationIntrospector aiOverride) {
        _mapperConfig.setAnnotationIntrospector(aiOverride);
    }

    /**
     * Method that can be used to directly define {@link ObjectMapper} to use
     * for serialization and deserialization; if null, will use the standard
     * provider discovery from context instead. Default setting is null.
     */
    public void setMapper(MAPPER m) {
        _mapperConfig.setMapper(m);
    }

    /**
     * Method for specifying JSON View to use for reading content
     * when end point does not have explicit View annotations.
     */
    public THIS setDefaultReadView(Class<?> view) {
        _defaultReadView = view;
        return _this();
    }

    /**
     * Method for specifying JSON View to use for reading content
     * when end point does not have explicit View annotations.
     */
    public THIS setDefaultWriteView(Class<?> view) {
        _defaultWriteView = view;
        return _this();
    }

    /**
     * Method for specifying JSON View to use for reading and writing content
     * when end point does not have explicit View annotations.
     * Functionally equivalent to:
     *<code>
     *  setDefaultReadView(view);
     *  setDefaultWriteView(view);
     *</code>
     */
    public THIS setDefaultView(Class<?> view) {
        _defaultReadView = _defaultWriteView = view;
        return _this();
    }

    // // // JakartaRSFeature config

    public THIS configure(JakartaRSFeature feature, boolean state) {
        return state ? enable(feature) : disable(feature);
    }

    public THIS enable(JakartaRSFeature feature) {
        _jakartaRSFeatures |= feature.getMask();
        return _this();
    }

    public THIS enable(JakartaRSFeature first, JakartaRSFeature... f2) {
        _jakartaRSFeatures |= first.getMask();
        for (JakartaRSFeature f : f2) {
            _jakartaRSFeatures |= f.getMask();
        }
        return _this();
    }

    public THIS disable(JakartaRSFeature feature) {
        _jakartaRSFeatures &= ~feature.getMask();
        return _this();
    }

    public THIS disable(JakartaRSFeature first, JakartaRSFeature... f2) {
        _jakartaRSFeatures &= ~first.getMask();
        for (JakartaRSFeature f : f2) {
            _jakartaRSFeatures &= ~f.getMask();
        }
        return _this();
    }

    public boolean isEnabled(JakartaRSFeature f) {
        return (_jakartaRSFeatures & f.getMask()) != 0;
    }

    // // // DeserializationFeature

    public THIS configure(DeserializationFeature f, boolean state) {
        _mapperConfig.configure(f, state);
        return _this();
    }

    public THIS enable(DeserializationFeature f) {
        _mapperConfig.configure(f, true);
        return _this();
    }

    public THIS disable(DeserializationFeature f) {
        _mapperConfig.configure(f, false);
        return _this();
    }

    // // // SerializationFeature

    public THIS configure(SerializationFeature f, boolean state) {
        _mapperConfig.configure(f, state);
        return _this();
    }

    public THIS enable(SerializationFeature f) {
        _mapperConfig.configure(f, true);
        return _this();
    }

    public THIS disable(SerializationFeature f) {
        _mapperConfig.configure(f, false);
        return _this();
    }

    /*
    /**********************************************************************
    /* Abstract methods sub-classes need to implement
    /**********************************************************************
     */

    /**
     * Helper method used to check whether given media type
     * is supported by this provider for read operations
     * (when binding input data such as POST body).
     *<p>
     * Default implementation simply calls {@link #hasMatchingMediaType}.
     */
    protected boolean hasMatchingMediaTypeForReading(MediaType mediaType) {
        return hasMatchingMediaType(mediaType);
    }

    /**
     * Helper method used to check whether given media type
     * is supported by this provider for writing operations,
     * such as when converting response object to response
     * body of request (like GET or POST).
     *<p>
     * Default implementation simply calls {@link #hasMatchingMediaType}.
     */
    protected boolean hasMatchingMediaTypeForWriting(MediaType mediaType) {
        return hasMatchingMediaType(mediaType);
    }

    /**
     * Helper method used to check whether given media type
     * is supported by this provider.
     */
    protected abstract boolean hasMatchingMediaType(MediaType mediaType);

    /**
     * Helper method that is called if no mapper has been explicitly configured.
     */
    protected abstract MAPPER _locateMapperViaProvider(Class<?> type, MediaType mediaType);

    protected EP_CONFIG _configForReading(MAPPER mapper,
                                          Annotation[] annotations, Class<?> defaultView)
    {
//        ObjectReader r = _readerInjector.getAndClear();
        ObjectReader r;
        if (defaultView != null) {
            r = mapper.readerWithView(defaultView);
        } else {
            r = mapper.reader();
        }
        // 25-Jan-2020, tatu: Important: JAX-RS expects that the InputStream
        //   is NOT closed by parser so...
        r = r.without(StreamReadFeature.AUTO_CLOSE_SOURCE);
        if (JakartaRSFeature.READ_FULL_STREAM.enabledIn(_jakartaRSFeatures)) {
            r = r.withFeatures(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        }
        return _configForReading(r, annotations);
    }

    protected EP_CONFIG _configForWriting(MAPPER mapper,
                                          Annotation[] annotations, Class<?> defaultView)
    {
//        ObjectWriter w = _writerInjector.getAndClear();
        ObjectWriter w;
        if (defaultView != null) {
            w = mapper.writerWithView(defaultView);
        } else {
            w = mapper.writer();
        }
        // 25-Jan-2020, tatu: Important: JAX-RS expects that the OutputStream
        //   is NOT closed by generator so...
        w = w.without(StreamWriteFeature.AUTO_CLOSE_TARGET);
        return _configForWriting(w, annotations);
    }

    protected abstract EP_CONFIG _configForReading(ObjectReader reader,
                                                   Annotation[] annotations);

    protected abstract EP_CONFIG _configForWriting(ObjectWriter writer,
                                                   Annotation[] annotations);

    /*
    /**********************************************************************
    /* Partial MessageBodyWriter impl
    /**********************************************************************
     */

    /**
     * Method that Jakarta-RS container calls to try to figure out
     * serialized length of given value. Since computation of
     * this length is about as expensive as serialization itself,
     * implementation will return -1 to denote "not known", so
     * that container will determine length from actual serialized
     * output (if needed).
     */
    @Override
    public long getSize(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        // In general figuring output size requires actual writing; usually not
        // worth it to write everything twice.
        return -1;
    }

    /**
     * Method that Jakarta-RS container calls to try to check whether
     * given value (of specified type) can be serialized by
     * this provider.
     * Implementation will first check that expected media type is
     * expected one (by call to {@link #hasMatchingMediaType}); then verify
     * that type is not one of "untouchable" types.
     *<p>
     * NOTE: in 2.x also checked (if configured) for "canSerialize()", but
     * with 3.x that is not done since there that detection never worked
     * in a useful manner.
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        if (!hasMatchingMediaType(mediaType)) {
            return false;
        }
        Boolean customUntouchable = _findCustomUntouchable(type);
        if (customUntouchable != null) {
            // negation: Boolean.TRUE means untouchable -> can not write
            return !customUntouchable.booleanValue();
        }
        // Ok: looks like we must weed out some core types here; ones that
        // make no sense to try to bind from JSON:
        if (_isIgnorableForWriting(new ClassKey(type))) {
            return false;
        }
        // but some are interface/abstract classes, so
        for (Class<?> cls : _unwritableClasses) {
            if (cls.isAssignableFrom(type)) {
                return false;
            }
        }
        // 29-Jan-2018, tatu: Mapper does not really know, a priori, much about what might not
        //   be something it can serialize. Jackson 2.x did have `canSerialize()` method which didn't
        //   do much good; 3.x does not.
        /*
        if (_cfgCheckCanSerialize) {
            if (!locateMapper(type, mediaType).canSerialize(type)) {
                return false;
            }
        }
        */
        return true;
    }

    /**
     * Method that Jakarta-RS container calls to serialize given value.
     */
    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String,Object> httpHeaders, OutputStream entityStream)
            throws JacksonException
    {
        EP_CONFIG endpoint = _endpointForWriting(value, type, genericType, annotations,
                mediaType, httpHeaders);

        // Any headers we should write?
        _modifyHeaders(value, type, genericType, annotations, httpHeaders, endpoint);

        ObjectWriter writer = endpoint.getWriter();

        // Where can we find desired encoding? Within HTTP headers?
        JsonEncoding enc = findEncoding(mediaType, httpHeaders);
        JavaType rootType = null;

        if ((genericType != null) && (value != null)) {
            // 10-Jan-2011, tatu: as per [JACKSON-456], it's not safe to just force root
            //    type since it prevents polymorphic type serialization. Since we really
            //    just need this for generics, let's only use generic type if it's truly generic.

            if (!(genericType instanceof Class<?>)) { // generic types are other impls of 'java.lang.reflect.Type'
                // This is still not exactly right; should root type be further
                // specialized with 'value.getClass()'? Let's see how well this works before
                // trying to come up with more complete solution.

                // 18-Mar-2015, tatu: As per [#60], there is now a problem with non-polymorphic lists,
                //    since forcing of type will then force use of content serializer, which is
                //    generally not the intent. Fix may require addition of functionality in databind

                TypeFactory typeFactory = writer.typeFactory();
                JavaType baseType = typeFactory.constructType(genericType);
                rootType = typeFactory.constructSpecializedType(baseType, type);
                // 26-Feb-2011, tatu: To help with [JACKSON-518], we better recognize cases where
                //    type degenerates back into "Object.class" (as is the case with plain TypeVariable,
                //    for example), and not use that.
                if (rootType.getRawClass() == Object.class) {
                    rootType = null;
                }
            }
        }

        // Most of the configuration now handled through EndpointConfig, ObjectWriter
        // but we may need to force root type:
        if (rootType != null) {
            writer = writer.forType(rootType);
        }
        // Some end points decorate value: for JSON we may want JSONP wrapping for example
        value = endpoint.modifyBeforeWrite(value);

        // [jaxrs-providers#32]: allow modification by filter-injectible thing
        ObjectWriterModifier mod = ObjectWriterInjector.getAndClear();
        if (mod != null) {
            writer = mod.modify(endpoint, httpHeaders, value, writer);
        }

        try (JsonGenerator g = _createGenerator(writer, entityStream, enc)) {
            writer.writeValue(g, value);
        }
    }

    /**
     * Helper method to use for determining desired output encoding.
     * For now, will always just use UTF-8...
     */
    protected JsonEncoding findEncoding(MediaType mediaType, MultivaluedMap<String,Object> httpHeaders)
    {
        return JsonEncoding.UTF8;
    }

    /**
     * Overridable method used for adding optional response headers before
     * serializing response object.
     */
    protected void _modifyHeaders(Object value, Class<?> type, Type genericType, Annotation[] annotations,
                                  MultivaluedMap<String,Object> httpHeaders,
                                  EP_CONFIG endpoint)
            throws JacksonException
    {
        // Add "nosniff" header?
        if (isEnabled(JakartaRSFeature.ADD_NO_SNIFF_HEADER)) {
            httpHeaders.add(HEADER_CONTENT_TYPE_OPTIONS, "nosniff");
        }
    }

    /**
     * Overridable helper method called to create a {@link JsonGenerator} for writing
     * contents into given raw {@link OutputStream}.
     */
    protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc)
            throws JacksonException
    {
        // Note: disabling of AUTO_CLOSE_TARGET should have happened earlier
        return writer.createGenerator(rawStream, enc);
    }

    protected EP_CONFIG _endpointForWriting(Object value, Class<?> type, Type genericType,
                                            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String,Object> httpHeaders)
    {
        // 29-Jun-2016, tatu: allow skipping caching
        if (!isEnabled(JakartaRSFeature.CACHE_ENDPOINT_WRITERS)) {
            return _configForWriting(locateMapper(type, mediaType), annotations, _defaultWriteView);
        }

        AnnotationBundleKey key = new AnnotationBundleKey(annotations, type);
        EP_CONFIG endpoint = _writers.get(key);
        // not yet resolved (or not cached any more)? Resolve!
        if (endpoint == null) {
            MAPPER mapper = locateMapper(type, mediaType);
            endpoint = _configForWriting(mapper, annotations, _defaultWriteView);
            // and cache for future reuse
            _writers.put(key.immutableKey(), endpoint);
        }
        return endpoint;
    }

    /*
    /**********************************************************************
    /* MessageBodyReader impl
    /**********************************************************************
     */

    /**
     * Method that Jakarta-RS container calls to try to check whether
     * values of given type (and media type) can be deserialized by
     * this provider.
     * Implementation will first check that expected media type is
     * a JSON type (via call to {@link #hasMatchingMediaType});
     * then verify
     * that type is not one of "untouchable" types (types we will never
     * automatically handle).
     *<p>
     * NOTE: in 2.x also checked (if configured) for "canDeserialize()", but
     * with 3.x that is not done since there that detection never worked
     * in a useful manner.
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        if (!hasMatchingMediaType(mediaType)) {
            return false;
        }
        Boolean customUntouchable = _findCustomUntouchable(type);
        if (customUntouchable != null) {
            // negation: Boolean.TRUE means untouchable -> can not write
            return !customUntouchable.booleanValue();
        }
        // Ok: looks like we must weed out some core types here; ones that
        // make no sense to try to bind from JSON:
        if (_isIgnorableForReading(new ClassKey(type))) {
            return false;
        }
        // and there are some other abstract/interface types to exclude too:
        for (Class<?> cls : _unreadableClasses) {
            if (cls.isAssignableFrom(type)) {
                return false;
            }
        }
        // 29-Jan-2018, tatu: Mapper does not really know, a priori, much about what might not
        //   be something it can serialize. Jackson 2.x did have `canSerialize()` method which didn't
        //   do much good; 3.x does not.
        /*
        // Finally: if we really want to verify that we can serialize, we'll check:
        if (_cfgCheckCanDeserialize) {
            if (_isSpecialReadable(type)) {
                return true;
            }
            ObjectMapper mapper = locateMapper(type, mediaType);
            if (!mapper.canDeserialize(mapper.constructType(type))) {
                return false;
            }
        }
        */
        return true;
    }

    /**
     * Method that Jakarta-RS container calls to deserialize given value.
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String,String> httpHeaders,
                           InputStream entityStream)
            throws JacksonException,
            NoContentException
    {
        EP_CONFIG endpoint = _endpointForReading(type, genericType, annotations,
                mediaType, httpHeaders);

        ObjectReader reader = endpoint.getReader();
        JsonParser p = _createParser(reader, entityStream);

        // If null is returned, considered to be empty stream
        if (p == null || p.nextToken() == null) {
            if (JakartaRSFeature.ALLOW_EMPTY_INPUT.enabledIn(_jakartaRSFeatures)) {
                return null;
            }
            throw _createNoContentException();
        }
        Class<?> rawType = type;
        if (rawType == JsonParser.class) {
            return p;
        }
        final TypeFactory tf = reader.typeFactory();
        final JavaType resolvedType = tf.constructType(genericType);

        // 09-Jul-2015, tatu: As per [jaxrs-providers#69], handle MappingIterator too
        boolean multiValued = (rawType == MappingIterator.class);

        if (multiValued) {
            JavaType[] contents = tf.findTypeParameters(resolvedType, MappingIterator.class);
            JavaType valueType = (contents == null || contents.length == 0)
                    ? tf.constructType(Object.class) : contents[0];
            reader = reader.forType(valueType);
        } else {
            reader = reader.forType(resolvedType);
        }

        // Allow modification by filter-injectable thing
        ObjectReaderModifier mod = ObjectReaderInjector.getAndClear();
        if (mod != null) {
            reader = mod.modify(endpoint, httpHeaders, resolvedType, reader, p);
        }

        if (multiValued) {
            return reader.readValues(p);
        }
        return reader.readValue(p);
    }

    /**
     * Overridable helper method called to create a {@link JsonParser} for reading
     * contents of given raw {@link InputStream}.
     * May return null to indicate that Stream is empty; that is, contains no
     * content.
     */
    protected JsonParser _createParser(ObjectReader reader, InputStream rawStream)
            throws JacksonException
    {
        // Note: disabling of AUTO_CLOSE_SOURCE should have happened earlier
        // so can just construct and return parser as-is
        return reader.createParser(rawStream);
    }

    /**
     * Overridable helper method that will basically fetch representation of the
     * endpoint that can be used to get {@link ObjectReader} to use for deserializing
     * content
     */
    protected EP_CONFIG _endpointForReading(Class<Object> type, Type genericType, Annotation[] annotations,
                                            MediaType mediaType, MultivaluedMap<String,String> httpHeaders)
    {
        // Allow skipping caching
        if (!isEnabled(JakartaRSFeature.CACHE_ENDPOINT_READERS)) {
            return _configForReading(locateMapper(type, mediaType), annotations, _defaultReadView);
        }

        AnnotationBundleKey key = new AnnotationBundleKey(annotations, type);
        EP_CONFIG endpoint = _readers.get(key);
        // not yet resolved (or not cached any more)? Resolve!
        if (endpoint == null) {
            MAPPER mapper = locateMapper(type, mediaType);
            endpoint = _configForReading(mapper, annotations, _defaultReadView);
            // and cache for future reuse
            _readers.put(key.immutableKey(), endpoint);
        }
        return endpoint;
    }

    /*
    /**********************************************************************
    /* Overridable helper methods
    /**********************************************************************
     */

    /**
     * Method called to locate {@link ObjectMapper} to use for serialization
     * and deserialization. Exact logic depends on setting of
     * {@link JakartaRSFeature#DYNAMIC_OBJECT_MAPPER_LOOKUP}.
     *
     *<p>
     * If {@link JakartaRSFeature#DYNAMIC_OBJECT_MAPPER_LOOKUP} is disabled (default
     * setting unless changed), behavior is as follows:
     *<ol>
     * <li>If an instance has been explicitly defined by
     * {@link #setMapper} (or non-null instance passed in constructor), that
     * will be used.
     *  </li>
     * <li>If not, will try to locate it using standard Jakarta-RS
     * <code>ContextResolver</code> mechanism, if it has been properly configured
     * to access it (by Jakarta-RS runtime).
     *  </li>
     * <li>Finally, if no mapper is found, will return a default unconfigured
     * {@link ObjectMapper} instance (one constructed with default constructor
     * and not modified in any way)
     *   </li>
     *</ol>
     *<p>
     * If {@link JakartaRSFeature#DYNAMIC_OBJECT_MAPPER_LOOKUP} is enabled, steps
     * 1 and 2 are reversed, such that Jakarta-RS <code>ContextResolver</code>
     * is first used, and only if none is defined will configured mapper be used.
     *
     * @param type Class of object being serialized or deserialized;
     *   not checked at this point, since it is assumed that unprocessable
     *   classes have been already weeded out,
     *   but will be passed to <code>ContextResolver</code> as is.
     * @param mediaType Declared media type for the instance to process:
     *   not used by this method,
     *   but will be passed to <code>ContextResolver</code> as is.
     */
    public MAPPER locateMapper(Class<?> type, MediaType mediaType)
    {
        // 29-Jun-2016, tatu: As per [jaxrs-providers#86] may want to do provider lookup first
        if (isEnabled(JakartaRSFeature.DYNAMIC_OBJECT_MAPPER_LOOKUP)) {
            MAPPER m = _locateMapperViaProvider(type, mediaType);
            if (m == null) {
                m = (MAPPER) _mapperConfig.getConfiguredMapper();
                if (m == null) {
                    m = _mapperConfig.getDefaultMapper();
                }
            }
            return m;
        }

        // Otherwise start with (pre-)configured Mapper and only check provider
        // if not found

        MAPPER m = _mapperConfig.getConfiguredMapper();
        if (m == null) {
            // If not, maybe we can get one configured via context?
            m = _locateMapperViaProvider(type, mediaType);
            if (m == null) {
                // If not, let's get the fallback default instance
                m = _mapperConfig.getDefaultMapper();
            }
        }
        return m;
    }

    /**
     * Overridable helper method used to allow handling of somewhat special
     * types for reading
     */
    protected boolean _isSpecialReadable(Class<?> type) {
        return JsonParser.class == type;
    }

    /**
     * Overridable helper method called to check whether given type is a known
     * "ignorable type" (in context of reading), values of which are not bound
     * from content.
     */
    protected boolean _isIgnorableForReading(ClassKey typeKey)
    {
        return _untouchables.contains(typeKey);
    }

    /**
     * Overridable helper method called to check whether given type is a known
     * "ignorable type" (in context of reading), values of which
     * can not be written out.
     */
    protected boolean _isIgnorableForWriting(ClassKey typeKey)
    {
        return _untouchables.contains(typeKey);
    }

    protected NoContentException _createNoContentException() {
        return new NoContentException(NO_CONTENT_MESSAGE);
    }

    /*
    /**********************************************************************
    /* Private/sub-class helper methods
    /**********************************************************************
     */

    protected static boolean _containedIn(Class<?> mainType, HashSet<ClassKey> set)
    {
        if (set != null) {
            ClassKey key = new ClassKey(mainType);
            // First: type itself?
            if (set.contains(key)) return true;
            // Then supertypes (note: will not contain Object.class)
            for (Class<?> cls : findSuperTypes(mainType, null)) {
                key.reset(cls);
                if (set.contains(key)) return true;
            }
        }
        return false;
    }

    protected Boolean _findCustomUntouchable(Class<?> mainType)
    {
        if (_cfgCustomUntouchables != null) {
            ClassKey key = new ClassKey(mainType);
            // First: type itself?
            Boolean b = _cfgCustomUntouchables.get(key);
            if (b != null) {
                return b;
            }
            // Then supertypes (note: will not contain Object.class)
            for (Class<?> cls : findSuperTypes(mainType, null)) {
                key.reset(cls);
                b = _cfgCustomUntouchables.get(key);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    protected static List<Class<?>> findSuperTypes(Class<?> cls, Class<?> endBefore)
    {
        return findSuperTypes(cls, endBefore, new ArrayList<Class<?>>(8));
    }

    protected static List<Class<?>> findSuperTypes(Class<?> cls, Class<?> endBefore, List<Class<?>> result)
    {
        _addSuperTypes(cls, endBefore, result, false);
        return result;
    }

    protected static void _addSuperTypes(Class<?> cls, Class<?> endBefore, Collection<Class<?>> result, boolean addClassItself)
    {
        if (cls == endBefore || cls == null || cls == Object.class) {
            return;
        }
        if (addClassItself) {
            if (result.contains(cls)) { // already added, no need to check supers
                return;
            }
            result.add(cls);
        }
        for (Class<?> intCls : cls.getInterfaces()) {
            _addSuperTypes(intCls, endBefore, result, true);
        }
        _addSuperTypes(cls.getSuperclass(), endBefore, result, true);
    }

    @SuppressWarnings("unchecked")
    private final THIS _this() {
        return (THIS) this;
    }
}
