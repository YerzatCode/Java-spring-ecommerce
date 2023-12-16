package com.example.demoauth.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demoauth.configs.jwt.JwtUtils;
import com.example.demoauth.models.ERole;
import com.example.demoauth.models.Role;
import com.example.demoauth.models.User;
import com.example.demoauth.pojo.JwtResponse;
import com.example.demoauth.pojo.LoginRequest;
import com.example.demoauth.pojo.MessageResponse;
import com.example.demoauth.pojo.SignupRequest;
import com.example.demoauth.repository.RoleRepository;
import com.example.demoauth.repository.UserRepository;
import com.example.demoauth.service.UserDetailsImpl;

@RestController
    @RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	UserRepository userRespository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JwtUtils jwtUtils;

	@Value("${app.jwtSecret}")
	private String jwtSecret;

	@GetMapping("/me")
	public ResponseEntity<?> meProfile(@RequestHeader("Authorization") String jwtToken){
		if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
			// Извлечение самого токена без префикса "Bearer "
			String token = jwtToken.substring(7);

			// Вы можете выполнить операции с токеном здесь, например, его декодирование
			Claims claims = JwtUtils.decodeJWT(token, jwtSecret);

			String username = claims.getSubject();

			Optional<User> user = userRespository.findByUsername(username);

			return ResponseEntity.ok(user);
		} else {
			// Обработка случаев, когда токен отсутствует или не является корректным
			return ResponseEntity.ok("Not found");
		}
	}
	
	@PostMapping("/signin")
	public ResponseEntity<?> authUser(@RequestBody LoginRequest loginRequest) {
		
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(
						loginRequest.getUsername(), 
						loginRequest.getPassword()));
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());
		
		return ResponseEntity.ok(new JwtResponse(jwt, 
				userDetails.getId(), 
				userDetails.getUsername(), 
				userDetails.getEmail(), 
				roles));
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
		
		if (userRespository.existsByUsername(signupRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is exist"));
		}
		
		if (userRespository.existsByEmail(signupRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is exist"));
		}
		
		User user = new User(signupRequest.getUsername(), 
				signupRequest.getEmail(),
				passwordEncoder.encode(signupRequest.getPassword()));
		
		Set<String> reqRoles = signupRequest.getRoles();
		Set<Role> roles = new HashSet<>();
		
		if (reqRoles == null) {
			Role userRole = roleRepository
					.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error, Role USER is not found"));
			roles.add(userRole);
		} else {
			reqRoles.forEach(r -> {
				switch (r) {
				case "admin":
					Role adminRole = roleRepository
						.findByName(ERole.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Error, Role ADMIN is not found"));
					roles.add(adminRole);
					
					break;
				case "mod":
					Role modRole = roleRepository
						.findByName(ERole.ROLE_MODERATOR)
						.orElseThrow(() -> new RuntimeException("Error, Role MODERATOR is not found"));
					roles.add(modRole);
					
					break;

				default:
					Role userRole = roleRepository
						.findByName(ERole.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Error, Role USER is not found"));
					roles.add(userRole);
				}
			});
		}
		user.setRoles(roles);
		userRespository.save(user);
		return ResponseEntity.ok(new MessageResponse("User CREATED"));
	}
}
