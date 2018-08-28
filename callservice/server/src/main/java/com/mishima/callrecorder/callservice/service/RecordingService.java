package com.mishima.callrecorder.callservice.service;

import java.io.InputStream;

public interface RecordingService {

  InputStream download(String key);

}
