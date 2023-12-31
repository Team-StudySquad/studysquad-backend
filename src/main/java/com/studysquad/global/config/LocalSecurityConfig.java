package com.studysquad.global.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.*;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.studysquad.global.filter.ApiAccessDeniedHandler;
import com.studysquad.global.filter.ApiAuthenticationEntryPoint;
import com.studysquad.global.filter.JwtAuthenticationFilter;
import com.studysquad.global.security.JwtProvider;
import com.studysquad.user.service.ApiUserDetailsService;

import lombok.RequiredArgsConstructor;

@Profile({"local", "default"})
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class LocalSecurityConfig {

	private final JwtProvider jwtProvider;
	private final ApiUserDetailsService userDetailsService;
	private final ApiAuthenticationEntryPoint entryPoint;
	private final ApiAccessDeniedHandler deniedHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.formLogin().disable()
			.httpBasic().disable()
			.csrf().disable()
			.cors().configurationSource(corsConfigurationSource())
			.and()
			.headers().frameOptions().disable()
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
			.requestMatchers(toH2Console()).permitAll()
			.mvcMatchers(ApiUrls.PERMIT_API_URLS).permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.exceptionHandling()
			.authenticationEntryPoint(entryPoint)
			.accessDeniedHandler(deniedHandler);
		http.addFilterAfter(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtProvider, userDetailsService);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOriginPatterns(Arrays.asList("*"));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setExposedHeaders(Arrays.asList("Authorization"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}

}
