/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

package org.glassfish.jersey.server.microprofile;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.function.BiFunction;

import javax.ws.rs.JAXRS;

import org.eclipse.microprofile.config.Config;
import org.glassfish.jersey.server.spi.Configurator;
import org.junit.Test;

/**
 * Unit tests for {@link MicroprofileConfigurator}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
public class MicroprofileConfiguratorTest {

    @Test
    @SuppressWarnings("unchecked")
    public final void shouldProvideGetOptionalValueAsPropertiesProviderToConfigurationBuilder() {
        // given
        final Configurator configurator = new MicroprofileConfigurator();
        final Config config = mock(Config.class);
        final JAXRS.Configuration.Builder configurationBuilder = mock(JAXRS.Configuration.Builder.class);
        given(configurationBuilder.from(any(BiFunction.class))).willAnswer(invocation -> {
            final BiFunction<String, Class<String>, Optional<?>> propertiesProvider =
                    invocation.getArgumentAt(0, BiFunction.class);
            propertiesProvider.apply("NAME", String.class);
            return invocation.getMock();
        });

        // when
        configurator.configure(configurationBuilder, config);

        // then
        verify(config).getOptionalValue("NAME", String.class);
    }

}
