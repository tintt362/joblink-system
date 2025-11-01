package com.joblink.auth_service.service;

import com.joblink.auth_service.dto.request.*;
import com.joblink.auth_service.dto.response.*;
import com.joblink.auth_service.entity.InvalidatedToken;
import com.joblink.auth_service.entity.Role;
import com.joblink.auth_service.entity.User;
import com.joblink.auth_service.exceptions.AppException;
import com.joblink.auth_service.exceptions.ErrorCode;
import com.joblink.auth_service.repository.InvalidatedTokenRepository;
import com.joblink.auth_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;

import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthServiceIml implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;
    @Value("${jwt.signerKey}")
    private String signerKey;


    @NonFinal
    @Value("${jwt.valid-duration}")
    private int VALIDATION_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    private int REFRESHABLE_DURATION;

    @Transactional
    @Override
    public UserResponse register(RegisterRequestDto requestDto) {
        // check email account is exit
        if (userRepository.existsByEmailIgnoreCase(requestDto.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);

        }

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(Role.valueOf(requestDto.getRole().toUpperCase()))
                .enabled(false) // require email confirm
                .build();

        User userSaved = userRepository.save(user);
        UserResponse userResponse = UserResponse.builder()
                .id(String.valueOf(userSaved.getId()))
                .email(userSaved.getEmail())
                .createdAt(userSaved.getCreatedAt())
                .updatedAt(userSaved.getUpdatedAt())
                .build();
        return userResponse;
    }

    // 3. Cập nhật phương thức authenticate
    @Override
    public LoginResponse authenticate(LoginRequestDto loginRequestDto) {
        // ... check user/password (giữ nguyên) ...

        User user = userRepository.findByEmailIgnoreCase(loginRequestDto.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.CREDENTIALS_NOT_VALID));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.CREDENTIALS_NOT_VALID);
        }

        // TẠO CẢ HAI TOKEN
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // Trả về cả Refresh Token
                .build();
    }

    // 1. Hàm tạo Access Token (Dùng cho Request)
    public String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer("tintt362.com")
                .issueTime(new Date())
                // Thời hạn ngắn: VALIDATION_DURATION (ví dụ: 15 phút)
                .expirationTime(Date.from(Instant.now().plus(VALIDATION_DURATION, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("Role", user.getRole().toString()) // Chuyển enum sang String
                .build();

        // ... logic ký token ...
        try {
            byte[] secretKeyBytes = Base64.getDecoder().decode(signerKey);
            JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimsSet.toJSONObject()));
            jwsObject.sign(new MACSigner(secretKeyBytes));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    // 2. Hàm tạo Refresh Token (Dùng cho Gia hạn)
    public String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer("tintt362.com")
                .issueTime(new Date())
                // Thời hạn dài: REFRESHABLE_DURATION (ví dụ: 7 ngày)
                .expirationTime(Date.from(Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString()) // ID duy nhất
                // Refresh Token KHÔNG cần chứa Role (Claims tối giản)
                .build();

        // ... logic ký token (giống hệt Access Token) ...
        try {
            byte[] secretKeyBytes = Base64.getDecoder().decode(signerKey);
            JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimsSet.toJSONObject()));
            jwsObject.sign(new MACSigner(secretKeyBytes));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User provisionOAuthUser(String email, String provider, Object attributes) {
        return null;
    }


    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer("tintt362.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(VALIDATION_DURATION, ChronoUnit.SECONDS)))

                .jwtID(UUID.randomUUID().toString())
                .claim("Role", user.getRole())
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimsSet.toJSONObject()));

        try {
            byte[] secretKeyBytes = Base64.getDecoder().decode(signerKey);
            jwsObject.sign(new MACSigner(secretKeyBytes));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }


    // 4. Cập nhật phương thức verifyToken
// Bỏ tham số isRefresh=true, logic kiểm tra thời hạn là mặc định của JWT
    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        byte[] secretKeyBytes = Base64.getDecoder().decode(signerKey);
        JWSVerifier verifier = new MACVerifier(secretKeyBytes);
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 1. Kiểm tra chữ ký
        boolean verified = signedJWT.verify(verifier);

        // 2. Kiểm tra thời hạn (exp claim)
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 3. Kiểm tra thu hồi (Revocation)
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }


//    public void logout(LogoutRequest request) throws ParseException, JOSEException {
//        // kiểm tra token co hop le hay khong
//        var signToken = verifyToken(request.getToken(), true);
//        // vi sao lai true:
//        // vi khi ma logout roi, thi user van co the lay token do, dem di refresh lai token moi van dc
//        String jwtId = signToken.getJWTClaimsSet().getJWTID();
//        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
//        InvalidatedToken invalidatedToken = InvalidatedToken
//                .builder()
//                .id(jwtId)
//                .expiryTime(expiryTime)
//                .build();
//
//        invalidatedTokenRepository.save(invalidatedToken);
//    }

//    public IntrospectResponse introspect(IntrospectRequest request) {
//        var token = request.getToken();
//        boolean isValid = true;
//        try {
//            verifyToken(token, false);
//        } catch (ParseException e) {
//            isValid = false;
//        } catch (JOSEException e) {
//            isValid = false;
//        }
//        return IntrospectResponse.builder()
//                .valid(isValid)
//                .build();
//    }

    // 5. Cập nhật phương thức refreshToken
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        // 1. Verify Refresh Token
        var signJWT = verifyToken(request.getToken()); // Giờ đây nó chỉ verify dựa trên exp dài

        // 2. Thu hồi Refresh Token cũ (One-time use)
        var jwtId = signJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken
                .builder().id(jwtId).expiryTime(expiryTime).build();
        invalidatedTokenRepository.save(invalidatedToken);

        // 3. Issue CẶP Token mới
        var userId = signJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findById(Integer.valueOf(userId)).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

        //String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user); // Cấp Refresh Token MỚI

        // Trả về cả hai token mới (Giả định RefreshTokenResponse chứa cả hai)
        return RefreshTokenResponse.builder()
                .refreshToken(newRefreshToken)
                .build();
    }

    // AuthServiceImpl.java

    @Override
    public MeResponse getMe(String userId, String role) {
        // Token đã được verify bởi Spring Security, chỉ cần lấy user từ DB
        User user = userRepository.findById(Integer.valueOf(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return MeResponse.builder()
                .id(String.valueOf(user.getId()))
                .email(user.getEmail())
                .role(role) // Role đã có trong JWT
                .build();
    }
}
