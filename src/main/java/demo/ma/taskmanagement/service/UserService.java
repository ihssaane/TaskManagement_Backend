package demo.ma.taskmanagement.service;


import demo.ma.taskmanagement.Repositories.UserRepository;
import demo.ma.taskmanagement.dto.UserDto;
import demo.ma.taskmanagement.entity.User;
import demo.ma.taskmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsers() {
        return userRepository.findActiveUsers().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto, String updatedByUsername) {
        log.info("Updating user with ID: {} by user: {}", id, updatedByUsername);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User updatedBy = userRepository.findByUsername(updatedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + updatedByUsername));

        // Vérification des permissions
        if (!canUserModifyUser(existingUser, updatedBy)) {
            throw new RuntimeException("You don't have permission to modify this user");
        }

        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setEmail(userDto.getEmail());

        // Seul un admin peut modifier le rôle et le statut
        if (updatedBy.getRole() == User.Role.ADMIN) {
            existingUser.setRole(userDto.getRole());
            existingUser.setIsActive(userDto.getIsActive());
        }

        User savedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", savedUser.getUsername());

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully: {}", user.getUsername());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void activateUser(Long id) {
        log.info("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("User activated successfully: {}", user.getUsername());
    }

    private boolean canUserModifyUser(User targetUser, User currentUser) {
        return currentUser.getRole() == User.Role.ADMIN ||
                targetUser.getId().equals(currentUser.getId());
    }
}
