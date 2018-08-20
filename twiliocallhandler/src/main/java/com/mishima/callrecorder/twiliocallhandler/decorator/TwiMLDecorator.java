package com.mishima.callrecorder.twiliocallhandler.decorator;

import java.io.StringReader;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

@Slf4j
public class TwiMLDecorator {

  private final SAXBuilder builder = new SAXBuilder();
  private final XMLOutputter outputter = new XMLOutputter();

  public String decorate(String xml, String recordingStatusCallbackUrl) {
    try {
      Document document = builder.build(new StringReader(xml));
      Element root = document.getRootElement();
      Element dial = root.getChild("Dial");
      dial.setAttribute("recordingStatusCallback", recordingStatusCallbackUrl);
      dial.setAttribute("recordingStatusCallbackMethod", "POST");
      return outputter.outputString(root);
    } catch( Exception ex ) {
      log.error("Exception occurred decorating xml -> {}", ex);
      return xml;
    }
  }

}
