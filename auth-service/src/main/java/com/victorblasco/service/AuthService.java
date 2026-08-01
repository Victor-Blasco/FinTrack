package com.victorblasco.service;

import com.victorblasco.dto.*;
import com.victorblasco.exception.EmailAlreadyExistsException;
import com.victorblasco.exception.InvalidCredentialsException;
import com.victorblasco.exception.InvalidTokenException;
import com.victorblasco.model.User;
import com.victorblasco.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Servicio de negocio encargado del ciclo de vida de la autenticación de usuarios.
 * <p>
 * Gestiona el registro con contraseñas encriptadas mediante BCrypt, la generación de firmas
 * JWT HMAC-SHA256 y la validación de tokens de acceso para la BFF Gateway.
 * </p>
 *
 * @author Victor Blasco
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    /**
     * Construye el servicio de autenticación inyectando dependencias de repositorio, codificador y decodificador JWT.
     *
     * @param userRepository repositorio de usuarios
     * @param passwordEncoder codificador BCrypt para contraseñas
     * @param jwtEncoder firmador de tokens JWT
     * @param jwtDecoder decodificador de tokens JWT
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Registra un nuevo usuario verificando que el correo electrónico no exista previamente.
     *
     * @param request DTO con datos de registro
     * @return DTO {@link RegisterResponse} con el ID generado y email registrado
     * @throws EmailAlreadyExistsException si el email ya está en uso
     */
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword);
        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail());
    }

    /**
     * Autentica las credenciales de un usuario y emite un token JWT firmado de acceso.
     *
     * @param request DTO con credenciales (email y password)
     * @return DTO {@link LoginResponse} con el token JWT e información de sesión
     * @throws InvalidCredentialsException si el usuario no existe o la contraseña es incorrecta
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = generateToken(user);
        return new LoginResponse(token, user.getId(), user.getEmail());
    }

    /**
     * Valida la autenticidad y fecha de caducidad de un token JWT.
     *
     * @param token cadena Bearer token a verificar
     * @return DTO {@link ValidateResponse} indicando si el token es válido y el UUID del usuario
     * @throws InvalidTokenException si el token es nulo, corrupto o ha expirado
     */
    public ValidateResponse validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String userIdStr = jwt.getSubject();
            UUID userId = UUID.fromString(userIdStr);
            return new ValidateResponse(true, userId);
        } catch (Exception e) {
            throw new InvalidTokenException("Token inválido o expirado");
        }
    }

    /**
     * Genera un token JWT firmado con validez de 24 horas.
     *
     * @param user usuario autenticado
     * @return cadena con el token JWT codificado
     */
    private String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("fintrack-auth-service")
                .issuedAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
