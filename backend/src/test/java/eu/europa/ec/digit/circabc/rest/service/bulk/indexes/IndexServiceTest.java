package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IndexServiceTest {

  private IndexServiceImpl indexService;
  private ServiceRegistry serviceRegistry;

  @Before
  public void setUp() throws Exception {
    indexService = new IndexServiceImpl();
    serviceRegistry = Mockito.mock(ServiceRegistry.class);
    indexService.setServiceRegistry(serviceRegistry);

    // Clear static VALIDATORS list to ensure test isolation
    Field validatorsField = IndexServiceImpl.class.getDeclaredField(
      "VALIDATORS"
    );
    validatorsField.setAccessible(true);
    ((List<?>) validatorsField.get(null)).clear();
  }

  @Test
  public void testGetIndexHeaders_returnsNonNull() {
    IndexHeaders headers = indexService.getIndexHeaders();
    assertNotNull(headers);
    assertNotNull(headers.getHeaders());
    assertFalse(headers.getHeaders().isEmpty());
  }

  @Test
  public void testGetIndexHeaders_containsExpectedColumns() {
    IndexHeaders headers = indexService.getIndexHeaders();
    assertNotNull(headers.getHeader(IndexHeaderColumn.NAME));
    assertNotNull(headers.getHeader(IndexHeaderColumn.TITLE));
    assertNotNull(headers.getHeader(IndexHeaderColumn.DOC_LANG));
    assertNotNull(headers.getHeader(IndexHeaderColumn.OVERWRITE));
  }

  @Test
  public void testGetIndexRecords_whenValidFile_thenParsesRecords()
    throws IOException {
    File tempFile = createTempIndexFile(
      "NAME\tTITLE\tDESCRIPTION\n" + "doc1.pdf\tTitle1\tDesc1\n"
    );

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertEquals(1, records.size());
    IndexRecord record = records.get(0);
    assertTrue(record.getName().endsWith("doc1.pdf"));
    assertEquals("Title1", record.getTitle());
    assertEquals("Desc1", record.getDescription());

    tempFile.delete();
  }

  @Test
  public void testGetIndexRecords_whenEmptyRow_thenAddsWarningMessage()
    throws IOException {
    File tempFile = createTempIndexFile(
      "NAME\tTITLE\n" + "\t\n" + "doc1.pdf\tTitle1\n"
    );

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertEquals(1, records.size());
    assertFalse(messages.isEmpty());

    tempFile.delete();
  }

  @Test
  public void testGetIndexRecords_whenMultipleRows_thenParsesAll()
    throws IOException {
    File tempFile = createTempIndexFile(
      "NAME\tTITLE\n" + "doc1.pdf\tTitle1\n" + "doc2.pdf\tTitle2\n"
    );

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertEquals(2, records.size());

    tempFile.delete();
  }

  @Test
  public void testGetIndexRecords_whenEmptyFile_thenNoRecords()
    throws IOException {
    File tempFile = createTempIndexFile("");

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertTrue(records.isEmpty());
    assertTrue(messages.isEmpty());

    tempFile.delete();
  }

  @Test
  public void testGenerateIndexRecords_whenEmptyList_thenWritesHeaderOnly()
    throws IOException {
    File tempFile = File.createTempFile("index_gen_", ".txt");
    List<IndexRecord> records = new ArrayList<>();

    indexService.generateIndexRecords(tempFile, records);

    assertTrue(tempFile.length() > 0);

    tempFile.delete();
  }

  @Test
  public void testGenerateIndexRecords_whenRecordsProvided_thenWritesData()
    throws IOException {
    File tempFile = File.createTempFile("index_gen_", ".txt");
    List<IndexRecord> records = new ArrayList<>();

    IndexRecordImpl record = new IndexRecordImpl(1);
    record.setName("/doc1.pdf");
    record.setTitle("Title1");
    records.add(record);

    indexService.generateIndexRecords(tempFile, records);

    assertTrue(tempFile.length() > 0);

    // Read back and verify
    List<IndexRecord> readRecords = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();
    indexService.getIndexRecords(tempFile, readRecords, messages);

    assertEquals(1, readRecords.size());
    assertTrue(readRecords.get(0).getName().contains("doc1.pdf"));
    assertEquals("Title1", readRecords.get(0).getTitle());

    tempFile.delete();
  }

  @Test(expected = IOException.class)
  public void testGetIndexRecords_whenFileNotFound_thenThrowsIOException()
    throws IOException {
    File nonExistent = new File("/nonexistent/path/index.txt");
    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(nonExistent, records, messages);
  }

  @Test
  public void testGetIndexRecords_whenNameHasBackslash_thenNormalizesPath()
    throws IOException {
    File tempFile = createTempIndexFile(
      "NAME\tTITLE\n" + "folder\\doc.pdf\tTitle\n"
    );

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertEquals(1, records.size());
    // On Unix, backslash should be replaced with forward slash
    String name = records.get(0).getName();
    assertTrue(name.startsWith(File.separator));
    assertFalse(name.contains("\\"));

    tempFile.delete();
  }

  private File createTempIndexFile(String content) throws IOException {
    File tempFile = File.createTempFile("index_test_", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write(content);
    }
    return tempFile;
  }
}
