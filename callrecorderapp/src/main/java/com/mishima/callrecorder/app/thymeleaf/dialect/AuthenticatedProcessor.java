package com.mishima.callrecorder.app.thymeleaf.dialect;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.standard.processor.AbstractStandardConditionalVisibilityTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

public class AuthenticatedProcessor extends AbstractStandardConditionalVisibilityTagProcessor {

  public static final int ATTR_PRECEDENCE = 300;
  public static final String ATTR_NAME = "authentication";

  private static final String AUTHENTICATED = "authenticated";
  private static final String ANONYMOUS = "anonymous";

  public AuthenticatedProcessor(final TemplateMode templateMode, final String dialectPrefix, final String attrName) {
    super(templateMode, dialectPrefix, attrName, ATTR_PRECEDENCE);
  }


  @Override
  protected boolean isVisible(
      final ITemplateContext context, final IProcessableElementTag tag,
      final AttributeName attributeName, final String attributeValue) {

    final String attrValue = (attributeValue == null? null : attributeValue.trim());

    if(!AUTHENTICATED.equals(attrValue) && !ANONYMOUS.equals(attrValue)) {
      return false;
    }

    final SecurityContext securityContext = SecurityContextHolder.getContext();
    if( securityContext == null || securityContext.getAuthentication() == null) {
      return ANONYMOUS.equals(attrValue);
    } else {
      return AUTHENTICATED.equals(attrValue);
    }

  }

}
