package com.sgic.defect_tracker.config;

import com.sgic.defect_tracker.service.EmployeeDetailsService;
import com.sgic.defect_tracker.service.JwtTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final EmployeeDetailsService employeeDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            EmployeeDetailsService employeeDetailsService) {

        this.jwtTokenService = jwtTokenService;
        this.employeeDetailsService = employeeDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {


            String email = jwtTokenService.extractEmail(token);


            UserDetails userDetails =
                    employeeDetailsService.loadUserByUsername(email);

            System.out.println("========== JWT DEBUG ==========");
            System.out.println("Email: " + email);
            System.out.println("Authorities: " + userDetails.getAuthorities());
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Request Method: " + request.getMethod());
            System.out.println("================================");

            // Authentication object create பண்ணுகிறது
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );


            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);


            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {


            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write(
                    "{\"message\":\"Invalid or expired token\"}"
            ); 
        }
    }
}