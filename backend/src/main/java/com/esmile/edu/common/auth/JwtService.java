package com.esmile.edu.common.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Service using RSA asymmetric key pairs.
 * Keys are auto-generated on first startup and stored in the configured directory.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Value("${jwt.expiration}")
    private long expiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws IOException {
        Path privatePath = Paths.get(privateKeyPath);
        Path publicPath = Paths.get(publicKeyPath);

        // Create parent directories if needed
        Files.createDirectories(privatePath.getParent());
        Files.createDirectories(publicPath.getParent());

        // Generate or load keys
        if (Files.exists(privatePath) && Files.exists(publicPath)) {
            log.info("Loading existing RSA key pair from {} and {}", privateKeyPath, publicKeyPath);
            loadKeys(privatePath, publicPath);
        } else {
            log.info("Generating new RSA key pair at {} and {}", privateKeyPath, publicKeyPath);
            generateAndSaveKeys(privatePath, publicPath);
        }
    }

    private void loadKeys(Path privatePath, Path publicPath) throws IOException {
        try {
            String privateKeyPem = Files.readString(privatePath);
            String publicKeyPem = Files.readString(publicPath);

            privateKey = parsePrivateKey(removePemHeaders(privateKeyPem.trim()));
            publicKey = parsePublicKey(removePemHeaders(publicKeyPem.trim()));
        } catch (Exception e) {
            log.error("Failed to load RSA keys, regenerating: {}", e.getMessage());
            generateAndSaveKeys(privatePath, publicPath);
        }
    }

    private void generateAndSaveKeys(Path privatePath, Path publicPath) throws IOException {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();

            // Save in PEM format
            String privateKeyPem = toPem("PRIVATE KEY", Base64.getMimeEncoder().encode(privateKey.getEncoded()));
            String publicKeyPem = toPem("PUBLIC KEY", Base64.getMimeEncoder().encode(publicKey.getEncoded()));

            Files.writeString(privatePath, privateKeyPem);
            Files.writeString(publicPath, publicKeyPem);

            log.info("RSA key pair generated and saved successfully");
        } catch (Exception e) {
            throw new IOException("Failed to generate RSA key pair", e);
        }
    }

    private String removePemHeaders(String pem) {
        return pem.replace("-----BEGIN PRIVATE KEY-----", "")
                  .replace("-----END PRIVATE KEY-----", "")
                  .replace("-----BEGIN PUBLIC KEY-----", "")
                  .replace("-----END PUBLIC KEY-----", "")
                  .replaceAll("\\s", "");
    }

    private String toPem(String type, byte[] encoded) {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN ").append(type).append("-----\n");
        String encodedStr = Base64.getMimeEncoder().encodeToString(encoded);
        // Split into 64-character lines
        for (int i = 0; i < encodedStr.length(); i += 64) {
            sb.append(encodedStr, i, Math.min(i + 64, encodedStr.length())).append("\n");
        }
        sb.append("-----END ").append(type).append("-----\n");
        return sb.toString();
    }

    private PrivateKey parsePrivateKey(String base64) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return java.security.KeyFactory.getInstance("RSA").generatePrivate(
            new java.security.spec.PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey parsePublicKey(String base64) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return java.security.KeyFactory.getInstance("RSA").generatePublic(
            new java.security.spec.X509EncodedKeySpec(decoded));
    }

    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getExpiration() {
        return expiration;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
