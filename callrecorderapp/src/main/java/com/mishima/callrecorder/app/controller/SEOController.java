package com.mishima.callrecorder.app.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SEOController {

  @RequestMapping(value = "/robots.txt")
  public void robots(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.getWriter().write("User-agent: *\nDisallow: /private/\nDisallow: /contact\nDisallow: /forgot\nDisallow: /login\nDisallow: /privacy\nDisallow: /register\nDisallow: /reset\nDisallow: /terms\n");
  }

}
