package com.mishima.callrecorder.callservice.service;

import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.persistence.CallRepository;
import com.mishima.callrecorder.callservice.service.result.CallsByAccount;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.query.Criteria;

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

  @Override
  public Map<String,List<Call>> findUnpaidCallsByAccountId() {
    return callRepository.findAllUnpaid().stream().collect(Collectors.groupingBy(Call::getAccountId));
  }

  @Override
  public void deleteCallById(String id) {
    callRepository.deleteById(id);
  }
}
