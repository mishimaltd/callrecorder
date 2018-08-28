package com.mishima.callrecorder.callservice.service;

import com.mishima.callrecorder.callservice.entity.Call;
import java.util.List;
import java.util.Optional;

public interface CallService {

  Call save(Call call);

  Optional<Call> findBySid(String sid);

  List<Call> findByAccountId(String accountId);

}
