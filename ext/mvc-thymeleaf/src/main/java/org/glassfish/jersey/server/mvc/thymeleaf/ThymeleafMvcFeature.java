package org.glassfish.jersey.server.mvc.thymeleaf;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import org.glassfish.jersey.server.mvc.MvcFeature;

@ConstrainedTo(RuntimeType.SERVER)
public final class ThymeleafMvcFeature implements Feature {
    private static final String SUFFIX = ".thymeleaf";
    public static final String TEMPLATE_BASE_PATH = MvcFeature.TEMPLATE_BASE_PATH + SUFFIX;
    public static final String ENCODING = MvcFeature.ENCODING + SUFFIX;

    @Override
    public boolean configure(FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(ThymeleafViewProcessor.class)) {
            context.register(ThymeleafViewProcessor.class);

            // MvcFeature.
            if (!config.isRegistered(MvcFeature.class)) {
                context.register(MvcFeature.class);
            }

            return true;
        }
        return false;
    }
}
