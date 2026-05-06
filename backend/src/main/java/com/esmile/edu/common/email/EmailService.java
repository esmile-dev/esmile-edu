package com.esmile.edu.common.email;

/**
 * Email service interface for sending verification codes.
 * Implementations: MockEmailProvider, TencentCloudEmailProvider, etc.
 */
public interface EmailService {

    /**
     * Sends a verification code to the specified email address.
     * @param to  The recipient email address
     * @param code The verification code to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendVerificationCode(String to, String code);

    /**
     * Returns the name of the email provider.
     */
    String getProviderName();
}
