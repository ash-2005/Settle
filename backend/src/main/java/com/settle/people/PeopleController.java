package com.settle.people;

import com.settle.auth.SecurityConfig;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
public class PeopleController {

    private final PeopleService people;

    public PeopleController(PeopleService people) {
        this.people = people;
    }

    @PostMapping("/phone")
    public Map<String, Object> byPhone(@RequestBody Body body) {
        var person = people.addByPhone(SecurityConfig.currentUserId(), body.phone(), body.name());
        return people.view(person);
    }

    public record Body(@NotBlank String phone, String name) {}
}
