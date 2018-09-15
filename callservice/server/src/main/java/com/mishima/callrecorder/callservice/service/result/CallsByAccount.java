package com.mishima.callrecorder.callservice.service.result;

import com.mishima.callrecorder.callservice.entity.Call;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CallsByAccount {

  private String accountId;
  private List<Call> calls;

}
