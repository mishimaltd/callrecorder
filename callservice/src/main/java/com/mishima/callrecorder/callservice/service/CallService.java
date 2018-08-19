package com.mishima.callrecorder.callservice.service;

import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.persistence.CallRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CallService {

  @Autowired
  private CallRepository callRepository;

  public void saveCall(Call call) {
    callRepository.save(call);
  }

  public Call findBySid(String sid) {
    return callRepository.findBySid(sid);
  }

  public void deleteById(String id) {
    callRepository.deleteById(id);
  }


}
