package dev.gaau.login.serivce;

import dev.gaau.login.domain.Member;
import dev.gaau.login.dto.request.LoginRequestDto;
import dev.gaau.login.dto.request.SignUpRequestDto;
import dev.gaau.login.dto.response.LoginResponseDto;
import dev.gaau.login.dto.response.MemberResponseDto;
import dev.gaau.login.jwt.JwtUtil;
import dev.gaau.login.mapper.MemberMapper;
import dev.gaau.login.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberResponseDto join(SignUpRequestDto request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);

        Member member = memberMapper.signUpRequestDtoToMember(request);
        Member savedMember = memberRepository.save(member);

        return memberMapper.memberToMemberResponseDto(savedMember);
    }

}
