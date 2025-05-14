package dev.gaau.login.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Lob
    @Column(name = "token", columnDefinition = "TEXT", nullable = false)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public RefreshToken(String token, LocalDateTime expiresAt, Member member) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.member = member;
    }

}
