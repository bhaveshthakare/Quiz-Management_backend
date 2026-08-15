package com.quizplatform.backend.controller;

import com.quizplatform.backend.dto.UserDtos.*;
import com.quizplatform.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentSummary>> students() {
        return ResponseEntity.ok(userService.listStudents());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StudentSummary> setStatus(@PathVariable Long id,
                                                    @Valid @RequestBody StatusRequest req) {
        return ResponseEntity.ok(userService.setStatus(id, req));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<ProfileResponse> profile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.profile(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Student deleted"));
    }
}