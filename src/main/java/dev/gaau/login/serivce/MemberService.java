package dev.gaau.login.serivce;

import dev.gaau.login.domain.Member;
import dev.gaau.login.dto.request.LoginRequestDto;
import dev.gaau.login.dto.request.SignUpRequestDto;
import dev.gaau.login.dto.request.VerifyRefreshTokenRequestDto;
import dev.gaau.login.dto.response.MemberResponseDto;
import dev.gaau.login.dto.response.TokenResponseDto;
import dev.gaau.login.jwt.JwtUtil;
import dev.gaau.login.mapper.MemberMapper;
import dev.gaau.login.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public MemberResponseDto join(SignUpRequestDto request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);

        Member member = memberMapper.signUpRequestDtoToMember(request);
        Member savedMember = memberRepository.save(member);

        return memberMapper.memberToMemberResponseDto(savedMember);
    }

    public TokenResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest) {

        String username = request.getUsername();
        String password = request.getPassword();

        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("Member not found with username: " + username)
        );

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("Wrong password for " + username);
        }

        String accessToken = jwtUtil.createAccessToken(member.getId(), httpRequest.getRequestURI());
        String refreshToken = jwtUtil.createRefreshToken(member.getId(), httpRequest.getRequestURI());

        member.setRefreshToken(refreshToken);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public Optional<MemberResponseDto> findById(Long id) {
        return memberRepository.findById(id)
                .map(memberMapper::memberToMemberResponseDto);
    }

    public TokenResponseDto verifyRefreshToken(VerifyRefreshTokenRequestDto request, HttpServletRequest httpRequest) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.isValidToken(refreshToken))
            throw new RuntimeException("Invalid Token");

        Claims claims = jwtUtil.resolveToken(refreshToken).get();
        Long memberId = Long.valueOf(claims.getSubject());

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new RuntimeException("Not found Member with Id : " + memberId)
        );

        if (!member.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh token is not identical for member with id : " + memberId);
        }

        String newAccessToken = jwtUtil.createAccessToken(member.getId(), httpRequest.getRequestURI());
        String newRefreshToken = jwtUtil.createRefreshToken(member.getId(), httpRequest.getRequestURI());
        member.setRefreshToken(newRefreshToken);

        return new TokenResponseDto(newAccessToken,newRefreshToken);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("Member not found with username: " + username)
        );
    }
}
