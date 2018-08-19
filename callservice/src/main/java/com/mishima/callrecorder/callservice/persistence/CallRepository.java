package com.mishima.callrecorder.callservice.persistence;

import com.mishima.callrecorder.callservice.entity.Call;
import org.springframework.data.repository.CrudRepository;

public interface CallRepository extends CrudRepository<Call, String> {

}
