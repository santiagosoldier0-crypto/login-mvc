package com.exposicion.loginmvc.service;

import com.exposicion.loginmvc.model.User;
import com.exposicion.loginmvc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Servicio que contiene la lógica de negocio para la autenticación. Delega el
 * acceso a datos al UserRepository.
 */
@Service
public class AuthService {

    // Inyectamos el Repositorio para acceder a los datos de usuario.
    @Autowired
    private UserRepository userRepository;

    /**
     * Realiza el proceso de autenticación. 1. Busca el usuario por nombre
     * usando el Repositorio. 2. Si lo encuentra, compara la contraseña.
     *
     * @param username Nombre de usuario proporcionado.
     * @param password Contraseña proporcionada.
     * @return true si las credenciales son válidas, false en caso contrario.
     */
    public boolean authenticate(String username, String password) {

        // 1. Buscamos al usuario en el repositorio (simulación de BD)
        Optional<User> userOptional = userRepository.findByUsername(username);

        // 2. Verificamos si el usuario existe y si la contraseña coincide
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Compara la contraseña proporcionada con la almacenada.
            return user.getPassword().equals(password);
        }

        // Si el usuario no fue encontrado, falla la autenticación.
        return false;
    }
}
