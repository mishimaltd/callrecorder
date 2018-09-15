package com.mishima.callrecorder.callservice.service;

import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.service.result.CallsByAccount;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CallService {

  Call save(Call call);

  Optional<Call> findBySid(String sid);

  List<Call> findByAccountId(String accountId);

  Map<String,List<Call>> findUnpaidCallsByAccountId();

  void deleteCallById(String id);

}
