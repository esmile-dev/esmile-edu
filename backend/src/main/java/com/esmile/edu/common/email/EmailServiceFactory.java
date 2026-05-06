package com.esmile.edu.common.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory for obtaining the configured EmailService implementation.
 * Falls back to MockEmailProvider if no provider is configured.
 */
@Component
public class EmailServiceFactory {

    private static final String DEFAULT_PROVIDER = "mock";

    private final ApplicationContext applicationContext;

    public EmailServiceFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Returns the EmailService based on configured provider.
     * Defaults to mock provider if not configured or provider bean not found.
     */
    public EmailService getEmailService() {
        String providerName = applicationContext.getEnvironment()
            .getProperty("email.provider", DEFAULT_PROVIDER);

        String beanName = providerName + "EmailProvider";

        if (applicationContext.containsBean(beanName)) {
            return applicationContext.getBean(beanName, EmailService.class);
        }

        // Fallback to mock if provider bean not found
        if (applicationContext.containsBean("mockEmailProvider")) {
            return applicationContext.getBean("mockEmailProvider", EmailService.class);
        }

        throw new IllegalStateException(
            "No EmailService implementation found. " +
            "Please configure a valid email.provider or ensure mockEmailProvider is available.");
    }
}
