package com.mishima.callrecorder.event.service;

import static org.junit.Assert.assertEquals;

import com.mishima.callrecorder.eventhandler.service.RecordingCostService;
import org.junit.Test;

public class RecordingCostServiceTest {

  private RecordingCostService service = new RecordingCostService();

  @Test
  public void givenZeroDurationThenExpectZeroCost() {
    assertEquals(0, service.calculateCost(0));
  }

  @Test
  public void givenExactMinutesDurationThenExpectUnroundedCost() {
    assertEquals(2 * RecordingCostService.costPerMinute, service.calculateCost(120));
  }

  @Test
  public void givenFractionMinutesDurationThenExpectRoundedCost() {
    assertEquals(3 * RecordingCostService.costPerMinute, service.calculateCost(121));
  }

}
