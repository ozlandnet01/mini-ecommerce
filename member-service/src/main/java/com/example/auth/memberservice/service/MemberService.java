package com.example.auth.memberservice.service;

import com.example.auth.memberservice.dto.LoginRequest;
import com.example.auth.memberservice.dto.LoginResponse;
import com.example.auth.memberservice.dto.MemberRegisterRequest;
import com.example.auth.memberservice.dto.MemberRegisterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {

  MemberRegisterResponse register(MemberRegisterRequest request);

  LoginResponse login(LoginRequest request);
  
  /**
   * Get all members with pagination
   * @param pageable pagination information (page number, page size, etc.)
   * @return Page of MemberRegisterResponse
   */
  Page<MemberRegisterResponse> getAllUsers(Pageable pageable);
}
