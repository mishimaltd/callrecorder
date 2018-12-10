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
    response.getWriter().write("User-agent: *\nDisallow: /private/\nDisallow: /contact\nDisallow: /forgot\nDisallow: /login\nDisallow: /privacy\nDisallow: /register\nDisallow: /reset\nDisallow: /terms\n\nSitemap: https://www.mydialbuddy.com/sitemap.xml");
  }

  @RequestMapping(value = "/sitemap.xml")
  public void sitemap(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
        + "  <url>\n"
        + "    <loc>http://www.mydialbuddy.com/</loc>\n"
        + "  </url>\n"
        + "  <url>\n"
        + "    <loc>http://www.mydialbuddy.com/public/trial</loc>\n"
        + "  </url>\n"
        + "  <url>\n"
        + "    <loc>http://www.mydialbuddy.com/public/contact</loc>\n"
        + "  </url>\n"
        + "  <url>\n"
        + "    <loc>http://www.mydialbuddy.com/public/terms</loc>\n"
        + "  </url>\n"
        + "  <url>\n"
        + "    <loc>http://www.mydialbuddy.com/public/privacy</loc>\n"
        + "  </url>\n"
        + "</urlset>\n");
  }

}
