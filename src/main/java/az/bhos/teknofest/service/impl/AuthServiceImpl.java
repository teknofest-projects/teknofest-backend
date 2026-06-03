package az.bhos.teknofest.service.impl;

import static az.bhos.teknofest.utils.RedisUtils.registerKey;
import static az.bhos.teknofest.utils.common.ErrorConstants.*;
import static az.bhos.teknofest.utils.common.EventConstants.USERNAME;
import static az.bhos.teknofest.utils.common.EventConstants.VERIFICATION_CODE;
import static az.bhos.teknofest.utils.generators.OTPGenerator.generateSixDigitOTP;
import static org.springframework.security.core.userdetails.User.withUsername;

import az.bhos.teknofest.handler.exception.ApplicationException;
import az.bhos.teknofest.handler.exception.InvalidCredentialsException;
import az.bhos.teknofest.handler.exception.ResourceNotFoundException;
import az.bhos.teknofest.model.dto.auth.AuthResponseDto;
import az.bhos.teknofest.model.dto.auth.LoginRequestDto;
import az.bhos.teknofest.model.dto.auth.RegisterRequestDto;
import az.bhos.teknofest.model.dto.auth.VerifyRequestDto;
import az.bhos.teknofest.model.dto.shared.SuccessResponse;
import az.bhos.teknofest.model.entity.User;
import az.bhos.teknofest.model.event.NotificationEvent;
import az.bhos.teknofest.model.redis.CachedVerificationData;
import az.bhos.teknofest.repository.UserRepository;
import az.bhos.teknofest.security.JwtService;
import az.bhos.teknofest.service.AuthService;
import az.bhos.teknofest.service.RedisService;
import az.bhos.teknofest.utils.enums.NotificationType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;

    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public SuccessResponse<Void> register(RegisterRequestDto registerRequestDto) {
        String email = registerRequestDto.getEmail();
        log.info("register started for user: {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApplicationException(EMAIL_ALREADY_EXISTS);
        }

        String password = passwordEncoder.encode(registerRequestDto.getPassword());
        String code = generateSixDigitOTP();

        CachedVerificationData cachedVerificationData = CachedVerificationData.builder()
                .hashedToken(passwordEncoder.encode(code))
                .username(registerRequestDto.getUsername())
                .userEmail(email)
                .hashedPassword(password)
                .attemptCount(0)
                .expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .build();

        redisService.set(
                registerKey(email),
                cachedVerificationData,
                Duration.ofMinutes(VERIFICATION_CODE_EXPIRY_MINUTES)
        );

        applicationEventPublisher.publishEvent(new NotificationEvent(
                email,
                NotificationType.VERIFICATION_CODE,
                Map.of(
                        USERNAME, registerRequestDto.getUsername(),
                        VERIFICATION_CODE, code
                )
        ));

        return SuccessResponse.of("verification code sent to your email successfully!");
    }

    @Override
    public SuccessResponse<AuthResponseDto> verify(VerifyRequestDto verifyRequestDto) {
        String email = verifyRequestDto.getEmail();
        CachedVerificationData cachedVerificationData = redisService
                .get(registerKey(email), CachedVerificationData.class)
                .orElseThrow(() -> new ResourceNotFoundException(PENDING_VERIFICATION_NOT_FOUND));

        if (cachedVerificationData.getExpiryDate().isBefore(LocalDateTime.now())) {
            redisService.delete(registerKey(email));
            throw new InvalidCredentialsException(VERIFICATION_TOKEN_EXPIRED);
        }

        if (!passwordEncoder.matches(verifyRequestDto.getCode(), cachedVerificationData.getHashedToken())) {
            cachedVerificationData.setAttemptCount(cachedVerificationData.getAttemptCount() + 1);
            if (cachedVerificationData.getAttemptCount() >= MAX_FAILED_ATTEMPTS) {
                redisService.delete(registerKey(email));
                throw new InvalidCredentialsException(INVALID_VERIFICATION_TOKEN);
            }

            long remaining = Duration.between(LocalDateTime.now(), cachedVerificationData.getExpiryDate()).toMinutes();
            redisService.set(
                    registerKey(email),
                    cachedVerificationData,
                    Duration.ofMinutes(Math.max(remaining, 1))
            );

            throw new InvalidCredentialsException(INVALID_VERIFICATION_TOKEN);
        }

        var user = User.builder()
                .name(cachedVerificationData.getUsername())
                .email(cachedVerificationData.getUserEmail())
                .password(cachedVerificationData.getHashedPassword())
                .build();
        userRepository.save(user);

        redisService.delete(registerKey(email));

        return SuccessResponse.of(generateAuthResponse(user), "account verified successfully");
    }

    @Override
    public SuccessResponse<AuthResponseDto> login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        log.info("login started for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, loginRequestDto.getPassword()));

        return SuccessResponse.of(generateAuthResponse(user), "login successfully!");
    }

    private AuthResponseDto generateAuthResponse(User user) {
        var UserDetails = withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getUserRole().name())
                .build();

        var accessToken = jwtService.generateToken(UserDetails);
        var refreshToken = jwtService.generateRefreshToken(UserDetails);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
