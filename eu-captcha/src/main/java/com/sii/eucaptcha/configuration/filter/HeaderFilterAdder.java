package com.sii.eucaptcha.configuration.filter;

import com.sii.eucaptcha.security.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HeaderFilterAdder extends OncePerRequestFilter {

  private static final String X_JWT_STRING = "x-jwtString";

  private final JwtToken jwtToken;

  public HeaderFilterAdder(JwtToken jwtToken) {
    this.jwtToken = jwtToken;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest httpServletRequest,
    HttpServletResponse httpServletResponse,
    FilterChain filterChain
  ) throws ServletException, IOException {
    if (httpServletRequest.getRequestURI().contains("captchaImg")) {
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        httpServletRequest.getHeader("Origin")
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
        "true"
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
        "*, Access-Control-Allow-Headers, X-Requested-With, x-jwtString, Content-Type, cache-control"
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
        X_JWT_STRING
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
        "Content-Type"
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
        "POST, GET, HEAD, OPTIONS"
      );
      httpServletResponse.addHeader(X_JWT_STRING, jwtToken.generateJwtToken());
    }
    if (httpServletRequest.getRequestURI().contains("reloadCaptchaImg")) {
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        httpServletRequest.getHeader("Origin")
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
        "true"
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
        "*, Access-Control-Allow-Headers, X-Requested-With, x-jwtString, Content-Type, cache-control"
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
        X_JWT_STRING
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
        "Content-Type"
      );
      httpServletResponse.addHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
        "POST, GET, HEAD, OPTIONS"
      );
      httpServletResponse.addHeader(
        X_JWT_STRING,
        httpServletRequest.getHeader(X_JWT_STRING)
      );
    }
    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }
}
