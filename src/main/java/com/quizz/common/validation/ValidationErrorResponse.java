package com.quizz.common.validation;

import java.util.List;

public record ValidationErrorResponse(List<FieldErrorResponse> errors) {
}
