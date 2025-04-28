package dev.gaau.login.mapper;

import dev.gaau.login.domain.Member;
import dev.gaau.login.dto.request.SignUpRequestDto;
import dev.gaau.login.dto.response.MemberResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {

    MemberMapper INSTANCE = Mappers.getMapper( MemberMapper.class );

    Member signUpRequestDtoToMember(SignUpRequestDto signUpRequestDto);
    MemberResponseDto memberToMemberResponseDto(Member member);
}
