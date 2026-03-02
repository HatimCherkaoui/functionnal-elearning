package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import org.eckmo.functionnal.dto.CourseDTO;
import org.eckmo.functionnal.dto.EnrollmentDTO;
import org.eckmo.functionnal.dto.UserDTO;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.service.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ui")
public class UiController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AnalyticsService analyticsService;
    private final SearchService searchService;

    @GetMapping
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("courseCount", courseService.getAllCourses().size());
        model.addAttribute("enrollmentCount", enrollmentService.getAllEnrollments().size());
        model.addAttribute("totalRevenue", analyticsService.calculateTotalRevenue());
        model.addAttribute("topStudents", analyticsService.getTopPerformingStudents(5));
        model.addAttribute("courseStats", analyticsService.getCourseStatistics());
        model.addAttribute("pendingInstructorCount", userService.countPendingInstructors());
        return "ui/index";
    }

    @GetMapping("/users")
    public String users(
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
        return "ui/users";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute("userForm") UserDTO userDTO) {
        userService.createUser(userDTO);
        return "redirect:/ui/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/ui/users";
    }

    @GetMapping("/courses")
    public String courses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(required = false) String search,
            Model model) {
        Page<CourseDTO> coursesPage = courseService.getAllCoursesPaged(page, size, sort, search);
        model.addAttribute("coursesPage", coursesPage);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("courseForm", CourseDTO.builder().level("BEGINNER").build());
        return "ui/courses";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute("courseForm") CourseDTO courseDTO) {
        courseService.createCourse(courseDTO);
        return "redirect:/ui/courses";
    }

    @PostMapping("/courses/{id}/publish")
    public String publishCourse(@PathVariable Long id) {
        courseService.publishCourse(id);
        return "redirect:/ui/courses";
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/ui/courses";
    }

    @GetMapping("/enrollments")
    public String enrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<EnrollmentDTO> enrollmentsPage = enrollmentService.getAllEnrollmentsPaged(page, size);
        model.addAttribute("enrollmentsPage", enrollmentsPage);
        model.addAttribute("enrollmentForm", EnrollmentDTO.builder().build());
        return "ui/enrollments";
    }

    @PostMapping("/enrollments")
    public String createEnrollment(@ModelAttribute("enrollmentForm") EnrollmentDTO dto) {
        enrollmentService.enrollUserInCourse(dto.getUserId(), dto.getCourseId());
        return "redirect:/ui/enrollments";
    }

    @PostMapping("/enrollments/{id}/progress")
    public String updateProgress(@PathVariable Long id, @RequestParam Double progress) {
        enrollmentService.updateEnrollmentProgress(id, progress);
        return "redirect:/ui/enrollments";
    }

    @PostMapping("/enrollments/{id}/score")
    public String updateScore(@PathVariable Long id, @RequestParam Double score) {
        enrollmentService.updateEnrollmentScore(id, score);
        return "redirect:/ui/enrollments";
    }

    @PostMapping("/enrollments/{id}/complete")
    public String completeEnrollment(@PathVariable Long id) {
        enrollmentService.completeEnrollment(id,
                (userId, courseId, currentScore) -> currentScore >= 70.0,
                (userName, courseName, certCode) ->
                        String.format("Certificate for %s in %s - Code: %s", userName, courseName, certCode));
        return "redirect:/ui/enrollments";
    }

    @PostMapping("/enrollments/{id}/drop")
    public String dropEnrollment(@PathVariable Long id) {
        enrollmentService.dropEnrollment(id);
        return "redirect:/ui/enrollments";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("totalRevenue", analyticsService.calculateTotalRevenue());
        model.addAttribute("topStudents", analyticsService.getTopPerformingStudents(10));
        model.addAttribute("courseStats", analyticsService.getCourseStatistics());
        model.addAttribute("enrollmentTrends", analyticsService.getEnrollmentTrendsByCategory());
        model.addAttribute("completionRates", analyticsService.getCompletionRateByCategory());
        model.addAttribute("engagement", analyticsService.getUserEngagementMetrics());
        model.addAttribute("popularCategories", analyticsService.getPopularCategories(10));
        model.addAttribute("revenueByCategory", analyticsService.getRevenueByCategory());
        model.addAttribute("instructorPerformance", analyticsService.getInstructorPerformance());
        return "ui/analytics";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        Map<String, Object> results = searchService.globalSearch(q);
        model.addAttribute("results", results);
        model.addAttribute("query", q);
        return "ui/search";
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchApi(@RequestParam String q) {
        return ResponseEntity.ok(searchService.globalSearch(q));
    }
}
