package com.joblink.auth_service.config;

import com.joblink.auth_service.repository.InvalidatedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    // 💡 Chỉ cần Repository để kiểm tra Revoked Token
    private InvalidatedTokenRepository invalidatedTokenRepository;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        if (Objects.isNull(nimbusJwtDecoder)) {
            // ... logic khởi tạo nimbusJwtDecoder (giữ nguyên) ...
            byte[] secretKeyBytes = Base64.getDecoder().decode(signerKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }

        Jwt jwt = nimbusJwtDecoder.decode(token);

        // 1. Kiểm tra thu hồi (Revocation check)
        String jwtId = jwt.getId();
        if (jwtId != null && invalidatedTokenRepository.existsById(jwtId)) {
            throw new JwtException("Token has been revoked/logged out.");
        }

        // 2. Kiểm tra Claims (Đảm bảo ID và Role có mặt)
        if (!jwt.getClaims().containsKey("Role")) {
            throw new JwtException("Token missing required 'Role' claim.");
        }

        return jwt;
    }
}
