package org.glassfish.jersey.server.mvc.thymeleaf;

import org.thymeleaf.TemplateEngine;

public class ThymeleafSuppliedConfigurationFactory implements ThymeleafConfigurationFactory {
    private final ThymeleafConfigurationFactory configurationFactory;

    public ThymeleafSuppliedConfigurationFactory(ThymeleafConfigurationFactory configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    @Override
    public TemplateEngine getTemplateEngine() {
        return configurationFactory.getTemplateEngine();
    }

}
