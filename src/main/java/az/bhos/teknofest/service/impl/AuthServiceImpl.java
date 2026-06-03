package az.bhos.teknofest.service.impl;

import static az.bhos.teknofest.utils.common.ErrorConstants.EMAIL_ALREADY_EXISTS;
import static az.bhos.teknofest.utils.common.ErrorConstants.USER_NOT_FOUND;
import static org.springframework.security.core.userdetails.User.withUsername;

import az.bhos.teknofest.handler.exception.ApplicationException;
import az.bhos.teknofest.handler.exception.ResourceNotFoundException;
import az.bhos.teknofest.model.dto.auth.AuthResponseDto;
import az.bhos.teknofest.model.dto.auth.LoginRequestDto;
import az.bhos.teknofest.model.dto.auth.RegisterRequestDto;
import az.bhos.teknofest.model.dto.shared.SuccessResponse;
import az.bhos.teknofest.model.entity.User;
import az.bhos.teknofest.repository.UserRepository;
import az.bhos.teknofest.security.JwtService;
import az.bhos.teknofest.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public SuccessResponse<AuthResponseDto> register(RegisterRequestDto registerRequestDto) {
        String email = registerRequestDto.getEmail();
        log.info("register started for user: {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApplicationException(EMAIL_ALREADY_EXISTS);
        }

        String password = passwordEncoder.encode(registerRequestDto.getPassword());

        User user = User.builder()
                .name(registerRequestDto.getName())
                .email(email)
                .password(password)
                .build();
        userRepository.save(user);

        return SuccessResponse.of(generateAuthResponse(user), "register successfully!");
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
