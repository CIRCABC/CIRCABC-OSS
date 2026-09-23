package eu.europa.ec.digit.circabc.rest.template;

import static org.junit.Assert.*;

import freemarker.template.TemplateModelException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ConcatAsStringMethodTest {

  private ConcatAsStringMethod method;

  @Before
  public void setUp() {
    method = new ConcatAsStringMethod();
  }

  @Test
  public void testExec_whenMultipleArgs_thenConcatenated()
    throws TemplateModelException {
    @SuppressWarnings("rawtypes")
    List args = Arrays.asList("hello", " ", "world");
    Object result = method.exec(args);
    assertEquals("hello world", result);
  }

  @Test
  public void testExec_whenEmptyList_thenEmptyString()
    throws TemplateModelException {
    @SuppressWarnings("rawtypes")
    List args = Collections.emptyList();
    Object result = method.exec(args);
    assertEquals("", result);
  }

  @Test
  public void testExec_whenNullElements_thenSkipped()
    throws TemplateModelException {
    @SuppressWarnings("rawtypes")
    List args = Arrays.asList("a", null, "b");
    Object result = method.exec(args);
    assertEquals("ab", result);
  }

  @Test
  public void testExec_whenNonStringObjects_thenUsesToString()
    throws TemplateModelException {
    @SuppressWarnings("rawtypes")
    List args = Arrays.asList(Integer.valueOf(42), Boolean.TRUE);
    Object result = method.exec(args);
    assertEquals("42true", result);
  }

  @Test
  public void testExec_whenAllNull_thenEmptyString()
    throws TemplateModelException {
    @SuppressWarnings("rawtypes")
    List args = Arrays.asList(null, null, null);
    Object result = method.exec(args);
    assertEquals("", result);
  }
}
