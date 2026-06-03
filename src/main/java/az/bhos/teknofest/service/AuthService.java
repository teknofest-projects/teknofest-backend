package az.bhos.teknofest.service;

import az.bhos.teknofest.model.dto.auth.AuthResponseDto;
import az.bhos.teknofest.model.dto.auth.LoginRequestDto;
import az.bhos.teknofest.model.dto.auth.RegisterRequestDto;
import az.bhos.teknofest.model.dto.auth.VerifyRequestDto;
import az.bhos.teknofest.model.dto.shared.SuccessResponse;

public interface AuthService {
    SuccessResponse<Void> register(RegisterRequestDto userRegisterRequestDto);
    SuccessResponse<AuthResponseDto> login(LoginRequestDto userLoginRequestDto);
    SuccessResponse<AuthResponseDto> verify(VerifyRequestDto verifyRequestDto);
}
