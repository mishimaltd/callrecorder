package com.mishima.callrecorder.domain.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.domain.entity.Event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Slf4j
public class EventTest {

  private final ObjectMapper om = new ObjectMapper();

  @Test
  public void testSerialization() throws Exception {
    Event event = Event.builder().eventType(EventType.CallRecordingCompleted).attribute("CallSid", "113").attribute("AccountId", "2223").build();
    String json = om.writeValueAsString(event);
    log.info("Serialized as {}", json);
    Event deserialized = om.readValue(json, new TypeReference<Event>(){});
    assertEquals(event, deserialized);
  }

}
