package com.victorblasco.repository;

import com.victorblasco.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para operaciones de lectura y escritura sobre la entidad {@link User}.
 *
 * @author Victor Blasco
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Busca un usuario por su dirección de correo electrónico exacta.
     *
     * @param email correo electrónico a consultar
     * @return {@link Optional} conteniendo el usuario si fue encontrado, o vacío en caso contrario
     */
    Optional<User> findByEmail(String email);

    /**
     * Comprueba si ya existe un usuario registrado con el correo electrónico dado.
     *
     * @param email correo electrónico a verificar
     * @return {@code true} si el email ya existe en la base de datos, {@code false} en caso contrario
     */
    boolean existsByEmail(String email);
}
