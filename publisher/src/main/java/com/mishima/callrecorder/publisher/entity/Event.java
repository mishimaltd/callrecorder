package com.mishima.callrecorder.publisher.entity;

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
public class Event {

  public enum EventType {
    CallInitiated,
    CallRecordingCompleted,
    CallEnded,
    CallRecordingUploaded,
    CallTranscriptionSubmitted,
    CallTranscriptionCompleted,
    BillingSubmitted,
    BillingCompleted,
    InvoiceCompleted,
    Error
  }

  private EventType eventType;
  private Map<String,Object> attributes;

  private Event() {
  }

  private Event(EventType eventType, Map<String,Object> attributes) {
    assert(eventType != null);
    assert(attributes != null);
    this.eventType = eventType;
    this.attributes = attributes;
  }

  public static Builder builder() {
    return new Builder();
  }


  public static final class Builder {

    private EventType eventType;
    private Map<String,Object> attributes = new HashMap<>();

    private Builder() {
    }

    public Builder eventType(EventType eventType) {
      this.eventType = eventType;
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
      return new Event(eventType, attributes);
    }
  }
}
