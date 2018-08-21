package com.mishima.callrecorder.callservice.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@ToString
@EqualsAndHashCode
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

  private long created;
  private long lastUpdated;

  // Recording details
  private String recordingUrl;
  private String s3recordingUrl;
  private String transcriptionJobId;
  private String s3transcriptionUrl;

  // Billing details
  private double cost;
  private String invoiceStatus;
  private String billingStatus;

  private Call() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String accountId;
    private String sid;
    private String status;
    private String from;
    private String to;
    private int duration;
    private long created;
    private long lastUpdated;
    // Recording details
    private String recordingUrl;
    private String s3recordingUrl;
    private String transcriptionJobId;
    private String s3transcriptionUrl;
    // Billing details
    private double cost;
    private String invoiceStatus;
    private String billingStatus;

    public Builder accountId(String accountId) {
      this.accountId = accountId;
      return this;
    }

    public Builder sid(String sid) {
      this.sid = sid;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder from(String from) {
      this.from = from;
      return this;
    }

    public Builder to(String to) {
      this.to = to;
      return this;
    }

    public Builder duration(int duration) {
      this.duration = duration;
      return this;
    }

    public Builder created(long created) {
      this.created = created;
      return this;
    }

    public Builder lastUpdated(long lastUpdated) {
      this.lastUpdated = lastUpdated;
      return this;
    }

    public Builder recordingUrl(String recordingUrl) {
      this.recordingUrl = recordingUrl;
      return this;
    }

    public Builder s3recordingUrl(String s3recordingUrl) {
      this.s3recordingUrl = s3recordingUrl;
      return this;
    }

    public Builder transcriptionJobId(String transcriptionJobId) {
      this.transcriptionJobId = transcriptionJobId;
      return this;
    }

    public Builder s3transcriptionUrl(String s3transcriptionUrl) {
      this.s3transcriptionUrl = s3transcriptionUrl;
      return this;
    }

    public Builder cost(double cost) {
      this.cost = cost;
      return this;
    }

    public Builder invoiceStatus(String invoiceStatus) {
      this.invoiceStatus = invoiceStatus;
      return this;
    }

    public Builder billingStatus(String billingStatus) {
      this.billingStatus = billingStatus;
      return this;
    }

    public Call build() {
      Call call = new Call();
      call.transcriptionJobId = this.transcriptionJobId;
      call.status = this.status;
      call.s3transcriptionUrl = this.s3transcriptionUrl;
      call.from = this.from;
      call.s3recordingUrl = this.s3recordingUrl;
      call.created = this.created;
      call.cost = this.cost;
      call.sid = this.sid;
      call.to = this.to;
      call.accountId = this.accountId;
      call.recordingUrl = this.recordingUrl;
      call.billingStatus = this.billingStatus;
      call.invoiceStatus = this.invoiceStatus;
      call.lastUpdated = this.lastUpdated;
      call.duration = this.duration;
      return call;
    }
  }
}
