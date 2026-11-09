package io.cloudNativeData.ai.assistance.domain;

import lombok.Builder;

@Builder
public record PromptContext(String promptText,String context) {
}
