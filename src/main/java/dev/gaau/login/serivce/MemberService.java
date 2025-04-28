package dev.gaau.login.serivce;

import dev.gaau.login.domain.Member;
import dev.gaau.login.dto.request.SignUpRequestDto;
import dev.gaau.login.dto.response.MemberResponseDto;
import dev.gaau.login.mapper.MemberMapper;
import dev.gaau.login.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberResponseDto join(SignUpRequestDto request) {
        Member member = memberMapper.signUpRequestDtoToMember(request);
        Member savedMember = memberRepository.save(member);

        return memberMapper.memberToMemberResponseDto(savedMember);
    }
}
