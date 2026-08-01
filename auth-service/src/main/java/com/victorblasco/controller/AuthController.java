package com.victorblasco.controller;

import com.victorblasco.dto.*;
import com.victorblasco.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints de autenticación y gestión de usuarios.
 * <p>
 * Rutas soportadas bajo `/api/v1/auth`:
 * <ul>
 *   <li>POST `/register`: Registro de nuevos usuarios.</li>
 *   <li>POST `/login`: Autenticación y obtención de token JWT.</li>
 *   <li>POST `/validate`: Validación interna de tokens JWT para la BFF Gateway.</li>
 * </ul>
 * </p>
 *
 * @author Victor Blasco
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Construye el controlador inyectando el servicio de autenticación.
     *
     * @param authService servicio de autenticación
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para registrar un nuevo usuario en la plataforma.
     *
     * @param request datos de registro (email y contraseña)
     * @return {@link ResponseEntity} con los datos del usuario registrado y código 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para iniciar sesión y generar un token de acceso JWT.
     *
     * @param request credenciales de acceso (email y contraseña)
     * @return {@link ResponseEntity} con el token JWT e información del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para validar un token JWT existente (utilizado por el API Gateway / Proxy).
     *
     * @param token Bearer token enviado en la cabecera o cuerpo
     * @return {@link ResponseEntity} con la confirmación de validez y el userId asociado
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestParam("token") String token) {
        ValidateResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }
}
