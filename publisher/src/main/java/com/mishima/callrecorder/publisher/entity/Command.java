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
public class Command {

  public enum CommandType {
    UploadRecording,
    Billing
  }

  private CommandType commandType;
  private Map<String,Object> attributes;

  private Command() {
  }

  private Command(CommandType commandType, Map<String,Object> attributes) {
    assert(commandType != null);
    assert(attributes != null);
    this.commandType = commandType;
    this.attributes = attributes;
  }

  public static Builder builder() {
    return new Builder();
  }


  public static final class Builder {

    private CommandType commandType;
    private Map<String,Object> attributes = new HashMap<>();

    private Builder() {
    }

    public Builder commandType(CommandType commandType) {
      this.commandType = commandType;
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
      return new Command(commandType, attributes);
    }
  }
}
