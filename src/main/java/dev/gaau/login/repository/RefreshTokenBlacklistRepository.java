package dev.gaau.login.repository;

import dev.gaau.login.domain.RefreshTokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenBlacklistRepository extends JpaRepository<RefreshTokenBlacklist, Long> {
    Boolean existsByToken(String token);
}
