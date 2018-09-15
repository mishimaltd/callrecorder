package com.mishima.callrecorder.domain.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationResult {

  private Map<String, List<String>> fieldErrors;
  private List<String> globalErrors;

  public ValidationResult(
      Map<String, List<String>> fieldErrors, List<String> globalErrors) {
    this.fieldErrors = fieldErrors;
    this.globalErrors = globalErrors;
  }

  public boolean hasFieldErrors() {
    return !fieldErrors.isEmpty();
  }

  public boolean hasGlobalErrors() {
    return !globalErrors.isEmpty();
  }

  public boolean isSuccess() {
    return !hasFieldErrors() && !hasGlobalErrors();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Map<String,List<String>> fieldErrors = new HashMap<>();
    private List<String> globalErrors = new ArrayList<>();

    private Builder() {
    }

    public Builder fieldError(String fieldName, String validationError) {
      fieldErrors.putIfAbsent(fieldName, new ArrayList<>());
      fieldErrors.get(fieldName).add(validationError);
      return this;
    }

    public Builder globalError(String validationError) {
      globalErrors.add(validationError);
      return this;
    }

    public ValidationResult build() {
      return new ValidationResult(fieldErrors, globalErrors);
    }
  }
}
