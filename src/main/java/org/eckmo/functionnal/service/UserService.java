package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.dto.UserDTO;
import org.eckmo.functionnal.exception.ResourceNotFoundException;
import org.eckmo.functionnal.functional.UserProcessor;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Function<User, UserDTO> USER_TO_DTO = user -> UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .isActive(user.getIsActive())
            .approvalStatus(user.getApprovalStatus() != null ? user.getApprovalStatus().name() : "APPROVED")
            .rejectionReason(user.getRejectionReason())
            .bio(user.getBio())
            .profilePicture(user.getProfilePicture())
            .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : "")
            .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(DATE_FORMATTER) : "")
            .build();

    private final Predicate<User> IS_ACTIVE = User::getIsActive;
    private final Predicate<User> IS_INSTRUCTOR = user -> user.getRole() == User.UserRole.INSTRUCTOR;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(USER_TO_DTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsersPaged(int page, int size, String sortBy, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<User> users = (search != null && !search.isBlank())
                ? userRepository.searchUsers(search, pageable)
                : userRepository.findAll(pageable);
        return users.map(USER_TO_DTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(USER_TO_DTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(USER_TO_DTO)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsers() {
        return userRepository.findAllActiveUsers().stream().filter(IS_ACTIVE).map(USER_TO_DTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getInstructors() {
        return userRepository.findAllInstructors().stream().filter(IS_INSTRUCTOR).map(USER_TO_DTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getPendingInstructors() {
        return userRepository.findPendingInstructors().stream().map(USER_TO_DTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countPendingInstructors() {
        return userRepository.countByApprovalStatus(User.ApprovalStatus.PENDING);
    }

    public UserDTO createUser(UserDTO userDTO) {
        validateEmail(userDTO.getEmail());
        User.ApprovalStatus approvalStatus = "INSTRUCTOR".equals(userDTO.getRole())
                ? User.ApprovalStatus.PENDING
                : User.ApprovalStatus.APPROVED;

        User user = User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .role(User.UserRole.valueOf(Optional.ofNullable(userDTO.getRole()).orElse("STUDENT")))
                .isActive(Optional.ofNullable(userDTO.getIsActive()).orElse(true))
                .approvalStatus(approvalStatus)
                .bio(userDTO.getBio())
                .build();

        return USER_TO_DTO.apply(userRepository.save(user));
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Optional.ofNullable(userDTO.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(userDTO.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(userDTO.getEmail())
                .filter(e -> !e.equals(user.getEmail()))
                .ifPresent(e -> { validateEmail(e); user.setEmail(e); });
        Optional.ofNullable(userDTO.getPassword()).filter(p -> !p.isBlank()).ifPresent(user::setPassword);
        Optional.ofNullable(userDTO.getRole()).ifPresent(r -> user.setRole(User.UserRole.valueOf(r)));
        Optional.ofNullable(userDTO.getIsActive()).ifPresent(user::setIsActive);
        Optional.ofNullable(userDTO.getBio()).ifPresent(user::setBio);
        Optional.ofNullable(userDTO.getProfilePicture()).ifPresent(user::setProfilePicture);

        return USER_TO_DTO.apply(userRepository.save(user));
    }

    public UserDTO approveInstructor(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        user.setIsActive(true);
        user.setRejectionReason(null);
        return USER_TO_DTO.apply(userRepository.save(user));
    }

    public UserDTO rejectInstructor(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setApprovalStatus(User.ApprovalStatus.REJECTED);
        user.setIsActive(false);
        user.setRejectionReason(reason);
        return USER_TO_DTO.apply(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.findById(id)
                .ifPresentOrElse(userRepository::delete,
                        () -> { throw new ResourceNotFoundException("User not found with id: " + id); });
    }

    public String processUser(Long userId, UserProcessor processor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return processor.process(user.getId(), user.getEmail(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public long countUsersByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> filterUsers(Predicate<User> predicate) {
        return userRepository.findAll().stream().filter(predicate).map(USER_TO_DTO).collect(Collectors.toList());
    }

    private void validateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }
}
