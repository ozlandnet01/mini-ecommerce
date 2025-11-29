package com.example.auth.memberservice.service;

import com.example.auth.memberservice.dto.LoginRequest;
import com.example.auth.memberservice.dto.LoginResponse;
import com.example.auth.memberservice.dto.MemberRegisterRequest;
import com.example.auth.memberservice.dto.MemberRegisterResponse;
import com.example.auth.memberservice.exception.BusinessException;
import com.example.auth.memberservice.constant.Role;
import com.example.auth.memberservice.model.Member;
import com.example.auth.memberservice.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Register a new member with given email and password.
   *
   * @param request the object containing email and password
   * @return MemberRegisterResponse containing member's id, email, and role
   * @throws BusinessException if email is not found or password is invalid
   */
  @Override
  public MemberRegisterResponse register(MemberRegisterRequest request) {

    if (memberRepository.existsByEmail(request.email().toLowerCase())) {
      throw new BusinessException("EMAIL_ALREADY_USED", "Email already registered");
    }

    Member member = Member.builder()
            .email(request.email().toLowerCase())
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(Role.CUSTOMER.name())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    Member saved = memberRepository.save(member);

    return new MemberRegisterResponse(
            saved.getId(),
            saved.getEmail()
    );
  }


  /**
   * Authenticate a member with given email and password.
   *
   * @param request the object containing email and password
   * @return LoginResponse containing member's id, email, and role
   * @throws BusinessException if email is not found or password is invalid
   */
  @Override
  public LoginResponse login(LoginRequest request) {
    Member member = memberRepository.findByEmail(request.email().toLowerCase())
            .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
      throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
    }

    member.setUpdatedAt(Instant.now());
    memberRepository.save(member);

    return LoginResponse.fromMember(member);
  }
}
