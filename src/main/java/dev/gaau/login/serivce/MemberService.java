package dev.gaau.login.serivce;

import dev.gaau.login.domain.Member;
import dev.gaau.login.domain.RefreshToken;
import dev.gaau.login.dto.request.LoginRequestDto;
import dev.gaau.login.dto.request.SignUpRequestDto;
import dev.gaau.login.dto.response.MemberResponseDto;
import dev.gaau.login.dto.response.TokenResponseDto;
import dev.gaau.login.jwt.JwtUtil;
import dev.gaau.login.mapper.MemberMapper;
import dev.gaau.login.repository.MemberRepository;
import dev.gaau.login.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public MemberResponseDto join(SignUpRequestDto request) {

        String username = request.getUsername();

        if (memberRepository.existsByUsername(username)) {
            throw new RuntimeException("An Already existing username");
        }

        if (!validatePassword(request.getPassword())) {
            throw new RuntimeException("Password Should be longer than 8 letters" +
                    "and include upper letters, lower letters and special symbols." );
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);

        Member member = memberMapper.signUpRequestDtoToMember(request);
        Member savedMember = memberRepository.save(member);

        return memberMapper.memberToMemberResponseDto(savedMember);
    }

    private Boolean validatePassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";
        return password.matches(passwordRegex);
    }

    public TokenResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest,
                                  HttpServletResponse response) {

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

        LocalDateTime expiresAt = jwtUtil.getExpiration(refreshToken).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();


        Optional<RefreshToken> findToken = refreshTokenRepository.findByMember(member);
        if (findToken.isPresent()) {
            findToken.get().setToken(refreshToken);
            findToken.get().setExpiresAt(expiresAt);
        } else {
            RefreshToken token = new RefreshToken(refreshToken, expiresAt, member);
            refreshTokenRepository.save(token);
        }

        setTokenCookies(response, refreshToken);

        return new TokenResponseDto(accessToken);
    }

    public void setTokenCookies(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");

        response.addCookie(refreshTokenCookie);
    }

    public Optional<MemberResponseDto> findById(Long id) {
        return memberRepository.findById(id)
                .map(memberMapper::memberToMemberResponseDto);
    }

    public TokenResponseDto verifyRefreshToken(HttpServletRequest httpRequest,
                                               HttpServletResponse response) {

        String refreshToken = null;
        Cookie[] cookies = httpRequest.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }

        if (!jwtUtil.isValidToken(refreshToken)
                || refreshTokenBlackListRepository.existsByToken(refreshToken))
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

        setTokenCookies(response, newRefreshToken);

        return new TokenResponseDto(newAccessToken);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("Member not found with username: " + username)
        );
    }

    public Boolean logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            SecurityContextHolder.clearContext();

            Member findMember = (Member) authentication.getPrincipal();
            ofNullable(findMember).ifPresent(member -> {
                String refreshToken = member.getRefreshToken();
                ofNullable(refreshToken).ifPresent( token -> {
                    RefreshTokenBlacklist newToken = new RefreshTokenBlacklist();
                    newToken.setToken(token);
                    refreshTokenBlackListRepository.save(newToken);
                });
            });
        }

        return false;
    }
}
