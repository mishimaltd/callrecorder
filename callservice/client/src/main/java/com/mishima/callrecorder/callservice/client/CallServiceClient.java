package com.mishima.callrecorder.callservice.client;

import com.mishima.callrecorder.callservice.entity.Call;
import java.util.Optional;

public interface CallServiceClient {

  Call saveCall(Call call);

  Optional<Call> findByCallSid(String callSid);

}
