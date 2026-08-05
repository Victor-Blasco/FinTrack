package com.victorblasco.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA que representa a un usuario registrado en la base de datos PostgreSQL.
 *
 * @author Victor Blasco
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public User() {
    }

    /**
     * Crea un nuevo objeto de usuario con email y contraseña cifrada.
     *
     * @param email correo electrónico único del usuario
     * @param password hash BCrypt de la contraseña
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
    }

    /**
     * Asigna automáticamente la marca de tiempo antes de insertar en base de datos.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
