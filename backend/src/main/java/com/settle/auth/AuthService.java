package com.settle.auth;

import com.settle.common.ApiException;
import com.settle.common.Phones;
import com.settle.people.Person;
import com.settle.people.PersonRepository;
import com.settle.users.UserAccount;
import com.settle.users.UserAccountRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository users;
    private final PersonRepository people;
    private final JwtService jwt;
    private final String localOtp;
    private final ConcurrentHashMap<String, Instant> lastOtp = new ConcurrentHashMap<>();

    public AuthService(
            UserAccountRepository users,
            PersonRepository people,
            JwtService jwt,
            @Value("${settle.otp}") String localOtp) {
        this.users = users;
        this.people = people;
        this.jwt = jwt;
        this.localOtp = localOtp;
    }

    public void requestOtp(String rawPhone) {
        String phone = Phones.normalize(rawPhone);
        Instant now = Instant.now();
        Instant prev = lastOtp.get(phone);
        if (prev != null && prev.plusSeconds(3).isAfter(now)) {
            throw ApiException.bad("Wait a few seconds before requesting another code");
        }
        lastOtp.put(phone, now);
        log.info("OTP for {} is {} (local mock — not SMS)", phone, localOtp);
    }

    @Transactional
    public Map<String, Object> verify(String rawPhone, String code, String displayName, String username) {
        String phone = Phones.normalize(rawPhone);
        if (!localOtp.equals(code == null ? "" : code.trim())) {
            throw ApiException.bad("Wrong code. Local Docker always uses " + localOtp);
        }
        UserAccount user = users.findByPhone(phone).orElseGet(() -> register(phone, displayName, username));
        Person person = people.findByUserId(user.getId()).orElseThrow(ApiException::notFound);
        return Map.of(
                "accessToken", jwt.accessToken(user.getId()),
                "refreshToken", jwt.refreshToken(user.getId()),
                "user", toMe(user, person));
    }

    public Map<String, Object> refresh(String refreshToken) {
        UUID userId = jwt.parseRefresh(refreshToken);
        UserAccount user = users.findById(userId).orElseThrow(ApiException::unauthorized);
        Person person = people.findByUserId(user.getId()).orElseThrow(ApiException::notFound);
        return Map.of(
                "accessToken", jwt.accessToken(user.getId()),
                "refreshToken", jwt.refreshToken(user.getId()),
                "user", toMe(user, person));
    }

    public Map<String, Object> me(UUID userId) {
        UserAccount user = users.findById(userId).orElseThrow(ApiException::unauthorized);
        Person person = people.findByUserId(user.getId()).orElseThrow(ApiException::notFound);
        return toMe(user, person);
    }

    private UserAccount register(String phone, String displayName, String username) {
        String name = (displayName == null || displayName.isBlank()) ? ("User " + phone.substring(phone.length() - 4)) : displayName.trim();
        String handle = username == null || username.isBlank() ? defaultUsername(phone) : username.trim().replaceFirst("^@", "");
        if (users.existsByUsername(handle)) {
            handle = handle + phone.substring(phone.length() - 4);
        }
        UserAccount user = users.save(UserAccount.create(phone, handle, name));
        Person existing = people.findByPhone(phone).orElse(null);
        if (existing == null) {
            people.save(Person.forUser(user.getId(), phone, name));
        } else {
            existing.attachUser(user.getId(), name);
            people.save(existing);
        }
        return user;
    }

    private String defaultUsername(String phone) {
        return "u" + phone.replace("+", "");
    }

    private Map<String, Object> toMe(UserAccount user, Person person) {
        return Map.of(
                "id", user.getId(),
                "personId", person.getId(),
                "phone", user.getPhone(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName());
    }
}
