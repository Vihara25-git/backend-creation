package com.sgic.defect_tracker.service;

public interface JwtTokenService {

    String generateToken(String email);

    String extractEmail(String token);
}