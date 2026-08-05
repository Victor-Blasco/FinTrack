package com.victorblasco.service;

import com.victorblasco.config.JwtProperties;
import com.victorblasco.dto.*;
import com.victorblasco.exception.EmailAlreadyExistsException;
import com.victorblasco.exception.InvalidCredentialsException;
import com.victorblasco.exception.InvalidTokenException;
import com.victorblasco.model.User;
import com.victorblasco.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Servicio de negocio encargado del ciclo de vida de la autenticación de usuarios.
 * <p>
 * Gestiona el registro con contraseñas encriptadas mediante BCrypt, la generación de firmas
 * JWT HMAC-SHA256 parametrizadas y la validación de tokens de acceso para la BFF Gateway.
 * </p>
 *
 * @author Victor Blasco
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    /**
     * Construye el servicio de autenticación inyectando repositorios, codificadores y propiedades JWT.
     *
     * @param userRepository repositorio de usuarios
     * @param passwordEncoder codificador BCrypt para contraseñas
     * @param jwtEncoder firmador de tokens JWT
     * @param jwtDecoder decodificador de tokens JWT
     * @param jwtProperties propiedades de configuración JWT
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder,
                       JwtDecoder jwtDecoder,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Registra un nuevo usuario verificando que el correo electrónico no exista previamente.
     *
     * @param request DTO con datos de registro
     * @return DTO {@link RegisterResponse} con el ID generado y email registrado
     * @throws EmailAlreadyExistsException si el email ya está en uso
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Intento de registro fallido: el email [{}] ya existe", request.email());
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword);
        User savedUser = userRepository.save(user);

        log.info("Usuario registrado con éxito con ID [{}]", savedUser.getId());
        return new RegisterResponse(savedUser.getId(), savedUser.getEmail());
    }

    /**
     * Autentica las credenciales de un usuario y emite un token JWT firmado de acceso.
     *
     * @param request DTO con credenciales (email y password)
     * @return DTO {@link LoginResponse} con el token JWT e información de sesión
     * @throws InvalidCredentialsException si el usuario no existe o la contraseña es incorrecta
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: usuario no encontrado para email [{}]", request.email());
                    return new InvalidCredentialsException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Intento de login fallido: contraseña incorrecta para usuario [{}]", user.getId());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = generateToken(user);
        log.info("Token JWT emitido exitosamente para el usuario [{}]", user.getId());
        return new LoginResponse(token, user.getId(), user.getEmail());
    }

    /**
     * Valida la autenticidad y fecha de caducidad de un token JWT.
     *
     * @param token cadena Bearer token a verificar
     * @return DTO {@link ValidateResponse} indicando si el token es válido y el UUID del usuario
     * @throws InvalidTokenException si el token es nulo, corrupto o ha expirado
     */
    @Transactional(readOnly = true)
    public ValidateResponse validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String userIdStr = jwt.getSubject();
            UUID userId = UUID.fromString(userIdStr);
            return new ValidateResponse(true, userId);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token de acceso rechazado por firma inválida, expiración o formato incorrecto: {}", e.getMessage());
            throw new InvalidTokenException("Token inválido o expirado");
        }
    }

    /**
     * Genera un token JWT firmado según la configuración de {@link JwtProperties}.
     *
     * @param user usuario autenticado
     * @return cadena con el token JWT codificado
     */
    private String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expirationHours(), ChronoUnit.HOURS))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
