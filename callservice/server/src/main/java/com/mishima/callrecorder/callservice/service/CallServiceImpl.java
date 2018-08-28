package com.mishima.callrecorder.callservice.service;

import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.persistence.CallRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallServiceImpl implements CallService {

  private final CallRepository callRepository;

  public CallServiceImpl(CallRepository callRepository) {
    this.callRepository = callRepository;
  }

  @Override
  public Call save(Call call) {
    return callRepository.save(call);
  }

  @Override
  public Optional<Call> findBySid(String sid) {
    return callRepository.findBySid(sid);
  }

  @Override
  public List<Call> findByAccountId(String accountId) {
    return callRepository.findByAccountIdOrderByCreatedDesc(accountId);
  }
}
