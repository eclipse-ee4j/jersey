package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg;

import tools.jackson.databind.cfg.ConfigFeature;

/**
 * Enumeration that defines simple on/off features that can be
 * used on all Jackson Jakarta-RS providers, regardless of
 * underlying data format.
 */
public enum JakartaRSFeature implements ConfigFeature
{
    /*
    /**********************************************************************
    /* Input handling
    /**********************************************************************
     */

    /**
     * Feature to define whether empty input is considered legal or not.
     * If set to true, empty content is allowed and will be read as Java 'null': if false,
     * an {@link java.io.IOException} will be thrown.
     *<p>
     * NOTE: in case of Jakarta-RS 2.0, specific exception will be {@code jakarta.ws.rs.core.NoContentException},
     */
    ALLOW_EMPTY_INPUT(true),

    /**
     * For HTTP keep-alive or multipart content to work correctly, Jackson must read the entire HTTP input
     * stream up until reading EOF (-1).
     * <a href="https://github.com/FasterXML/jackson-jaxrs-providers/issues/108">Issue #108</a>
     * If set to true, always consume all input content. This has a side-effect of failing on trailing content.
     *<p>
     * Feature is enabled by default.
     * Note that this means that behavior in earlier versions
     * (2.14 and before) differs from 2.15 and later.
     *
     * @since 2.15
     */
    READ_FULL_STREAM(true),

    /*
    /**********************************************************************
    /* HTTP headers
    /**********************************************************************
     */

    /**
     * Feature that can be enabled to make provider automatically
     * add "nosniff" (see
     * <a href="http://security.stackexchange.com/questions/20413/how-can-i-prevent-reflected-xss-in-my-json-web-services">this entry</a>
     * for details
     *<p>
     * Feature is disabled by default.
     */
    ADD_NO_SNIFF_HEADER(false),

    /*
    /**********************************************************************
    /* Caching, related
    /**********************************************************************
     */

    /**
     * Feature that may be enabled to force dynamic lookup of <code>ObjectMapper</code>
     * via Jakarta-RS Provider interface, regardless of whether <code>MapperConfigurator</code>
     * has explicitly configured mapper or not; if disabled, static configuration will
     * take precedence.
     * Note that if this feature is enabled, it typically makes sense to also disable
     * {@link JakartaRSFeature#CACHE_ENDPOINT_READERS} and {@link JakartaRSFeature#CACHE_ENDPOINT_WRITERS}
     * since caching would prevent lookups.
     *<p>
     * Feature is disabled by default.
     */
    DYNAMIC_OBJECT_MAPPER_LOOKUP(false),

    /**
     * Feature that determines whether provider will cache endpoint
     * definitions for reading or not (including caching of actual <code>ObjectReader</code> to use).
     * Feature may be disabled if reconfiguration or alternate instance of <code>ObjectMapper</code> is needed.
     *<p>
     * Note that disabling of the feature may add significant amount of overhead for processing.
     *<p>
     * Feature is enabled by default.
     */
    CACHE_ENDPOINT_READERS(true),

    /**
     * Feature that determines whether provider will cache endpoint
     * definitions for writing or not (including caching of actual <code>ObjectWriter</code> to use).
     * Feature may be disabled if reconfiguration or alternate instance of <code>ObjectMapper</code> is needed.
     *<p>
     * Note that disabling of the feature may add significant amount of overhead for processing.
     *<p>
     * Feature is enabled by default.
     */
    CACHE_ENDPOINT_WRITERS(true),

    /*
    /**********************************************************************
    /* Other
    /**********************************************************************
     */

    /**
     * [jaxrs-providers#162]: Feature that determines whether
     * {@code hasMatchingMediaType()} will return {@code true} or {@code false}
     * in cases where no Media-Type header passed.
     *<p>
     * NOTE: effective default before 3.1 was {@code true} but with 3.1 changes
     * to explicit {@code false}, since this is considered likelier acceptable
     * default setting.
     *
     * @since 3.1
     */
    MATCH_ALL_IF_NO_MEDIA_TYPE(false)
    ;

    private final boolean _defaultState;

    private JakartaRSFeature(boolean defaultState) {
        _defaultState = defaultState;
    }

    public static int collectDefaults() {
        int flags = 0;
        for (JakartaRSFeature f : values()) {
            if (f.enabledByDefault()) { flags |= f.getMask(); }
        }
        return flags;
    }

    @Override
    public boolean enabledByDefault() { return _defaultState; }

    @Override
    public int getMask() { return (1 << ordinal()); }

    @Override
    public boolean enabledIn(int flags) { return (flags & getMask()) != 0; }
}
