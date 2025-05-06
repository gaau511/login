package dev.gaau.login.filter;

import dev.gaau.login.dto.response.MemberResponseDto;
import dev.gaau.login.jwt.JwtUtil;
import dev.gaau.login.serivce.MemberService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader("accessToken");

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.isValidToken(accessToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            // response.sendRedirect("new URL for refreshToken reissue.");
            String body = """
            {
                "code": "INVALID_TOKEN",
                "message": "JWT Token is invalid."
            }
            """;

            response.getWriter().write(body);
            return;
        }

        Claims claims = jwtUtil.resolveToken(accessToken).get();

        Long memberId = Long.valueOf(claims.getSubject());
        MemberResponseDto memberDto = memberService.findById(memberId).orElseThrow(
                () -> new RuntimeException("Cannot find member with ID : " + memberId)
        );

        String username = memberDto.getUsername();
        UserDetails userDetails = memberService.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request,response);
    }


}
