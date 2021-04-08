/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.logging;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.CommonProperties;

/**
 * This feature enables logging request and/or response on client-side and/or server-side depending
 * on the context's {@link RuntimeType}.
 * <p>
 * The feature may be register programmatically like other features by calling any of {@link javax.ws.rs.core.Configurable}
 * {@code register(...)} method, i.e. {@link javax.ws.rs.core.Configurable#register(Class)} or by setting any of the
 * configuration property listed bellow.
 * <p>
 * Common configurable properties applies for both client and server and are following:
 * <ul>
 * <li>{@link #LOGGING_FEATURE_LOGGER_NAME}</li>
 * <li>{@link #LOGGING_FEATURE_LOGGER_LEVEL}</li>
 * <li>{@link #LOGGING_FEATURE_VERBOSITY}</li>
 * <li>{@link #LOGGING_FEATURE_MAX_ENTITY_SIZE}</li>
 * <li>{@link #LOGGING_FEATURE_SEPARATOR}</li>
 * </ul>
 * <p>
 * If any of the configuration value is not set, following default values are applied:
 * <ul>
 * <li>logger name: {@code org.glassfish.jersey.logging.LoggingFeature}</li>
 * <li>logger level: {@link Level#FINE}</li>
 * <li>verbosity: {@link Verbosity#PAYLOAD_TEXT}</li>
 * <li>maximum entity size: {@value #DEFAULT_MAX_ENTITY_SIZE}</li>
 * </ul>
 * <p>
 * Server configurable properties:
 * <ul>
 * <li>{@link #LOGGING_FEATURE_LOGGER_NAME_SERVER}</li>
 * <li>{@link #LOGGING_FEATURE_LOGGER_LEVEL_SERVER}</li>
 * <li>{@link #LOGGING_FEATURE_VERBOSITY_SERVER}</li>
 * <li>{@link #LOGGING_FEATURE_MAX_ENTITY_SIZE_SERVER}</li>
 * <li>{@link #LOGGING_FEATURE_SEPARATOR_SERVER}</li>
 * </ul>
 * Client configurable properties:
 * <ul>
 * <li>{@link #LOGGING_FEATURE_LOGGER_NAME_CLIENT}</li>
 * <li>{@link #LOGGING_FEATURE_LOGGER_LEVEL_CLIENT}</li>
 * <li>{@link #LOGGING_FEATURE_VERBOSITY_CLIENT}</li>
 * <li>{@link #LOGGING_FEATURE_MAX_ENTITY_SIZE_CLIENT}</li>
 * <li>{@link #LOGGING_FEATURE_SEPARATOR_CLIENT}</li>
 * </ul>
 *
 * @author Ondrej Kosatka
 * @since 2.23
 */
public class LoggingFeature implements Feature {

    /**
     * Default logger name to log request and response messages.
     */
    public static final String DEFAULT_LOGGER_NAME = LoggingFeature.class.getName();
    /**
     * Default logger level which will be used for logging request and response messages.
     */
    public static final String DEFAULT_LOGGER_LEVEL = Level.FINE.getName();
    /**
     * Default maximum entity bytes to be logged.
     */
    public static final int DEFAULT_MAX_ENTITY_SIZE = 8 * 1024;
    /**
     * Default verbosity for entity logging. See {@link Verbosity}.
     */
    public static final Verbosity DEFAULT_VERBOSITY = Verbosity.PAYLOAD_TEXT;
    /**
     * Default separator for entity logging.
     */
    public static final String DEFAULT_SEPARATOR = "\n";

    private static final String LOGGER_NAME_POSTFIX = ".logger.name";
    private static final String LOGGER_LEVEL_POSTFIX = ".logger.level";
    private static final String VERBOSITY_POSTFIX = ".verbosity";
    private static final String MAX_ENTITY_POSTFIX = ".entity.maxSize";
    private static final String SEPARATOR_POSTFIX = ".separator";
    private static final String LOGGING_FEATURE_COMMON_PREFIX = "jersey.config.logging";
    /**
     * Common logger name property.
     */
    public static final String LOGGING_FEATURE_LOGGER_NAME = LOGGING_FEATURE_COMMON_PREFIX + LOGGER_NAME_POSTFIX;
    /**
     * Common logger level property.
     */
    public static final String LOGGING_FEATURE_LOGGER_LEVEL = LOGGING_FEATURE_COMMON_PREFIX + LOGGER_LEVEL_POSTFIX;
    /**
     * Common property for configuring a verbosity of entity.
     */
    public static final String LOGGING_FEATURE_VERBOSITY = LOGGING_FEATURE_COMMON_PREFIX + VERBOSITY_POSTFIX;
    /**
     * Common property for configuring a maximum number of bytes of entity to be logged.
     */
    public static final String LOGGING_FEATURE_MAX_ENTITY_SIZE = LOGGING_FEATURE_COMMON_PREFIX + MAX_ENTITY_POSTFIX;
    /**
     * Common property for configuring logging separator.
     */
    public static final String LOGGING_FEATURE_SEPARATOR = LOGGING_FEATURE_COMMON_PREFIX + SEPARATOR_POSTFIX;

    private static final String LOGGING_FEATURE_SERVER_PREFIX = "jersey.config.server.logging";
    /**
     * Server logger name property.
     */
    public static final String LOGGING_FEATURE_LOGGER_NAME_SERVER = LOGGING_FEATURE_SERVER_PREFIX + LOGGER_NAME_POSTFIX;
    /**
     * Server logger level property.
     */
    public static final String LOGGING_FEATURE_LOGGER_LEVEL_SERVER = LOGGING_FEATURE_SERVER_PREFIX + LOGGER_LEVEL_POSTFIX;
    /**
     * Server property for configuring a verbosity of entity.
     */
    public static final String LOGGING_FEATURE_VERBOSITY_SERVER = LOGGING_FEATURE_SERVER_PREFIX + VERBOSITY_POSTFIX;
    /**
     * Server property for configuring a maximum number of bytes of entity to be logged.
     */
    public static final String LOGGING_FEATURE_MAX_ENTITY_SIZE_SERVER = LOGGING_FEATURE_SERVER_PREFIX + MAX_ENTITY_POSTFIX;
    /**
     * Server property for configuring separator.
     */
    public static final String LOGGING_FEATURE_SEPARATOR_SERVER = LOGGING_FEATURE_SERVER_PREFIX + SEPARATOR_POSTFIX;

    private static final String LOGGING_FEATURE_CLIENT_PREFIX = "jersey.config.client.logging";
    /**
     * Client logger name property.
     */
    public static final String LOGGING_FEATURE_LOGGER_NAME_CLIENT = LOGGING_FEATURE_CLIENT_PREFIX + LOGGER_NAME_POSTFIX;
    /**
     * Client logger level property.
     */
    public static final String LOGGING_FEATURE_LOGGER_LEVEL_CLIENT = LOGGING_FEATURE_CLIENT_PREFIX + LOGGER_LEVEL_POSTFIX;
    /**
     * Client property for configuring a verbosity of entity.
     */
    public static final String LOGGING_FEATURE_VERBOSITY_CLIENT = LOGGING_FEATURE_CLIENT_PREFIX + VERBOSITY_POSTFIX;
    /**
     * Client property for configuring a maximum number of bytes of entity to be logged.
     */
    public static final String LOGGING_FEATURE_MAX_ENTITY_SIZE_CLIENT = LOGGING_FEATURE_CLIENT_PREFIX + MAX_ENTITY_POSTFIX;
    /**
     * Client property for logging separator.
     */
    public static final String LOGGING_FEATURE_SEPARATOR_CLIENT = LOGGING_FEATURE_CLIENT_PREFIX + SEPARATOR_POSTFIX;

    private final LoggingFeatureBuilder builder;

    /**
     * Creates the feature with default values.
     */
    public LoggingFeature() {
        this(null, null, null, null);
    }

    /**
     * Creates the feature with custom logger.
     *
     * @param logger the logger to log requests and responses.
     */
    public LoggingFeature(Logger logger) {
        this(logger, null, null, null);
    }

    /**
     * Creates the feature with custom logger and verbosity.
     *
     * @param logger    the logger to log requests and responses.
     * @param verbosity verbosity of logged messages. See {@link Verbosity}.
     */
    public LoggingFeature(Logger logger, Verbosity verbosity) {
        this(logger, null, verbosity, null);
    }

    /**
     * Creates the feature with custom logger and maximum number of bytes of entity to log.
     *
     * @param logger        the logger to log requests and responses.
     * @param maxEntitySize maximum number of entity bytes to be logged (and buffered) - if the entity is larger,
     *                      logging filter will print (and buffer in memory) only the specified number of bytes
     *                      and print "...more..." string at the end. Negative values are interpreted as zero.
     */
    public LoggingFeature(Logger logger, Integer maxEntitySize) {
        this(logger, null, DEFAULT_VERBOSITY, maxEntitySize);
    }

    /**
     * Creates the feature with custom logger, it's level, message verbosity and maximum number of bytes of entity to log.
     *
     * @param logger        the logger to log requests and responses.
     * @param level         level on which the messages will be logged.
     * @param verbosity     verbosity of logged messages. See {@link Verbosity}.
     * @param maxEntitySize maximum number of entity bytes to be logged (and buffered) - if the entity is larger,
     *                      logging filter will print (and buffer in memory) only the specified number of bytes
     *                      and print "...more..." string at the end. Negative values are interpreted as zero.
     */
    public LoggingFeature(Logger logger, Level level, Verbosity verbosity, Integer maxEntitySize) {

        this(LoggingFeature.builder()
                .withLogger(logger)
                .level(level)
                .verbosity(verbosity)
                .maxEntitySize(maxEntitySize)
                .separator(DEFAULT_SEPARATOR)
        );

    }

    /**
     * Constructor based on logging feature builder. All parameters are passed through a builder instance.
     *
     * @param builder instance of a builder with required logging feature parameters
     */
    public LoggingFeature(LoggingFeatureBuilder builder) {
        this.builder = builder;
    }

    @Override
    public boolean configure(FeatureContext context) {
        boolean enabled = context.getConfiguration().getRuntimeType() != null;

        if (enabled) {
            context.register(createLoggingFilter(context, context.getConfiguration().getRuntimeType()));
        }

        return enabled;
    }

    /**
     * builder method to create  LoggingFeature with required settings
     *
     * @return Builder for LoggingFeature
     */
    public static LoggingFeatureBuilder builder() {
        return new LoggingFeatureBuilder();
    }

    private LoggingInterceptor createLoggingFilter(FeatureContext context, RuntimeType runtimeType) {

        final LoggingFeatureBuilder loggingBuilder =
                configureBuilderParameters(builder, context, runtimeType);

        return (runtimeType == RuntimeType.SERVER)
                ? new ServerLoggingFilter(loggingBuilder)
                : new ClientLoggingFilter(loggingBuilder);
    }

    private static LoggingFeatureBuilder configureBuilderParameters(LoggingFeatureBuilder builder,
                                                   FeatureContext context, RuntimeType runtimeType) {

        final Map properties = context.getConfiguration().getProperties();
        //get values from properties (if any)
        final String filterLoggerName = CommonProperties.getValue(
                properties,
                runtimeType == RuntimeType.SERVER ? LOGGING_FEATURE_LOGGER_NAME_SERVER : LOGGING_FEATURE_LOGGER_NAME_CLIENT,
                CommonProperties.getValue(
                        properties,
                        LOGGING_FEATURE_LOGGER_NAME,
                        DEFAULT_LOGGER_NAME
                ));
        final String filterLevel = CommonProperties.getValue(
                properties,
                runtimeType == RuntimeType.SERVER ? LOGGING_FEATURE_LOGGER_LEVEL_SERVER : LOGGING_FEATURE_LOGGER_LEVEL_CLIENT,
                CommonProperties.getValue(
                        context.getConfiguration().getProperties(),
                        LOGGING_FEATURE_LOGGER_LEVEL,
                        DEFAULT_LOGGER_LEVEL));
        final String filterSeparator = CommonProperties.getValue(
                properties,
                runtimeType == RuntimeType.SERVER ? LOGGING_FEATURE_SEPARATOR_SERVER : LOGGING_FEATURE_SEPARATOR_CLIENT,
                CommonProperties.getValue(
                        context.getConfiguration().getProperties(),
                        LOGGING_FEATURE_SEPARATOR,
                        DEFAULT_SEPARATOR));
        final Verbosity filterVerbosity = CommonProperties.getValue(
                properties,
                runtimeType == RuntimeType.SERVER ? LOGGING_FEATURE_VERBOSITY_SERVER : LOGGING_FEATURE_VERBOSITY_CLIENT,
                CommonProperties.getValue(
                        properties,
                        LOGGING_FEATURE_VERBOSITY,
                        DEFAULT_VERBOSITY
                ));
        int filterMaxEntitySize = CommonProperties.getValue(
                properties,
                runtimeType == RuntimeType.SERVER ? LOGGING_FEATURE_MAX_ENTITY_SIZE_SERVER
                        : LOGGING_FEATURE_MAX_ENTITY_SIZE_CLIENT,
                CommonProperties.getValue(
                        properties,
                        LOGGING_FEATURE_MAX_ENTITY_SIZE,
                        DEFAULT_MAX_ENTITY_SIZE
                ));

        final Level loggerLevel = Level.parse(filterLevel);

        //configure builder vs properties values
        builder.filterLogger = builder.filterLogger == null ? Logger.getLogger(filterLoggerName) : builder.filterLogger;
        builder.verbosity = builder.verbosity == null ? filterVerbosity : builder.verbosity;
        builder.maxEntitySize = builder.maxEntitySize == null ? filterMaxEntitySize : builder.maxEntitySize;
        builder.level = builder.level == null ? loggerLevel : builder.level;
        builder.separator = builder.separator == null ? filterSeparator : builder.separator;

        return builder;
    }

    /**
     * {@code Verbosity} determines how detailed message will be logged.
     * <p>
     * <ul>
     * <li>The lowest verbosity ({@link #HEADERS_ONLY}) will log only request/response headers.</li>
     * <li>
     * The medium verbosity will log request/response headers, as well as an entity if considered a readable text. See {@link
     * #PAYLOAD_TEXT}.
     * </li>
     * <li>The highest verbosity will log all types of an entity (besides the request/response headers.</li>
     * </ul>
     * <p>
     * Note that the entity is logged up to the maximum number specified in any of the following constructors {@link
     * LoggingFeature#LoggingFeature(Logger, Integer)}, {@link LoggingFeature#LoggingFeature(Logger, Level, Verbosity, Integer)}
     * or by some of the feature's properties (see {@link #LOGGING_FEATURE_MAX_ENTITY_SIZE}, {@link
     * #LOGGING_FEATURE_MAX_ENTITY_SIZE_CLIENT}, {@link #LOGGING_FEATURE_MAX_ENTITY_SIZE_SERVER}.
     */
    public enum Verbosity {
        /**
         * Only content of HTTP headers is logged. No message payload data are logged.
         */
        HEADERS_ONLY,
        /**
         * Content of HTTP headers as well as entity content of textual media types is logged. Following is the list of media
         * types that are considered textual for the logging purposes:
         * <ul>
         * <li>{@code text/*}</li>
         * <li>{@code application/atom+xml}</li>
         * <li>{@code application/json}</li>
         * <li>{@code application/svg+xml}</li>
         * <li>{@code application/x-www-form-urlencoded}</li>
         * <li>{@code application/xhtml+xml}</li>
         * <li>{@code application/xml}</li>
         * </ul>
         */
        PAYLOAD_TEXT,
        /**
         * Full verbose logging. Content of HTTP headers as well as any message payload content will be logged.
         */
        PAYLOAD_ANY
    }

    /**
     * Builder class for logging feature configuration. Accepts parameters for the filter logger, verbosity, max
     * entity size, level, and separator.
     */
    public static class LoggingFeatureBuilder {

        Logger filterLogger;
        Verbosity verbosity;
        Integer maxEntitySize;
        Level level;
        String separator;

        public LoggingFeatureBuilder() {

        }
        public LoggingFeatureBuilder withLogger(Logger logger) {
            this.filterLogger = logger;
            return this;
        }
        public LoggingFeatureBuilder verbosity(Verbosity verbosity) {
            this.verbosity = verbosity;
            return this;
        }
        public LoggingFeatureBuilder maxEntitySize(Integer maxEntitySize) {
            this.maxEntitySize = maxEntitySize;
            return this;
        }
        public LoggingFeatureBuilder level(Level level) {
            this.level = level;
            return this;
        }
        public LoggingFeatureBuilder separator(String separator) {
            this.separator = separator;
            return this;
        }

        public LoggingFeature build() {
            return new LoggingFeature(this);
        }
    }
}