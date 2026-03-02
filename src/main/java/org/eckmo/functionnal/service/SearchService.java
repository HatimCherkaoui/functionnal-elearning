package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import org.eckmo.functionnal.dto.CourseDTO;
import org.eckmo.functionnal.dto.UserDTO;
import org.eckmo.functionnal.repository.CourseRepository;
import org.eckmo.functionnal.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Map<String, Object> globalSearch(String query) {
        Map<String, Object> results = new HashMap<>();
        if (query == null || query.isBlank()) {
            results.put("courses", List.of());
            results.put("users", List.of());
            results.put("total", 0);
            return results;
        }

        var courses = courseRepository.searchCourses(query, PageRequest.of(0, 5))
                .stream()
                .map(c -> CourseDTO.builder()
                        .id(c.getId()).title(c.getTitle()).category(c.getCategory())
                        .level(c.getLevel().name()).price(c.getPrice()).isPublished(c.getIsPublished())
                        .rating(c.getRating()).build())
                .collect(Collectors.toList());

        var users = userRepository.searchUsers(query, PageRequest.of(0, 5))
                .stream()
                .map(u -> UserDTO.builder()
                        .id(u.getId()).firstName(u.getFirstName()).lastName(u.getLastName())
                        .email(u.getEmail()).role(u.getRole().name())
                        .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : "")
                        .build())
                .collect(Collectors.toList());

        results.put("courses", courses);
        results.put("users", users);
        results.put("total", courses.size() + users.size());
        results.put("query", query);
        return results;
    }
}

