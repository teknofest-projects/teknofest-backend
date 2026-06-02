package az.bhos.teknofest.service.impl;

import az.bhos.teknofest.model.dto.auth.AuthResponseDto;
import az.bhos.teknofest.model.dto.auth.LoginRequestDto;
import az.bhos.teknofest.model.dto.auth.RegisterRequestDto;
import az.bhos.teknofest.model.dto.shared.SuccessResponse;
import az.bhos.teknofest.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public SuccessResponse<AuthResponseDto> register(RegisterRequestDto userRegisterRequestDto) {
        return null;
    }

    @Override
    public SuccessResponse<AuthResponseDto> login(LoginRequestDto userLoginRequestDto) {
        return null;
    }
}
