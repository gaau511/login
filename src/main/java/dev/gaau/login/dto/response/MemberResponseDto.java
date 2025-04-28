package dev.gaau.login.dto.response;

import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDto {

    private Long id;

    private String username;

    private String password;

    private String name;

    private String gender;

    private String email;

    private String nickname;

    private Date birth;

    private LocalDateTime createdAt;
}
