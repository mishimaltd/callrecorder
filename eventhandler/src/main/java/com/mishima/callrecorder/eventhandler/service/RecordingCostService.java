package com.mishima.callrecorder.eventhandler.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordingCostService {

  public static int costPerMinute = 10;

  public int calculateCost(int recordingDurationInSeconds) {
    int durationInMinutes = 0;
    if( recordingDurationInSeconds != 0 ) {
      int roundUp = recordingDurationInSeconds % 60 == 0? 0: 1;
      durationInMinutes = recordingDurationInSeconds / 60 + roundUp;
    }
    int cost = durationInMinutes * costPerMinute;
    log.info("Call duration {}minutes, total cost in cents: {}", durationInMinutes, cost);
    return cost;
  }

}
