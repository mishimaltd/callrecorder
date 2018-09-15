package com.mishima.callrecorder.domain.validation;

public interface EntityValidator<T> {

  ValidationResult validate(T entity);

}
