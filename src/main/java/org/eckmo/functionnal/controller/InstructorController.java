package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import org.eckmo.functionnal.service.CourseService;
import org.eckmo.functionnal.service.EnrollmentService;
import org.eckmo.functionnal.service.SubscriptionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ui/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
public class InstructorController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final SubscriptionService subscriptionService;

    @GetMapping
    public String dashboard(Model model) {
        var courses = courseService.getAllCourses();

        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courses.size());
        model.addAttribute("totalEnrollments", enrollmentService.getAllEnrollments().size());

        return "instructor/dashboard";
    }

    @GetMapping("/{courseId}/design")
    public String designCourse(@PathVariable Long courseId, Model model) {
        var course = courseService.getCourseById(courseId);
        model.addAttribute("course", course);
        model.addAttribute("courseId", courseId);

        return "instructor/course-designer";
    }

    @GetMapping("/{courseId}/analytics")
    public String courseAnalytics(@PathVariable Long courseId, Model model) {
        var course = courseService.getCourseById(courseId);
        var subscribers = subscriptionService.countCourseSubscribers(courseId);
        var enrollments = enrollmentService.getAllEnrollments().stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .toList();

        model.addAttribute("course", course);
        model.addAttribute("subscribers", subscribers);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("totalEnrollments", enrollments.size());
        model.addAttribute("completedCount", enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count());

        return "instructor/course-analytics";
    }
}

