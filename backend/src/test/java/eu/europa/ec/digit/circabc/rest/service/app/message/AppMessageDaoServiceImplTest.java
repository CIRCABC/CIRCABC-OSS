package eu.europa.ec.digit.circabc.rest.service.app.message;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.db.AppMessageDAO;
import io.swagger.model.db.DistributionEmailDAO;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class AppMessageDaoServiceImplTest {

  private AppMessageDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() {
    service = new AppMessageDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(sqlSessionTemplate);
  }

  @Test
  public void testSelectAppMessageTemplates_whenValidPageAndLimit_thenReturnsResults() {
    AppMessageDAO dao = new AppMessageDAO();
    dao.setId(1);
    dao.setMessageContent("test");
    List<AppMessageDAO> expected = Arrays.asList(dao);

    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("AppMessage.select_app_messages"), any(Map.class));

    List<AppMessageDAO> result = service.selectAppMessageTemplates(2, 10);

    assertEquals(1, result.size());
    assertEquals(Integer.valueOf(1), result.get(0).getId());
    verify(sqlSessionTemplate).selectList(
      eq("AppMessage.select_app_messages"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return (
          Integer.valueOf(1).equals(m.get("page")) &&
          Integer.valueOf(10).equals(m.get("limit")) &&
          Integer.valueOf(10).equals(m.get("limitMin"))
        );
      })
    );
  }

  @Test
  public void testSelectAppMessageTemplates_whenPageZero_thenNoPageParam() {
    doReturn(Arrays.asList())
      .when(sqlSessionTemplate)
      .selectList(eq("AppMessage.select_app_messages"), any(Map.class));

    List<AppMessageDAO> result = service.selectAppMessageTemplates(0, 5);

    assertTrue(result.isEmpty());
    verify(sqlSessionTemplate).selectList(
      eq("AppMessage.select_app_messages"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return !m.containsKey("page");
      })
    );
  }

  @Test
  public void testAddAppMessageTemplate_whenCalled_thenInsertsWithCorrectParams() {
    Date closure = new Date();

    service.addAppMessageTemplate("content", closure, "info", 5, true);

    verify(sqlSessionTemplate).insert(
      eq("AppMessage.insert_app_message"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return (
          "content".equals(m.get("content")) &&
          closure.equals(m.get("dateClosure")) &&
          "info".equals(m.get("level")) &&
          Integer.valueOf(5).equals(m.get("displayTime")) &&
          Boolean.TRUE.equals(m.get("enabled"))
        );
      })
    );
  }

  @Test
  public void testUpdateAppMessageTemplate_whenCalled_thenUpdatesWithCorrectParams() {
    Date closure = new Date();

    service.updateAppMessageTemplate(
      42,
      "updated",
      closure,
      "warning",
      10,
      false
    );

    verify(sqlSessionTemplate).update(
      eq("AppMessage.update_app_message"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return (
          Integer.valueOf(42).equals(m.get("id")) &&
          "updated".equals(m.get("content")) &&
          "warning".equals(m.get("level")) &&
          Integer.valueOf(10).equals(m.get("displayTime")) &&
          Boolean.FALSE.equals(m.get("enabled"))
        );
      })
    );
  }

  @Test
  public void testDeleteAppMessageTemplate_whenCalled_thenDeletesById() {
    service.deleteAppMessageTemplate(7);

    verify(sqlSessionTemplate).delete(
      eq("AppMessage.delete_app_message"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return Integer.valueOf(7).equals(m.get("id"));
      })
    );
  }

  @Test
  public void testGetMessageTemplate_whenExists_thenReturnsDao() {
    AppMessageDAO dao = new AppMessageDAO();
    dao.setId(3);
    when(
      sqlSessionTemplate.selectOne(
        eq("AppMessage.select_app_message"),
        any(Map.class)
      )
    ).thenReturn(dao);

    AppMessageDAO result = service.getMessageTemplate(3);

    assertNotNull(result);
    assertEquals(Integer.valueOf(3), result.getId());
  }

  @Test
  public void testCountAppMessageTemplates_whenCalled_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne("AppMessage.count_app_messages")
    ).thenReturn(15);

    Integer count = service.countAppMessageTemplates();

    assertEquals(Integer.valueOf(15), count);
  }

  @Test
  public void testSelectDistributionEmails_whenSearchIsNull_thenUsesEmptyString() {
    doReturn(Arrays.asList())
      .when(sqlSessionTemplate)
      .selectList(eq("AppMessage.select_distribution_emails"), any(Map.class));

    service.selectDistributionEmails(1, 10, null);

    verify(sqlSessionTemplate).selectList(
      eq("AppMessage.select_distribution_emails"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return "".equals(m.get("search"));
      })
    );
  }

  @Test
  public void testSelectDistributionEmails_whenSearchProvided_thenUsesLowerCase() {
    doReturn(Arrays.asList())
      .when(sqlSessionTemplate)
      .selectList(eq("AppMessage.select_distribution_emails"), any(Map.class));

    service.selectDistributionEmails(1, 10, "TEST");

    verify(sqlSessionTemplate).selectList(
      eq("AppMessage.select_distribution_emails"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return "test".equals(m.get("search"));
      })
    );
  }

  @Test
  public void testCountDistributionEmails_whenQueryProvided_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne(
        eq("AppMessage.count_distribution_emails"),
        any(Map.class)
      )
    ).thenReturn(5L);

    Long count = service.countDistributionEmails("test");

    assertEquals(Long.valueOf(5), count);
  }

  @Test
  public void testCountDistributionEmails_whenQueryIsNull_thenUsesEmptySearch() {
    when(
      sqlSessionTemplate.selectOne(
        eq("AppMessage.count_distribution_emails"),
        any(Map.class)
      )
    ).thenReturn(0L);

    Long count = service.countDistributionEmails(null);

    assertEquals(Long.valueOf(0), count);
    verify(sqlSessionTemplate).selectOne(
      eq("AppMessage.count_distribution_emails"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return "".equals(m.get("search"));
      })
    );
  }

  @Test
  public void testInsertEmail_whenCalled_thenInsertsEmailAddress() {
    DistributionEmailDAO email = new DistributionEmailDAO();
    email.setEmailAddress("user@example.com");

    service.insertEmail(email);

    verify(sqlSessionTemplate).insert(
      eq("AppMessage.insert_distribution_email"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return "user@example.com".equals(m.get("email"));
      })
    );
  }

  @Test
  public void testHasDistributionEmail_whenExists_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne(
        eq("AppMessage.count_distribution_email"),
        any(Map.class)
      )
    ).thenReturn(1);

    int result = service.hasDistributionEmail("USER@EXAMPLE.COM");

    assertEquals(1, result);
    verify(sqlSessionTemplate).selectOne(
      eq("AppMessage.count_distribution_email"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return "user@example.com".equals(m.get("email"));
      })
    );
  }

  @Test
  public void testDeleteDistributionEmail_whenCalled_thenDeletesById() {
    service.deleteDistributionEmail(5);

    verify(sqlSessionTemplate).delete(
      eq("AppMessage.delete_distribution_email"),
      argThat(map -> {
        Map<String, Object> m = (Map<String, Object>) map;
        return Integer.valueOf(5).equals(m.get("id"));
      })
    );
  }

  @Test
  public void testGetDistributionEmail_whenExists_thenReturnsDao() {
    DistributionEmailDAO dao = new DistributionEmailDAO();
    dao.setEmailAddress("test@test.com");
    when(
      sqlSessionTemplate.selectOne(
        eq("AppMessage.get_distribution_email"),
        any(Map.class)
      )
    ).thenReturn(dao);

    DistributionEmailDAO result = service.getDistributionEmail("TEST@TEST.COM");

    assertNotNull(result);
    assertEquals("test@test.com", result.getEmailAddress());
  }

  @Test
  public void testGetDistributionEmailById_whenExists_thenReturnsDao() {
    DistributionEmailDAO dao = new DistributionEmailDAO();
    dao.setId(9);
    when(
      sqlSessionTemplate.selectOne(
        eq("AppMessage.get_distribution_email_by_id"),
        any(Map.class)
      )
    ).thenReturn(dao);

    DistributionEmailDAO result = service.getDistributionEmailById(9);

    assertNotNull(result);
    assertEquals(Integer.valueOf(9), result.getId());
  }
}
