package com.mishima.callrecorder.app.thymeleaf.dialect;

import java.util.LinkedHashSet;
import java.util.Set;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.extras.springsecurity4.dialect.expression.SpringSecurityExpressionObjectFactory;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.templatemode.TemplateMode;

public class SecurityDialect extends AbstractDialect implements IProcessorDialect,
    IExpressionObjectDialect {

  public static final String NAME = "SpringSecurityAuthentication";
  public static final String DEFAULT_PREFIX = "secu";
  public static final int PROCESSOR_PRECEDENCE = 1000;

  public static final IExpressionObjectFactory EXPRESSION_OBJECT_FACTORY = new SpringSecurityExpressionObjectFactory();


  public SecurityDialect() {
    super(NAME);
  }


  public String getPrefix() {
    return DEFAULT_PREFIX;
  }

  public int getDialectProcessorPrecedence() {
    return PROCESSOR_PRECEDENCE;
  }

  public Set<IProcessor> getProcessors(final String dialectPrefix) {

    final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();

    final TemplateMode[] templateModes =
        new TemplateMode[] {
            TemplateMode.HTML, TemplateMode.XML,
            TemplateMode.TEXT, TemplateMode.JAVASCRIPT, TemplateMode.CSS };

    for (final TemplateMode templateMode : templateModes) {
      processors.add(new AuthenticatedProcessor(templateMode, dialectPrefix, AuthenticatedProcessor.ATTR_NAME));
    }

    return processors;

  }

  public IExpressionObjectFactory getExpressionObjectFactory() {
    return EXPRESSION_OBJECT_FACTORY;
  }

}
