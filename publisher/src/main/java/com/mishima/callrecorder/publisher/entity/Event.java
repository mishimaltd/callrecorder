package com.mishima.callrecorder.publisher.entity;

import com.mishima.callrecorder.domain.entity.BaseMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Event implements BaseMessage {

  public enum EventType {
    CallInitiated,
    CallRecordingCompleted,
    CallEnded,
    CallRecordingUploaded,
    SMSNotificationSent,
    Error
  }

  private EventType eventType;
  private String callSid;
  private Map<String,Object> attributes;

  private Event() {
  }

  private Event(EventType eventType, String callSid, Map<String,Object> attributes) {
    assert(eventType != null);
    assert(callSid != null);
    assert(attributes != null);
    this.eventType = eventType;
    this.callSid = callSid;
    this.attributes = attributes;
  }

  public static Builder builder() {
    return new Builder();
  }


  public static final class Builder {

    private EventType eventType;
    private String callSid;
    private Map<String,Object> attributes = new HashMap<>();

    private Builder() {
    }

    public Builder eventType(EventType eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder callSid(String callSid) {
      this.callSid = callSid;
      return this;
    }

    public Builder attribute(String key, Object value) {
      assert(key != null);
      assert(value != null);
      this.attributes.put(key,value);
      return this;
    }


    public Builder attributes(Map<String, Object> attributes) {
      this.attributes = attributes;
      return this;
    }

    public Event build() {
      return new Event(eventType, callSid, attributes);
    }
  }
}
