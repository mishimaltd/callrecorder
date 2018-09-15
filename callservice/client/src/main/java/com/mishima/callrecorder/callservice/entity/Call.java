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
  private boolean trial;
  private int duration;

  // Billing details
  private int costInCents;
  private boolean paid;
  private long paymentDate;

  private long created;
  private long lastUpdated;

  // Recording details
  private String recordingUrl;
  private int recordingDuration;
  private String s3recordingUrl;
  private String transcriptionJobId;
  private String s3transcriptionUrl;


  private Call() {
  }

  public static Builder builder() {
    return new Builder();
  }


  public static final class Builder {

    private String id;
    private String accountId;
    private String sid;
    private String status;
    private String from;
    private String to;
    private boolean trial;
    private int duration;
    // Billing details
    private int costInCents;
    private boolean paid;
    private long paymentDate;
    private long created;
    private long lastUpdated;
    // Recording details
    private String recordingUrl;
    private int recordingDuration;
    private String s3recordingUrl;
    private String transcriptionJobId;
    private String s3transcriptionUrl;

    private Builder() {
    }

    public static Builder aCall() {
      return new Builder();
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

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

    public Builder trial(boolean trial) {
      this.trial = trial;
      return this;
    }

    public Builder duration(int duration) {
      this.duration = duration;
      return this;
    }

    public Builder costInCents(int costInCents) {
      this.costInCents = costInCents;
      return this;
    }

    public Builder paid(boolean paid) {
      this.paid = paid;
      return this;
    }

    public Builder paymentDate(long paymentDate) {
      this.paymentDate = paymentDate;
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

    public Builder recordingDuration(int recordingDuration) {
      this.recordingDuration = recordingDuration;
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

    public Call build() {
      Call call = new Call();
      call.setId(id);
      call.setAccountId(accountId);
      call.setSid(sid);
      call.setStatus(status);
      call.setFrom(from);
      call.setTo(to);
      call.setTrial(trial);
      call.setDuration(duration);
      call.setCostInCents(costInCents);
      call.setPaid(paid);
      call.setPaymentDate(paymentDate);
      call.setCreated(created);
      call.setLastUpdated(lastUpdated);
      call.setRecordingUrl(recordingUrl);
      call.setRecordingDuration(recordingDuration);
      call.setS3recordingUrl(s3recordingUrl);
      call.setTranscriptionJobId(transcriptionJobId);
      call.setS3transcriptionUrl(s3transcriptionUrl);
      return call;
    }
  }
}
