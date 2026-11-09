package io.cloudNativeData.ai.assistance.domain;

import lombok.Builder;

@Builder
public record QuestionAnswer(boolean answered,
                             String answer,
                             String reasonIfNotAnswered) {

}
