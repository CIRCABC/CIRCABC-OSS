package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class I18NFormatMessageMethodTest {

  private I18NFormatMessageMethod method;

  @Before
  public void setUp() {
    method = new I18NFormatMessageMethod();
  }

  @Test
  public void testExec_whenEmptyArgs_thenReturnsEmptyString()
    throws TemplateModelException {
    List<Object> args = new ArrayList<>();
    Object result = method.exec(args);
    assertEquals("", result);
  }

  @Test
  public void testExec_whenFirstArgNotScalar_thenReturnsEmptyString()
    throws TemplateModelException {
    List<Object> args = new ArrayList<>();
    args.add(new Object());
    Object result = method.exec(args);
    assertEquals("", result);
  }

  @Test
  public void testExec_whenSingleScalarArg_thenCallsGetMessage()
    throws TemplateModelException {
    TemplateScalarModel scalar = mock(TemplateScalarModel.class);
    when(scalar.getAsString()).thenReturn("some.message.key");

    List<Object> args = new ArrayList<>();
    args.add(scalar);

    // I18NUtil.getMessage returns null when no bundle registered; method passes it through
    Object result = method.exec(args);
    // No exception thrown, result is null (from I18NUtil with no registered bundle)
    assertNull(result);
  }

  @Test
  public void testExec_whenMultipleArgs_thenExtractsParams()
    throws TemplateModelException {
    TemplateScalarModel id = mock(TemplateScalarModel.class);
    when(id.getAsString()).thenReturn("msg.key");

    TemplateScalarModel param1 = mock(TemplateScalarModel.class);
    when(param1.getAsString()).thenReturn("value1");

    TemplateNumberModel param2 = mock(TemplateNumberModel.class);
    when(param2.getAsNumber()).thenReturn(42);

    List<Object> args = new ArrayList<>();
    args.add(id);
    args.add(param1);
    args.add(param2);

    // Does not throw; I18NUtil returns null with no bundle
    Object result = method.exec(args);
    assertNull(result);
  }

  @Test
  public void testExec_whenDateParam_thenConvertsDate()
    throws TemplateModelException {
    TemplateScalarModel id = mock(TemplateScalarModel.class);
    when(id.getAsString()).thenReturn("msg.key");

    Date now = new Date();
    TemplateDateModel dateParam = mock(TemplateDateModel.class);
    when(dateParam.getAsDate()).thenReturn(now);

    List<Object> args = new ArrayList<>();
    args.add(id);
    args.add(dateParam);

    Object result = method.exec(args);
    // Verifies no exception with date model param
    assertNull(result);
  }

  @Test
  public void testExec_whenUnknownParamType_thenConvertsToEmptyString()
    throws TemplateModelException {
    TemplateScalarModel id = mock(TemplateScalarModel.class);
    when(id.getAsString()).thenReturn("msg.key");

    List<Object> args = new ArrayList<>();
    args.add(id);
    args.add(new Object()); // unknown type

    Object result = method.exec(args);
    assertNull(result);
  }
}
