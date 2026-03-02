package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import org.eckmo.functionnal.dto.UserDTO;
import org.eckmo.functionnal.model.InstructorDocument;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.service.InstructorDocumentService;
import org.eckmo.functionnal.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/ui/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final InstructorDocumentService documentService;

    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("activeUsers", userService.getActiveUsers().size());
        model.addAttribute("pendingInstructors", userService.getPendingInstructors());
        model.addAttribute("pendingCount", userService.countPendingInstructors());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(required = false) String search,
            Model model) {
        Page<UserDTO> usersPage = userService.getAllUsersPaged(page, size, sort, search);
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("userForm", UserDTO.builder().role(User.UserRole.STUDENT.name()).build());
        return "admin/users";
    }

    @GetMapping("/instructors/pending")
    public String pendingInstructors(Model model) {
        model.addAttribute("pendingInstructors", userService.getPendingInstructors());
        return "admin/pending-instructors";
    }

    @GetMapping("/instructors/{id}/review")
    public String reviewInstructor(@PathVariable Long id, Model model) {
        model.addAttribute("instructor", userService.getUserById(id));
        List<InstructorDocument> docs = documentService.getDocumentsByUser(id);
        model.addAttribute("documents", docs);
        return "admin/review-instructor";
    }

    @PostMapping("/instructors/{id}/approve")
    public String approveInstructor(@PathVariable Long id) {
        userService.approveInstructor(id);
        return "redirect:/ui/admin/instructors/pending?approved";
    }

    @PostMapping("/instructors/{id}/reject")
    public String rejectInstructor(@PathVariable Long id, @RequestParam String reason) {
        userService.rejectInstructor(id, reason);
        return "redirect:/ui/admin/instructors/pending?rejected";
    }

    @PostMapping("/users/{id}/update-password")
    public String updatePassword(@PathVariable Long id, @RequestParam String newPassword) {
        UserDTO user = userService.getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(id, user);
        return "redirect:/ui/admin/users?updated";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        user.setIsActive(!user.getIsActive());
        userService.updateUser(id, user);
        return "redirect:/ui/admin/users";
    }

    @PostMapping("/users/{id}/update-role")
    public String updateRole(@PathVariable Long id, @RequestParam String role) {
        UserDTO user = userService.getUserById(id);
        user.setRole(role);
        userService.updateUser(id, user);
        return "redirect:/ui/admin/users";
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            InstructorDocument document = documentService.getDocumentById(id);
            Path filePath = Paths.get(documentService.getUploadDir()).resolve(document.getFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = document.getFileType() != null ?
                    document.getFileType() : "application/octet-stream";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
