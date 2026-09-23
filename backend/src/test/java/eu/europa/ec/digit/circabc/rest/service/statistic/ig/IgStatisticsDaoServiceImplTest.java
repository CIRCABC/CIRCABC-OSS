package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class IgStatisticsDaoServiceImplTest {

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
    IgStatisticsParameter expected = new IgStatisticsParameter();
    when(
      sqlSessionTemplate.selectOne("GroupStatistics.selectIGStatisticsById", 1L)
    ).thenReturn(expected);

    IgStatisticsParameter result = service.getIgStatisticsById(1L);

    assertSame(expected, result);
    verify(sqlSessionTemplate).selectOne(
      "GroupStatistics.selectIGStatisticsById",
      1L
    );
  }

  @Test
  public void testGetIgStatisticsById_whenNotFound_thenReturnsNull() {
    when(
      sqlSessionTemplate.selectOne(
        "GroupStatistics.selectIGStatisticsById",
        99L
      )
    ).thenReturn(null);

    IgStatisticsParameter result = service.getIgStatisticsById(99L);

    assertNull(result);
  }

  @Test
  public void testInsertIGStatistics_callsInsert() {
    IgStatisticsParameter stats = new IgStatisticsParameter();

    service.insertIGStatistics(stats);

    verify(sqlSessionTemplate).insert(
      "GroupStatistics.insertIGStatistics",
      stats
    );
  }

  @Test
  public void testUpdateIGStatistics_callsUpdate() {
    IgStatisticsParameter stats = new IgStatisticsParameter();

    service.updateIGStatistics(stats);

    verify(sqlSessionTemplate).update(
      "GroupStatistics.updateIGStatistics",
      stats
    );
  }
}
