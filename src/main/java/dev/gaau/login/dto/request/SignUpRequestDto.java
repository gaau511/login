package dev.gaau.login.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

}
