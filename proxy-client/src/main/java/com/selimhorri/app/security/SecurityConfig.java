package com.selimhorri.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.selimhorri.app.business.user.model.RoleBasedAuthority;
import com.selimhorri.app.config.filter.JwtRequestFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final JwtRequestFilter jwtRequestFilter;

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService)
				.passwordEncoder(this.passwordEncoder);
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.cors().disable()
				.csrf().disable()
				.authorizeRequests()
				// Public endpoints
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/", "index", "**/css/**", "**/js/**").permitAll()
				.antMatchers("/api/authenticate/**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/users").permitAll()
				.antMatchers(HttpMethod.GET, "/api/products").permitAll()
				.antMatchers(HttpMethod.GET, "/api/products/*").permitAll()
				.antMatchers(HttpMethod.GET, "/api/categories").permitAll()
				.antMatchers(HttpMethod.GET, "/api/categories/*").permitAll()
				.antMatchers("/actuator/health/**", "/actuator/info/**").permitAll()
				// Admin only
				.antMatchers("/actuator/**").hasRole(RoleBasedAuthority.ROLE_ADMIN.getRole())
				// Protected endpoints - authentication required
				// Each microservice handles its own authorization
				.antMatchers("/api/**").authenticated()
				.anyRequest().authenticated()
				.and()
				.headers()
				.frameOptions()
				.sameOrigin()
				.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.addFilterBefore(this.jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

}
