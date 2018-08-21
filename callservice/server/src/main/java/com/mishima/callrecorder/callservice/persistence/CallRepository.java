package com.mishima.callrecorder.callservice.persistence;

import com.mishima.callrecorder.callservice.entity.Call;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface CallRepository extends CrudRepository<Call, String> {

  Optional<Call> findBySid(String sid);

}
