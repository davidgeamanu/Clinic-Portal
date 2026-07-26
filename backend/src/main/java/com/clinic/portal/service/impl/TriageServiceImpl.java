package com.clinic.portal.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlock;
import com.clinic.portal.dto.triage.TriageRequestDTO;
import com.clinic.portal.dto.triage.TriageResponseDTO;
import com.clinic.portal.model.Specialization;
import com.clinic.portal.repository.SpecializationRepository;
import com.clinic.portal.service.TriageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI symptom triage: recommends one of the clinic's seven fixed
 * specializations from a free-text symptom description.
 *
 * Uses the Claude API when an ANTHROPIC_API_KEY is configured; otherwise
 * falls back to a keyword classifier so the feature still works in demos
 * without a key. This is a routing aid, not medical advice — the UI shows
 * a disclaimer.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class TriageServiceImpl implements TriageService {

    private static final String MODEL = "claude-opus-4-8";

    /** Keyword fallback used when no API key is configured or the call fails. */
    private static final Map<String, List<String>> FALLBACK_KEYWORDS = Map.of(
            "Cardiology", List.of("chest pain", "palpitation", "heart", "shortness of breath", "blood pressure", "dizzy"),
            "Neurology", List.of("headache", "migraine", "numbness", "seizure", "tingling", "memory", "tremor", "vertigo"),
            "Orthopedics", List.of("back pain", "joint", "knee", "shoulder", "fracture", "sprain", "bone", "hip", "ankle"),
            "Pediatrics", List.of("child", "baby", "infant", "toddler", "my son", "my daughter"),
            "General Surgery", List.of("abdominal", "stomach pain", "hernia", "appendi", "gallbladder", "lump"),
            "Radiology", List.of("x-ray", "xray", "mri", "ct scan", "ultrasound", "imaging"),
            "Emergency", List.of("severe", "unbearable", "bleeding heavily", "unconscious", "can't breathe", "cannot breathe")
    );

    private final SpecializationRepository specializationRepository;
    private final ObjectMapper objectMapper;
    private final AnthropicClient anthropicClient;

    public TriageServiceImpl(SpecializationRepository specializationRepository,
                             ObjectMapper objectMapper,
                             @Value("${app.ai.anthropic-api-key:}") String apiKey) {
        this.specializationRepository = specializationRepository;
        this.objectMapper = objectMapper;
        this.anthropicClient = apiKey == null || apiKey.isBlank()
                ? null
                : AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        if (this.anthropicClient == null) {
            log.info("[TRIAGE] No ANTHROPIC_API_KEY configured - using keyword fallback classifier");
        }
    }

    @Override
    public TriageResponseDTO triage(TriageRequestDTO request) {
        List<Specialization> specializations = specializationRepository.findAll();

        if (anthropicClient != null) {
            try {
                return triageWithClaude(request.symptoms(), specializations);
            } catch (Exception e) {
                log.warn("[TRIAGE] Claude API call failed, using keyword fallback: {}", e.getMessage());
            }
        }
        return triageWithKeywords(request.symptoms(), specializations);
    }

    private TriageResponseDTO triageWithClaude(String symptoms, List<Specialization> specializations) {
        String departmentList = specializations.stream()
                .map(s -> "- " + s.getName() + ": " + s.getDescription())
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
                You are a triage assistant for a medical clinic. The clinic has exactly these departments:
                %s

                Given a patient's symptom description, choose the single most appropriate department.
                Respond with ONLY a JSON object (no markdown fences, no extra text) in this shape:
                {"specialization": "<one of the exact department names above>", "reasoning": "<one or two sentences addressed to the patient>", "urgency": "<ROUTINE|SOON|URGENT>"}

                If the symptoms suggest a potentially life-threatening condition, choose Emergency with urgency URGENT.
                """.formatted(departmentList);

        MessageCreateParams params = MessageCreateParams.builder()
                .model(MODEL)
                .maxTokens(1024L)
                .system(systemPrompt)
                .addUserMessage(symptoms)
                .build();

        Message message = anthropicClient.messages().create(params);

        String text = message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining())
                .trim();

        // Tolerate accidental markdown fences around the JSON
        if (text.startsWith("```")) {
            text = text.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
        }

        ClaudeTriageResult result = objectMapper.readValue(text, ClaudeTriageResult.class);

        Specialization matched = specializations.stream()
                .filter(s -> s.getName().equalsIgnoreCase(result.specialization()))
                .findFirst()
                .orElse(null);

        return new TriageResponseDTO(
                matched != null ? matched.getName() : result.specialization(),
                matched != null ? matched.getId() : null,
                result.reasoning(),
                result.urgency() != null ? result.urgency() : "ROUTINE",
                true
        );
    }

    private TriageResponseDTO triageWithKeywords(String symptoms, List<Specialization> specializations) {
        String lower = symptoms.toLowerCase(Locale.ROOT);

        String bestName = null;
        int bestScore = 0;
        for (Map.Entry<String, List<String>> entry : FALLBACK_KEYWORDS.entrySet()) {
            int score = (int) entry.getValue().stream().filter(lower::contains).count();
            // Emergency keywords outrank everything else
            if (entry.getKey().equals("Emergency") && score > 0) score += 10;
            if (score > bestScore) {
                bestScore = score;
                bestName = entry.getKey();
            }
        }

        if (bestName == null) {
            return new TriageResponseDTO(null, null,
                    "We couldn't determine a department from your description. Please pick the one that seems most relevant, or contact the clinic.",
                    "ROUTINE", false);
        }

        final String name = bestName;
        Specialization matched = specializations.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        String urgency = bestName.equals("Emergency") ? "URGENT" : "ROUTINE";
        return new TriageResponseDTO(
                bestName,
                matched != null ? matched.getId() : null,
                "Based on the symptoms you described, " + bestName + " looks like the most relevant department.",
                urgency,
                false
        );
    }

    /** Shape of the JSON Claude is instructed to return. */
    record ClaudeTriageResult(String specialization, String reasoning, String urgency) {}
}
