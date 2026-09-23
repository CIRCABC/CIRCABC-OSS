package eu.europa.ec.digit.circabc.rest.service.auto.upload;

import static org.junit.Assert.*;

import org.junit.Test;

public class AutoUploadJobResultTest {

  @Test
  public void testGetResult_jobOk_returns1() {
    assertEquals(Integer.valueOf(1), AutoUploadJobResult.JOB_OK.getResult());
  }

  @Test
  public void testGetResult_jobNothingToDo_returns0() {
    assertEquals(
      Integer.valueOf(0),
      AutoUploadJobResult.JOB_NOTHING_TO_DO.getResult()
    );
  }

  @Test
  public void testGetResult_jobError_returnsNegative1() {
    assertEquals(
      Integer.valueOf(-1),
      AutoUploadJobResult.JOB_ERROR.getResult()
    );
  }

  @Test
  public void testGetResult_jobRemoteFtpProblem_returnsNegative2() {
    assertEquals(
      Integer.valueOf(-2),
      AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM.getResult()
    );
  }

  @Test
  public void testValueOf_validName_returnsEnum() {
    assertEquals(
      AutoUploadJobResult.JOB_OK,
      AutoUploadJobResult.valueOf("JOB_OK")
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_invalidName_throwsException() {
    AutoUploadJobResult.valueOf("INVALID");
  }

  @Test
  public void testValues_returnsFourConstants() {
    assertEquals(4, AutoUploadJobResult.values().length);
  }
}
