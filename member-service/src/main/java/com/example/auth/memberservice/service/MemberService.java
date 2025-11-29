package com.example.auth.memberservice.service;

import com.example.auth.memberservice.dto.LoginRequest;
import com.example.auth.memberservice.dto.LoginResponse;
import com.example.auth.memberservice.dto.MemberRegisterRequest;
import com.example.auth.memberservice.dto.MemberRegisterResponse;

public interface MemberService {

  MemberRegisterResponse register(MemberRegisterRequest request);

  LoginResponse login(LoginRequest request);
}
