package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class IgStatisticsDaoServiceTest {

  private IgStatisticsDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() {
    service = new IgStatisticsDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(sqlSessionTemplate);
  }

  @Test
  public void testGetIgStatisticsById_whenFound_thenReturnsParameter() {
    Long igId = 42L;
    IgStatisticsParameter expected = new IgStatisticsParameter();
    expected.setIgId(igId);
    when(
      sqlSessionTemplate.selectOne(
        "GroupStatistics.selectIGStatisticsById",
        igId
      )
    ).thenReturn(expected);

    IgStatisticsParameter result = service.getIgStatisticsById(igId);

    assertSame(expected, result);
    verify(sqlSessionTemplate).selectOne(
      "GroupStatistics.selectIGStatisticsById",
      igId
    );
  }

  @Test
  public void testGetIgStatisticsById_whenNotFound_thenReturnsNull() {
    when(
      sqlSessionTemplate.selectOne(
        "GroupStatistics.selectIGStatisticsById",
        999L
      )
    ).thenReturn(null);

    IgStatisticsParameter result = service.getIgStatisticsById(999L);

    assertNull(result);
  }

  @Test
  public void testInsertIGStatistics_delegatesToSqlSession() {
    IgStatisticsParameter stats = new IgStatisticsParameter();
    stats.setIgId(1L);

    service.insertIGStatistics(stats);

    verify(sqlSessionTemplate).insert(
      "GroupStatistics.insertIGStatistics",
      stats
    );
  }

  @Test
  public void testUpdateIGStatistics_delegatesToSqlSession() {
    IgStatisticsParameter stats = new IgStatisticsParameter();
    stats.setIgId(2L);

    service.updateIGStatistics(stats);

    verify(sqlSessionTemplate).update(
      "GroupStatistics.updateIGStatistics",
      stats
    );
  }

  @Test
  public void testGetSetSqlSessionTemplate() {
    SqlSessionTemplate another = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(another);
    assertSame(another, service.getSqlSessionTemplate());
  }
}
