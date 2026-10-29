package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Thin client over the OpenRouter chat completions API, backed by Spring AI's
 * {@link ChatClient}. Keeps the public surface intentionally small: callers
 * hand over the conversation history and receive the assistant's reply text.
 */
@Slf4j
@Component
public class OpenRouterClient {
    //TODO 4: define and initialize a ChatClient

    public String complete(List<ChatMessage> history) {
        String reply = call(history);

        return extractContent(reply);
    }

    private String call(List<ChatMessage> history) {
        try {
            //TODO 5: make a call through ChatClient with the conversation history (including latest user message)
        } catch (OpenRouterException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("OpenRouter request failed", ex);
            throw new OpenRouterException("Failed to reach OpenRouter: " + ex.getMessage(), ex);
        }
    }

    //TODO 6: use helper methods/mappers/utility classes to construct the payload for a cleaner solution

    private String extractContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new OpenRouterException("OpenRouter returned an empty response");
        }
        return content.trim();
    }
}
