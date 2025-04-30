package dev.gaau.login.controller.api;

import dev.gaau.login.dto.request.LoginRequestDto;
import dev.gaau.login.dto.request.SignUpRequestDto;
import dev.gaau.login.dto.response.LoginResponseDto;
import dev.gaau.login.dto.response.MemberResponseDto;
import dev.gaau.login.serivce.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/signUp")
    public ResponseEntity<MemberResponseDto> signUp(@RequestBody SignUpRequestDto request) {
        MemberResponseDto memberDto = memberService.join(request);

        return ResponseEntity.ok(memberDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request,
                                                  HttpServletRequest httpRequest) {
        LoginResponseDto loginDto = memberService.login(request, httpRequest);

        return ResponseEntity.ok(loginDto);
    }
}
