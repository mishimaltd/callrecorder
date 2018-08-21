package com.mishima.callrecorder.callservice.controller;

import com.amazonaws.util.IOUtils;
import com.mishima.callrecorder.s3service.service.S3Service;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class RecordingController {

  @Autowired
  private S3Service s3Service;

  @GetMapping("/recording/{key}")
  public void download(@PathVariable("key") String key, HttpServletResponse res) throws Exception {
    res.setContentType("audio/wav");
    IOUtils.copy(s3Service.download(key), res.getOutputStream());
  }

}
