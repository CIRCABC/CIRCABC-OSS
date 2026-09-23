package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Test;

public class IndexServiceImplTest {

  private IndexServiceImpl indexService;
  private ServiceRegistry serviceRegistry;

  @Before
  public void setUp() throws Exception {
    indexService = new IndexServiceImpl();
    serviceRegistry = mock(ServiceRegistry.class);
    indexService.setServiceRegistry(serviceRegistry);

    // Clear the static VALIDATORS list to avoid cross-test pollution
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
  public void testGetIndexHeaders_containsNameHeader() {
    IndexHeaders headers = indexService.getIndexHeaders();
    IndexHeader nameHeader = headers.getHeader(IndexHeaderColumn.NAME);
    assertNotNull(nameHeader);
    assertEquals(IndexHeaderColumn.NAME, nameHeader.getHeaderName());
  }

  @Test
  public void testGetIndexRecords_parsesValidFile() throws Exception {
    String content =
      "NAME\tTITLE\tDESCRIPTION\n" + "\\doc.txt\tMyTitle\tMyDesc\n";
    File tempFile = createTempIndexFile(content);

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertEquals(1, records.size());
    IndexRecord record = records.get(0);
    assertNotNull(record.getEntry(IndexHeaderColumn.NAME));
    assertEquals(
      "MyTitle",
      record.getEntry(IndexHeaderColumn.TITLE).getValue()
    );
    assertEquals(
      "MyDesc",
      record.getEntry(IndexHeaderColumn.DESCRIPTION).getValue()
    );

    tempFile.delete();
  }

  @Test
  public void testGetIndexRecords_emptyRowProducesWarning() throws Exception {
    String content = "NAME\tTITLE\n" + "\t\n" + "\\file.txt\tTitle\n";
    File tempFile = createTempIndexFile(content);

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertEquals(1, records.size());
    assertEquals(1, messages.size());

    tempFile.delete();
  }

  @Test
  public void testGetIndexRecords_emptyFile() throws Exception {
    File tempFile = createTempIndexFile("");

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertTrue(records.isEmpty());
    assertTrue(messages.isEmpty());

    tempFile.delete();
  }

  @Test
  public void testGetIndexRecords_namePathNormalized() throws Exception {
    String content = "NAME\n" + "subdir\\file.txt\n";
    File tempFile = createTempIndexFile(content);

    List<IndexRecord> records = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    indexService.getIndexRecords(tempFile, records, messages);

    assertEquals(1, records.size());
    String name = records.get(0).getEntry(IndexHeaderColumn.NAME).getValue();
    // Should start with separator and have normalized separators
    assertTrue(name.startsWith(File.separator));

    tempFile.delete();
  }

  @Test
  public void testGenerateIndexRecords_writesHeaderAndData() throws Exception {
    File tempFile = File.createTempFile("index_out", ".txt");

    IndexRecordImpl record = new IndexRecordImpl(1);
    record.addIndexEntry(
      new IndexEntryImpl(IndexHeaderColumn.NAME, "/test.txt")
    );
    record.addIndexEntry(
      new IndexEntryImpl(IndexHeaderColumn.TITLE, "Test Title")
    );

    List<IndexRecord> records = new ArrayList<>();
    records.add(record);

    indexService.generateIndexRecords(tempFile, records);

    String output = new String(
      new FileInputStream(tempFile).readAllBytes(),
      StandardCharsets.UTF_8
    );
    // Header row should contain NAME
    assertTrue(output.contains(IndexHeaderColumn.NAME));
    // Data row should contain the values
    assertTrue(output.contains("Test Title"));
    assertTrue(output.contains("/test.txt"));

    tempFile.delete();
  }

  @Test
  public void testGenerateIndexRecords_emptyRecords() throws Exception {
    File tempFile = File.createTempFile("index_out", ".txt");

    indexService.generateIndexRecords(tempFile, new ArrayList<>());

    String output = new String(
      new FileInputStream(tempFile).readAllBytes(),
      StandardCharsets.UTF_8
    );
    // Should still have the header row
    assertTrue(output.contains(IndexHeaderColumn.NAME));
    // Only one line (header) plus line ending
    String[] lines = output.split("\r\n");
    assertEquals(1, lines.length);

    tempFile.delete();
  }

  @Test(expected = IOException.class)
  public void testGetIndexRecords_nonExistentFile() throws Exception {
    File nonExistent = new File("/tmp/does_not_exist_index_12345.txt");
    indexService.getIndexRecords(
      nonExistent,
      new ArrayList<>(),
      new ArrayList<>()
    );
  }

  private File createTempIndexFile(String content) throws IOException {
    File tempFile = File.createTempFile("index_test", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write(content);
    }
    return tempFile;
  }
}
