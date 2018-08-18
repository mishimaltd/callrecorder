package com.mishima.callrecorder.twiliocallhandler.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DialledNumberValidatorTest {

  private final DialledNumberValidator validator = new DialledNumberValidator();

  @Test
  public void testWhenValidNumberThenValidates() {
    assertTrue(validator.checkNumberIsValid("9195927481"));
  }

  @Test
  public void testWhenInternationalNumberThenDoesNotValidate() {
    assertFalse(validator.checkNumberIsValid("+447881626584"));
    assertFalse(validator.checkNumberIsValid("00447881626584"));
  }

  @Test
  public void testWhenPremiumNumberThenDoesNotValidate() {
    assertFalse(validator.checkNumberIsValid("19009634465"));
  }


}
