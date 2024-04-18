package org.glassfish.jersey.server.mvc.thymeleaf;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class ThymeleafDefaultConfigurationFactory implements ThymeleafConfigurationFactory {
    private final TemplateEngine templateEngine;

    public ThymeleafDefaultConfigurationFactory() {
        this.templateEngine = initTemplateEngine();
    }

    @Override
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    private ITemplateResolver getTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheTTLMs(3600000L);
        return templateResolver;
    }

    private TemplateEngine initTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(getTemplateResolver());
        return templateEngine;
    }

    private IMessageResolver getMessageResolver() {
        StandardMessageResolver messageResolver = new StandardMessageResolver();
        return messageResolver;
    }
}
