package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;

public class IgStatisticsParameterTest {

  private IgStatisticsParameter param;

  @Before
  public void setUp() {
    param = new IgStatisticsParameter();
  }

  @Test
  public void testDefaultConstructor_allFieldsNull() {
    assertNull(param.getIgId());
    assertNull(param.getRequestDate());
    assertNull(param.getCreationDate());
    assertNull(param.getNbUsers());
    assertNull(param.getLibraryFolderCount());
    assertNull(param.getLibraryDocumentCount());
    assertNull(param.getLibrarySize());
    assertNull(param.getInformationFolderCount());
    assertNull(param.getInformationDocumentCount());
    assertNull(param.getInformationSize());
    assertNull(param.getVersionCount());
    assertNull(param.getVersionSize());
    assertNull(param.getTotalSize());
    assertNull(param.getEventCount());
    assertNull(param.getMeetingCount());
    assertNull(param.getForumCount());
    assertNull(param.getTopicCount());
    assertNull(param.getPostCount());
    assertNull(param.getMaxLevel());
    assertNull(param.getCustomizationAndHiddenContentCount());
    assertNull(param.getCustomizationAndHiddenContentSize());
  }

  @Test
  public void testSetAndGetIgId() {
    param.setIgId(42L);
    assertEquals(Long.valueOf(42L), param.getIgId());
  }

  @Test
  public void testSetAndGetRequestDate() {
    Date date = new Date();
    param.setRequestDate(date);
    assertEquals(date, param.getRequestDate());
  }

  @Test
  public void testSetAndGetCreationDate() {
    Date date = new Date();
    param.setCreationDate(date);
    assertEquals(date, param.getCreationDate());
  }

  @Test
  public void testSetAndGetNbUsers() {
    param.setNbUsers(10);
    assertEquals(Integer.valueOf(10), param.getNbUsers());
  }

  @Test
  public void testSetAndGetLibraryFields() {
    param.setLibraryFolderCount(5);
    param.setLibraryDocumentCount(20);
    param.setLibrarySize(1024L);
    assertEquals(Integer.valueOf(5), param.getLibraryFolderCount());
    assertEquals(Integer.valueOf(20), param.getLibraryDocumentCount());
    assertEquals(Long.valueOf(1024L), param.getLibrarySize());
  }

  @Test
  public void testSetAndGetInformationFields() {
    param.setInformationFolderCount(3);
    param.setInformationDocumentCount(15);
    param.setInformationSize(2048L);
    assertEquals(Integer.valueOf(3), param.getInformationFolderCount());
    assertEquals(Integer.valueOf(15), param.getInformationDocumentCount());
    assertEquals(Long.valueOf(2048L), param.getInformationSize());
  }

  @Test
  public void testSetAndGetVersionFields() {
    param.setVersionCount(7);
    param.setVersionSize(512L);
    assertEquals(Integer.valueOf(7), param.getVersionCount());
    assertEquals(Long.valueOf(512L), param.getVersionSize());
  }

  @Test
  public void testSetAndGetTotalSize() {
    param.setTotalSize(99999L);
    assertEquals(Long.valueOf(99999L), param.getTotalSize());
  }

  @Test
  public void testSetAndGetEventAndMeetingCounts() {
    param.setEventCount(4);
    param.setMeetingCount(2);
    assertEquals(Integer.valueOf(4), param.getEventCount());
    assertEquals(Integer.valueOf(2), param.getMeetingCount());
  }

  @Test
  public void testSetAndGetForumTopicPostCounts() {
    param.setForumCount(3);
    param.setTopicCount(12);
    param.setPostCount(50);
    assertEquals(Integer.valueOf(3), param.getForumCount());
    assertEquals(Integer.valueOf(12), param.getTopicCount());
    assertEquals(Integer.valueOf(50), param.getPostCount());
  }

  @Test
  public void testSetAndGetMaxLevel() {
    param.setMaxLevel(5);
    assertEquals(Integer.valueOf(5), param.getMaxLevel());
  }

  @Test
  public void testSetAndGetCustomizationAndHiddenContentFields() {
    param.setCustomizationAndHiddenContentCount(8);
    param.setCustomizationAndHiddenContentSize(4096L);
    assertEquals(
      Integer.valueOf(8),
      param.getCustomizationAndHiddenContentCount()
    );
    assertEquals(
      Long.valueOf(4096L),
      param.getCustomizationAndHiddenContentSize()
    );
  }

  @Test
  public void testSetFieldsToNull() {
    param.setIgId(1L);
    param.setIgId(null);
    assertNull(param.getIgId());

    param.setNbUsers(10);
    param.setNbUsers(null);
    assertNull(param.getNbUsers());

    param.setTotalSize(500L);
    param.setTotalSize(null);
    assertNull(param.getTotalSize());
  }
}
