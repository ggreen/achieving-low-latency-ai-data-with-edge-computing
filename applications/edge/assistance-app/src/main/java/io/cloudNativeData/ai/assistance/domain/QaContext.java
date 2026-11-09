package io.cloudNativeData.ai.assistance.domain;


import lombok.Builder;

@Builder
public record QaContext(String id, String answer) {
}
