package com.onlinequiz.common.validation;

import java.util.List;

public record ValidationErrorResponse(List<FieldErrorResponse> errors) {
}
