package org.umc.valuedi.infra.genai.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.umc.valuedi.infra.genai.config.GeminiProperties;
import org.umc.valuedi.infra.genai.exception.GeminiException;
import org.umc.valuedi.infra.genai.exception.code.GeminiErrorCode;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final Client genaiClient;
    private final GeminiProperties geminiProperties;

    public String generateText(String prompt) {
        try {
            GenerateContentResponse response = genaiClient.models.generateContent(geminiProperties.getModel(), prompt, null);

            String text = response.text();
            if (text == null || text.isBlank()) {
                throw new GeminiException(GeminiErrorCode.EMPTY_RESPONSE);
            }
            return text;
        } catch (GeminiException e) {
            throw e;
        } catch (Exception e) {
            throw new GeminiException(GeminiErrorCode.GEMINI_CALL_FAILED);
        }
    }
}
