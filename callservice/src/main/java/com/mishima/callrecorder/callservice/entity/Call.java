package com.mishima.callrecorder.callservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Call {

  @Id
  private String id;
  @Indexed
  private String accountId;

  @Indexed
  private String sid;
  private String status;
  private String from;
  private String to;
  private int duration;

  // Recording details
  private String recordingUrl;
  private String s3recordingUrl;
  private String transcriptionJobId;
  private String s3transcriptionUrl;

  // Billing details
  private double cost;
  private String invoiceStatus;
  private String billingStatus;


}
