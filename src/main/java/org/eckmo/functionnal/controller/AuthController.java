package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import org.eckmo.functionnal.dto.UserDTO;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.service.InstructorDocumentService;
import org.eckmo.functionnal.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final InstructorDocumentService documentService;

    @GetMapping("/")
    public String root() { return "landing"; }

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userForm", UserDTO.builder().role(User.UserRole.STUDENT.name()).build());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("userForm") UserDTO userDTO,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents,
            Model model) {
        try {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            boolean isInstructor = "INSTRUCTOR".equals(userDTO.getRole());
            userDTO.setIsActive(!isInstructor); // instructors inactive until approved
            UserDTO created = userService.createUser(userDTO);

            if (isInstructor && documents != null && !documents.isEmpty()) {
                documentService.saveDocuments(created.getId(), documents);
            }

            return isInstructor
                    ? "redirect:/login?pending"
                    : "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userForm", userDTO);
            return "auth/register";
        }
    }
}
