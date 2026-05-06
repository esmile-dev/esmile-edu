package com.esmile.edu.common.video;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory for obtaining the configured VideoStoragePort implementation.
 * Defaults to MockVideoProvider if no provider is configured.
 *
 * <p>Configuration: Set {@code video.provider} property to select provider.
 * Available providers: {@code mock} (default), {@code tencent}</p>
 */
@Component
public class VideoServiceFactory {

    private static final String DEFAULT_PROVIDER = "mock";

    private final ApplicationContext applicationContext;

    public VideoServiceFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Returns the VideoStoragePort based on configured provider.
     * Defaults to mock provider if not configured or provider bean not found.
     */
    public VideoStoragePort getVideoStorage() {
        String providerName = applicationContext.getEnvironment()
            .getProperty("video.provider", DEFAULT_PROVIDER);

        String beanName = providerName + "VideoProvider";

        if (applicationContext.containsBean(beanName)) {
            return applicationContext.getBean(beanName, VideoStoragePort.class);
        }

        // Fallback to mock if provider bean not found
        if (applicationContext.containsBean("mockVideoProvider")) {
            return applicationContext.getBean("mockVideoProvider", VideoStoragePort.class);
        }

        throw new IllegalStateException(
            "No VideoStoragePort implementation found. " +
            "Please configure a valid video.provider or ensure mockVideoProvider is available.");
    }
}
