package dev.gaau.login.dto.request;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDto {

    private String username;

    private String password;

    private String name;

    private String gender;

    private String email;

    private String nickname;

    private Date birth;

}
