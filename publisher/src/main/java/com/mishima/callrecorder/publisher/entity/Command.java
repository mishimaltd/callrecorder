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
public class Command implements BaseMessage {

  public enum CommandType {
    UploadRecording,
    SendRecordingSMS,
    SendRecordingEmail,
    Billing
  }

  private CommandType commandType;

  private String callSid;

  private Map<String,Object> attributes;

  private Command() {
  }

  private Command(CommandType commandType, String callSid, Map<String,Object> attributes) {
    assert(commandType != null);
    assert(callSid != null);
    assert(attributes != null);
    this.commandType = commandType;
    this.callSid = callSid;
    this.attributes = attributes;
  }

  public String getCallSid() {
    return callSid;
  }

  public static Builder builder() {
    return new Builder();
  }


  public static final class Builder {

    private CommandType commandType;
    private String callSid;
    private Map<String,Object> attributes = new HashMap<>();

    private Builder() {
    }

    public Builder commandType(CommandType commandType) {
      this.commandType = commandType;
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

    public Command build() {
      return new Command(commandType, callSid, attributes);
    }
  }
}
