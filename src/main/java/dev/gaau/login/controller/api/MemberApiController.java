package dev.gaau.login.controller.api;

import dev.gaau.login.dto.request.LoginRequestDto;
import dev.gaau.login.dto.request.SignUpRequestDto;
import dev.gaau.login.dto.response.TokenResponseDto;
import dev.gaau.login.dto.response.MemberResponseDto;
import dev.gaau.login.serivce.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/signUp")
    public ResponseEntity<MemberResponseDto> signUp(@RequestBody SignUpRequestDto request) {
        MemberResponseDto memberDto = memberService.join(request);

        return ResponseEntity.ok(memberDto);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto request,
                                                  HttpServletRequest httpRequest,
                                                  HttpServletResponse httpResponse) {
        TokenResponseDto loginDto = memberService.login(request, httpRequest, httpResponse);

        return ResponseEntity.ok(loginDto);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> verifyRefreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {

        try {
            TokenResponseDto tokenResponseDto = memberService.verifyRefreshToken(httpRequest, httpResponse);
            return ResponseEntity.ok(tokenResponseDto);
        } catch (RuntimeException e) {
            Map<String, String> body = new HashMap<>();
            body.put("code", "INVALID_REFRESH_TOKEN");
            body.put("message", "Refresh token is invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

    }
}
