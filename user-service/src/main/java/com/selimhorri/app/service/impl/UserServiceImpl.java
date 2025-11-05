package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public List<UserDto> findAll() {
		log.info("*** UserDto List, service; fetch all users *");
		return this.userRepository.findAll()
				.stream()
				.map(UserMappingHelper::map)
				.distinct()
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public UserDto findById(final Integer userId) {
		log.info("*** UserDto, service; fetch user by id *");
		return this.userRepository.findById(userId)
				.map(UserMappingHelper::map)
				.orElseThrow(
						() -> new UserObjectNotFoundException(String.format("User with id: %d not found", userId)));
	}

	@Override
	public UserDto save(final UserDto userDto) {
		log.info("*** UserDto, service; save user *");

		// Encriptar la contraseña antes de guardar
		if (userDto.getCredentialDto() != null && userDto.getCredentialDto().getPassword() != null) {
			String originalPassword = userDto.getCredentialDto().getPassword();
			String encryptedPassword = passwordEncoder.encode(originalPassword);
			log.info("🔐 Password encrypted: {} -> {}", originalPassword.substring(0, 3) + "***", encryptedPassword.substring(0, 20) + "...");
			userDto.getCredentialDto().setPassword(encryptedPassword);
		}

		// Mapear DTO a Entity
		var user = UserMappingHelper.map(userDto);

		// CRÍTICO: Establecer relación bidireccional entre User y Credential
		// Esto es necesario para que JPA guarde el credential correctamente
		if (user.getCredential() != null) {
			user.getCredential().setUser(user);
			log.info("✅ Bidirectional relationship User <-> Credential established");
		}

		// Establecer relación bidireccional entre User y Addresses
		if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
			user.getAddresses().forEach(address -> address.setUser(user));
		}

		// Guardar y retornar
		User savedUser = this.userRepository.save(user);
		log.info("💾 User saved with ID: {}, Credential ID: {}", savedUser.getUserId(), 
				savedUser.getCredential() != null ? savedUser.getCredential().getCredentialId() : "null");
		
		return UserMappingHelper.map(savedUser);
	}

	@Override
	public UserDto update(final UserDto userDto) {
		log.info("*** UserDto, service; update user *");
		return UserMappingHelper.map(this.userRepository.save(UserMappingHelper.map(userDto)));
	}

	@Override
	public UserDto update(final Integer userId, final UserDto userDto) {
		log.info("*** UserDto, service; update user with userId *");
		return UserMappingHelper.map(this.userRepository.save(
				UserMappingHelper.map(this.findById(userId))));
	}

	@Override
	public void deleteById(final Integer userId) {
		log.info("*** Void, service; delete user by id *");
		this.userRepository.deleteById(userId);
	}

	@Override
	public UserDto findByUsername(final String username) {
		log.info("*** UserDto, service; fetch user with username *");
		return UserMappingHelper.map(this.userRepository.findByCredentialUsername(username)
				.orElseThrow(() -> new UserObjectNotFoundException(
						String.format("User with username: %s not found", username))));
	}

}