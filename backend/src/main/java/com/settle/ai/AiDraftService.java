package com.settle.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.settle.auth.SecurityConfig;
import com.settle.common.ApiException;
import com.settle.groups.GroupMember;
import com.settle.groups.GroupService;
import com.settle.people.PeopleService;
import com.settle.people.Person;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiDraftService {

    private final GroupService groups;
    private final PeopleService people;
    private final ObjectMapper json;
    private final String geminiKey;

    public AiDraftService(
            GroupService groups,
            PeopleService people,
            ObjectMapper json,
            @Value("${settle.ai.gemini-key:}") String geminiKey) {
        this.groups = groups;
        this.people = people;
        this.json = json;
        this.geminiKey = geminiKey == null ? "" : geminiKey.trim();
    }

    public Map<String, Object> status() {
        return Map.of(
                "llmConfigured", !geminiKey.isBlank(),
                "note", "AI only suggests a draft. You confirm. Java still writes the expense.");
    }

    public Map<String, Object> draft(UUID userId, String text, UUID groupId) {
        Person me = people.requireForUser(userId);
        List<ExpenseDraftParser.NamedPerson> named = new ArrayList<>();
        named.add(new ExpenseDraftParser.NamedPerson(me.getId(), me.getDisplayName()));
        if (groupId != null) {
            groups.visibleGroup(userId, groupId);
            for (GroupMember m : groups.activeMembers(groupId)) {
                Person p = people.require(m.getPersonId());
                named.add(new ExpenseDraftParser.NamedPerson(p.getId(), p.getDisplayName()));
            }
        }
        if (!geminiKey.isBlank()) {
            try {
                Map<String, Object> llm = geminiDraft(text, me, named);
                llm.put("source", "gemini");
                llm.put("note", "Gemini suggested this. Review every rupee before Add expense.");
                return llm;
            } catch (Exception ignored) {
                // fall through to parser
            }
        }
        return ExpenseDraftParser.parse(text, me.getId(), named);
    }

    private Map<String, Object> geminiDraft(String text, Person me, List<ExpenseDraftParser.NamedPerson> named) throws Exception {
        StringBuilder peopleLines = new StringBuilder();
        for (var p : named) {
            peopleLines.append("- ").append(p.id()).append(" = ").append(p.displayName()).append("\n");
        }
        String prompt = """
                Extract a shared expense draft as JSON only, no markdown.
                Keys: amount (string rupees), description (string), payerId (uuid), participantIds (uuid array).
                Current user id: %s name: %s
                People:
                %s
                User said: %s
                Use only ids from the list. Prefer equal split. If unsure, include the current user as payer.
                """.formatted(me.getId(), me.getDisplayName(), peopleLines, text);

        String body = json.writeValueAsString(Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiKey))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            throw ApiException.bad("Gemini request failed");
        }
        JsonNode root = json.readTree(res.body());
        String textOut = root.at("/candidates/0/content/parts/0/text").asText("");
        textOut = textOut.replace("```json", "").replace("```", "").trim();
        JsonNode draft = json.readTree(textOut);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("amount", draft.path("amount").asText());
        out.put("description", draft.path("description").asText("Expense"));
        out.put("payerId", draft.path("payerId").asText(me.getId().toString()));
        List<String> ids = new ArrayList<>();
        draft.path("participantIds").forEach(n -> ids.add(n.asText()));
        if (ids.isEmpty()) {
            ids.add(me.getId().toString());
        }
        out.put("participantIds", ids);
        out.put("splitMethod", "EQUAL");
        return out;
    }
}
