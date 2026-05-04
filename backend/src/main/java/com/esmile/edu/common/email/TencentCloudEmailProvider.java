package com.esmile.edu.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Tencent Cloud email provider stub.
 * Currently not implemented - throws UnsupportedOperationException.
 */
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "tencent")
public class TencentCloudEmailProvider implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(TencentCloudEmailProvider.class);

    @Override
    public boolean sendVerificationCode(String to, String code) {
        throw new UnsupportedOperationException(
            "Tencent Cloud email provider is not yet implemented. " +
            "Please use 'mock' provider or implement the provider.");
    }

    @Override
    public String getProviderName() {
        return "tencent";
    }
}
