package dev.gaau.login.repository;

import dev.gaau.login.domain.Member;
import dev.gaau.login.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByMember(Member member);
    void deleteByMember(Member member);
}
