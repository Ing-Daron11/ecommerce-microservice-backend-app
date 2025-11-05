package com.selimhorri.app.config.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.selimhorri.app.jwt.service.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain)
			throws ServletException, IOException {

		log.info("**JwtRequestFilter, once per request, validating and extracting token*\n");

		final var authorizationHeader = request.getHeader("Authorization");

		String username = null;
		String jwt = null;

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			username = jwtService.extractUsername(jwt);
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

			if (this.jwtService.validateToken(jwt, userDetails)) {
				final String userId = jwtService.extractUserId(jwt);

				final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				System.out.println("ID EN CADENA "+userId);
				request.setAttribute("userId", userId);

				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}

		}

		filterChain.doFilter(request, response);
		log.info("**Jwt request filtered!*\n");
	}

	// ✅ NUEVO: Excluir rutas públicas del filtro JWT
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		log.info("Verificando si filtrar path: {}", path);
		
		// Excluir rutas públicas del filtro JWT
		boolean shouldNotFilter = path.equals("/") || 
				path.equals("/index") || 
				path.startsWith("/app/api/authenticate") ||
				path.startsWith("/api/authenticate") ||
				(path.startsWith("/app/api/users") && "POST".equals(request.getMethod())) ||
				(path.startsWith("/api/users") && "POST".equals(request.getMethod())) ||
				(path.startsWith("/app/api/credentials") && "POST".equals(request.getMethod())) ||  // ✅ AGREGADO
				(path.startsWith("/api/credentials") && "POST".equals(request.getMethod())) ||  // ✅ AGREGADO
				(path.startsWith("/app/api/products") && "GET".equals(request.getMethod())) ||
				(path.startsWith("/api/products") && "GET".equals(request.getMethod())) ||
				(path.startsWith("/app/api/categories") && "GET".equals(request.getMethod())) ||
				(path.startsWith("/api/categories") && "GET".equals(request.getMethod())) ||
				path.startsWith("/actuator/health") ||
				path.startsWith("/actuator/info");
		
		if (shouldNotFilter) {
			log.info("✅ Path {} excluido del filtro JWT", path);
		}
		
		return shouldNotFilter;
	}

}
