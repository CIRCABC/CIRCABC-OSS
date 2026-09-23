package eu.europa.ec.digit.circabc.rest.service.log;

import static org.junit.Assert.*;

import java.util.Date;
import org.junit.Test;

public class LogSearchCriteriaTest {

  @Test
  public void testConstructor_whenAllFieldsProvided_thenGettersReturnCorrectValues() {
    Date from = new Date(1000L);
    Date to = new Date(2000L);

    LogSearchCriteria criteria = new LogSearchCriteria(
      42L,
      "admin",
      "Library",
      "download",
      from,
      to,
      10,
      25
    );

    assertEquals(42L, criteria.getIgId());
    assertEquals("admin", criteria.getUser());
    assertEquals("Library", criteria.getService());
    assertEquals("download", criteria.getMethod());
    assertEquals(from, criteria.getFromDate());
    assertEquals(to, criteria.getToDate());
    assertEquals(10, criteria.getStartRecord());
    assertEquals(25, criteria.getPageSize());
  }

  @Test
  public void testConstructor_whenNullFields_thenGettersReturnNull() {
    LogSearchCriteria criteria = new LogSearchCriteria(
      0L,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      0,
      0
    );

    assertEquals(0L, criteria.getIgId());
    assertNull(criteria.getUser());
    assertNull(criteria.getService());
    assertNull(criteria.getMethod());
    assertNull(criteria.getFromDate());
    assertNull(criteria.getToDate());
    assertEquals(0, criteria.getStartRecord());
    assertEquals(0, criteria.getPageSize());
  }
}
