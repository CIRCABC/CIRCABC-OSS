package eu.europa.ec.digit.circabc.rest.service.bulk;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecordImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexService;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.bulk.upload.UploadedEntry;
import eu.europa.ec.digit.circabc.rest.service.bulk.upload.UploadedEntryImpl;
import eu.europa.ec.digit.circabc.rest.service.bulk.validation.ErrorType;
import eu.europa.ec.digit.circabc.rest.service.compress.CompressedEntry;
import eu.europa.ec.digit.circabc.rest.service.compress.ZipService;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import io.swagger.util.ApiToolBox;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;

public class BulkServiceTest {

  private BulkServiceImpl bulkService;
  private NodeService nodeService;
  private ZipService zipService;
  private IndexService indexService;
  private ApiToolBox apiToolBox;
  private KeywordsService keywordsService;
  private FileFolderService fileFolderService;
  private ContentService contentService;
  private ContentFilterLanguagesService contentFilterLanguagesService;
  private MultilingualContentService multilingualContentService;
  private DictionaryService dictionaryService;
  private PermissionService permissionService;

  private NodeRef containerNodeRef;
  private NodeRef libraryNodeRef;

  @Before
  public void setUp() {
    bulkService = new BulkServiceImpl();

    nodeService = mock(NodeService.class);
    zipService = mock(ZipService.class);
    indexService = mock(IndexService.class);
    apiToolBox = mock(ApiToolBox.class);
    keywordsService = mock(KeywordsService.class);
    fileFolderService = mock(FileFolderService.class);
    contentService = mock(ContentService.class);
    contentFilterLanguagesService = mock(ContentFilterLanguagesService.class);
    multilingualContentService = mock(MultilingualContentService.class);
    dictionaryService = mock(DictionaryService.class);
    permissionService = mock(PermissionService.class);

    bulkService.setNodeService(nodeService);
    bulkService.setZipService(zipService);
    bulkService.setIndexService(indexService);
    bulkService.setKeywordsService(keywordsService);
    bulkService.setFileFolderService(fileFolderService);
    bulkService.setContentService(contentService);
    bulkService.setContentFilterLanguagesService(contentFilterLanguagesService);
    bulkService.setMultilingualContentService(multilingualContentService);
    bulkService.setDictionaryService(dictionaryService);
    bulkService.setPermissionService(permissionService);

    // Inject apiToolBox via reflection (no setter available)
    try {
      java.lang.reflect.Field field = BulkServiceImpl.class.getDeclaredField(
        "apiToolBox"
      );
      field.setAccessible(true);
      field.set(bulkService, apiToolBox);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    containerNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    libraryNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-id"
    );
  }

  @Test
  public void testGetCompressedEntries_delegatesToZipService() {
    File compressedFile = new File("test.zip");
    List<ValidationMessage> messages = new ArrayList<>();
    List<CompressedEntry> expected = new ArrayList<>();
    when(zipService.getCompressedEntries(compressedFile, messages)).thenReturn(
      expected
    );

    List<CompressedEntry> result = bulkService.getCompressedEntries(
      containerNodeRef,
      compressedFile,
      messages
    );

    assertSame(expected, result);
    verify(zipService).getCompressedEntries(compressedFile, messages);
  }

  @Test
  public void testGetIndexRecords_whenIndexFileExists_returnsRecords()
    throws IOException {
    File compressedFile = new File("test.zip");
    List<ValidationMessage> messages = new ArrayList<>();

    when(
      zipService.extract(
        eq(compressedFile),
        eq(IndexService.INDEX_FILE),
        any(File.class),
        eq(messages)
      )
    ).thenReturn(true);

    List<IndexRecord> result = bulkService.getIndexRecords(
      compressedFile,
      messages
    );

    assertNotNull(result);
    verify(zipService).extract(
      eq(compressedFile),
      eq(IndexService.INDEX_FILE),
      any(File.class),
      eq(messages)
    );
    verify(indexService).getIndexRecords(
      any(File.class),
      eq(result),
      eq(messages)
    );
  }

  @Test
  public void testGetIndexRecords_whenNoIndexFile_addsWarning()
    throws IOException {
    File compressedFile = new File("test.zip");
    List<ValidationMessage> messages = new ArrayList<>();

    when(
      zipService.extract(
        eq(compressedFile),
        eq(IndexService.INDEX_FILE),
        any(File.class),
        eq(messages)
      )
    ).thenReturn(false);

    List<IndexRecord> result = bulkService.getIndexRecords(
      compressedFile,
      messages
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(1, messages.size());
    assertEquals(ErrorType.Warning, messages.get(0).getErrorType());
    verifyNoInteractions(indexService);
  }

  @Test
  public void testValidateEntries_whenAllEntriesMatch_noMessages() {
    List<IndexRecord> indexRecords = new ArrayList<>();
    IndexRecordImpl record = new IndexRecordImpl(1);
    record.setName(
      File.separatorChar + "folder" + File.separatorChar + "doc.pdf"
    );
    indexRecords.add(record);

    List<UploadedEntry> uploadedEntries = new ArrayList<>();
    UploadedEntryImpl entry = new UploadedEntryImpl(
      "doc.pdf",
      File.separatorChar + "folder" + File.separatorChar + "doc.pdf"
    );
    uploadedEntries.add(entry);

    List<ValidationMessage> messages = new ArrayList<>();

    bulkService.validateEntries(indexRecords, uploadedEntries, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidateEntries_whenEntryMissing_addsFatalMessage() {
    List<IndexRecord> indexRecords = new ArrayList<>();
    IndexRecordImpl record = new IndexRecordImpl(1);
    record.setName(
      File.separatorChar + "folder" + File.separatorChar + "missing.pdf"
    );
    indexRecords.add(record);

    List<UploadedEntry> uploadedEntries = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    bulkService.validateEntries(indexRecords, uploadedEntries, messages);

    assertEquals(1, messages.size());
    assertEquals(ErrorType.Fatal, messages.get(0).getErrorType());
    assertEquals(1, messages.get(0).getRowNumber());
  }

  @Test
  public void testValidateEntries_whenEmptyTranslationMissing_noFatalMessage() {
    List<IndexRecord> indexRecords = new ArrayList<>();
    IndexRecordImpl record = new IndexRecordImpl(2);
    record.setName(
      File.separatorChar + "folder" + File.separatorChar + "empty.pdf"
    );
    record.setNoContent("Y");
    indexRecords.add(record);

    List<UploadedEntry> uploadedEntries = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    bulkService.validateEntries(indexRecords, uploadedEntries, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidateEntries_withEmptyLists_noMessages() {
    List<IndexRecord> indexRecords = new ArrayList<>();
    List<UploadedEntry> uploadedEntries = new ArrayList<>();
    List<ValidationMessage> messages = new ArrayList<>();

    bulkService.validateEntries(indexRecords, uploadedEntries, messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testUpload_withoutIndexRecords_delegatesToOverload() {
    File compressedFile = new File("test.zip");
    List<ValidationMessage> messages = new ArrayList<>();

    when(apiToolBox.getCurrentLibraryRoot(containerNodeRef)).thenReturn(
      libraryNodeRef
    );
    when(
      zipService.extract(
        eq(libraryNodeRef),
        eq(containerNodeRef),
        eq(compressedFile),
        anyList(),
        eq(Collections.<IndexRecord>emptyList()),
        eq(messages)
      )
    ).thenReturn(Collections.<String, NodeRef>emptyMap());

    List<UploadedEntry> result = bulkService.upload(
      containerNodeRef,
      compressedFile,
      messages
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(apiToolBox).getCurrentLibraryRoot(containerNodeRef);
  }

  @Test
  public void testUpload_withIndexRecords_extractsAndProcesses() {
    File compressedFile = new File("test.zip");
    List<ValidationMessage> messages = new ArrayList<>();
    List<IndexRecord> indexRecords = new ArrayList<>();

    when(apiToolBox.getCurrentLibraryRoot(containerNodeRef)).thenReturn(
      libraryNodeRef
    );

    Map<String, NodeRef> extractedFiles = Collections.emptyMap();
    when(
      zipService.extract(
        eq(libraryNodeRef),
        eq(containerNodeRef),
        eq(compressedFile),
        anyList(),
        eq(indexRecords),
        eq(messages)
      )
    ).thenReturn(extractedFiles);

    List<UploadedEntry> result = bulkService.upload(
      containerNodeRef,
      compressedFile,
      indexRecords,
      messages
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(zipService).extract(
      eq(libraryNodeRef),
      eq(containerNodeRef),
      eq(compressedFile),
      anyList(),
      eq(indexRecords),
      eq(messages)
    );
  }

  @Test
  public void testGetMetaData_withEmptyList_returnsEmpty() {
    List<NodeRef> nodeRefs = new ArrayList<>();

    List<IndexRecord> result = bulkService.getMetaData(nodeRefs);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetMetaData_whenNodeDoesNotExist_returnsEmpty() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "non-existent"
    );
    when(nodeService.exists(nodeRef)).thenReturn(false);

    List<IndexRecord> result = bulkService.getMetaData(
      Collections.singletonList(nodeRef)
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetMetaData_whenNoReadPermission_returnsEmpty() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "no-access"
    );
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.DENIED);

    List<IndexRecord> result = bulkService.getMetaData(
      Collections.singletonList(nodeRef)
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
