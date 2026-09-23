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
import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class BulkServiceImplTest {

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
  public void setUp() throws Exception {
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

    Field field = BulkServiceImpl.class.getDeclaredField("apiToolBox");
    field.setAccessible(true);
    field.set(bulkService, apiToolBox);

    containerNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    libraryNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-id"
    );
  }

  // --- getCompressedEntries ---

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

  // --- getIndexRecords ---

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

    assertTrue(result.isEmpty());
    assertEquals(1, messages.size());
    assertEquals(ErrorType.Warning, messages.get(0).getErrorType());
    verifyNoInteractions(indexService);
  }

  // --- validateEntries ---

  @Test
  public void testValidateEntries_whenAllEntriesMatch_noMessages() {
    List<IndexRecord> indexRecords = new ArrayList<>();
    IndexRecordImpl record = new IndexRecordImpl(1);
    record.setName(
      File.separatorChar + "folder" + File.separatorChar + "doc.pdf"
    );
    indexRecords.add(record);

    List<UploadedEntry> uploadedEntries = new ArrayList<>();
    uploadedEntries.add(
      new UploadedEntryImpl(
        "doc.pdf",
        File.separatorChar + "folder" + File.separatorChar + "doc.pdf"
      )
    );

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

    List<ValidationMessage> messages = new ArrayList<>();
    bulkService.validateEntries(indexRecords, new ArrayList<>(), messages);

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

    List<ValidationMessage> messages = new ArrayList<>();
    bulkService.validateEntries(indexRecords, new ArrayList<>(), messages);

    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidateEntries_withEmptyLists_noMessages() {
    List<ValidationMessage> messages = new ArrayList<>();
    bulkService.validateEntries(new ArrayList<>(), new ArrayList<>(), messages);
    assertTrue(messages.isEmpty());
  }

  @Test
  public void testValidateEntries_pathWithoutLeadingSeparator_normalizesAndMatches() {
    List<IndexRecord> indexRecords = new ArrayList<>();
    IndexRecordImpl record = new IndexRecordImpl(1);
    // No leading separator — should be normalized
    record.setName("folder" + File.separatorChar + "doc.pdf");
    indexRecords.add(record);

    List<UploadedEntry> uploadedEntries = new ArrayList<>();
    uploadedEntries.add(
      new UploadedEntryImpl(
        "doc.pdf",
        File.separatorChar + "folder" + File.separatorChar + "doc.pdf"
      )
    );

    List<ValidationMessage> messages = new ArrayList<>();
    bulkService.validateEntries(indexRecords, uploadedEntries, messages);

    assertTrue(messages.isEmpty());
  }

  // --- upload ---

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
  public void testUpload_withIndexRecords_processesNonMLDocs() {
    File compressedFile = new File("test.zip");
    List<ValidationMessage> messages = new ArrayList<>();

    IndexRecordImpl record = new IndexRecordImpl(1);
    String filePath =
      File.separatorChar + "folder" + File.separatorChar + "doc.pdf";
    record.setName(filePath);
    // oriLang null means non-multilingual doc
    List<IndexRecord> indexRecords = new ArrayList<>();
    indexRecords.add(record);

    NodeRef fileNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );

    when(apiToolBox.getCurrentLibraryRoot(containerNodeRef)).thenReturn(
      libraryNodeRef
    );

    Map<String, NodeRef> extractedFiles = new LinkedHashMap<>();
    extractedFiles.put(filePath, fileNodeRef);

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

    when(
      nodeService.getProperty(fileNodeRef, ContentModel.PROP_NAME)
    ).thenReturn("doc.pdf");
    when(nodeService.getProperties(fileNodeRef)).thenReturn(new HashMap<>());

    List<UploadedEntry> result = bulkService.upload(
      containerNodeRef,
      compressedFile,
      indexRecords,
      messages
    );

    assertEquals(1, result.size());
    assertEquals("doc.pdf", result.get(0).getFileName());
    assertEquals(filePath, result.get(0).getFilePath());
  }

  // --- getMetaData ---

  @Test
  public void testGetMetaData_withEmptyList_returnsEmpty() {
    List<IndexRecord> result = bulkService.getMetaData(new ArrayList<>());
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

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetMetaData_withContentNode_returnsPopulatedRecord() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-id"
    );
    TypeDefinition typeDef = mock(TypeDefinition.class);

    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(dictionaryService.getType(ContentModel.TYPE_CONTENT)).thenReturn(
      typeDef
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_CONTENT
      )
    ).thenReturn(true);
    when(multilingualContentService.isTranslation(nodeRef)).thenReturn(false);
    when(zipService.getRelativeLibraryPath(nodeRef)).thenReturn(
      "folder/doc.pdf"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      "My Title"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("My Description");
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE)).thenReturn(
      Locale.ENGLISH
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR)).thenReturn(
      "Author"
    );
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_STATUS)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_ISSUE_DATE)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_REFERENCE)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_EXPIRATION_DATE)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn(null);
    when(keywordsService.getKeywordsForNode(nodeRef)).thenReturn(
      Collections.<Keyword>emptyList()
    );

    List<IndexRecord> result = bulkService.getMetaData(
      Collections.singletonList(nodeRef)
    );

    assertEquals(1, result.size());
    IndexRecord record = result.get(0);
    assertEquals("folder/doc.pdf", record.getName());
    assertEquals("My Title", record.getTitle());
    assertEquals("My Description", record.getDescription());
    assertEquals("en", record.getDocLang());
    assertEquals("Author", record.getAuthor());
  }

  @Test
  public void testGetMetaData_withSensitiveSecurityRanking_excludesNode() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "sensitive-id"
    );
    TypeDefinition typeDef = mock(TypeDefinition.class);

    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(dictionaryService.getType(ContentModel.TYPE_CONTENT)).thenReturn(
      typeDef
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_CONTENT
      )
    ).thenReturn(true);
    when(multilingualContentService.isTranslation(nodeRef)).thenReturn(false);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn("SENSITIVE");

    List<IndexRecord> result = bulkService.getMetaData(
      Collections.singletonList(nodeRef)
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetMetaData_withPublicSecurityRanking_includesNode() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "public-id"
    );
    TypeDefinition typeDef = mock(TypeDefinition.class);

    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(dictionaryService.getType(ContentModel.TYPE_CONTENT)).thenReturn(
      typeDef
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_CONTENT
      )
    ).thenReturn(true);
    when(multilingualContentService.isTranslation(nodeRef)).thenReturn(false);
    when(zipService.getRelativeLibraryPath(nodeRef)).thenReturn(
      "folder/public.pdf"
    );
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn("PUBLIC");
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION)
    ).thenReturn(null);
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE)).thenReturn(
      null
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR)).thenReturn(
      null
    );
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_STATUS)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_ISSUE_DATE)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_REFERENCE)
    ).thenReturn(null);
    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_EXPIRATION_DATE)
    ).thenReturn(null);
    when(keywordsService.getKeywordsForNode(nodeRef)).thenReturn(
      Collections.<Keyword>emptyList()
    );

    List<IndexRecord> result = bulkService.getMetaData(
      Collections.singletonList(nodeRef)
    );

    assertEquals(1, result.size());
    assertEquals("folder/public.pdf", result.get(0).getName());
    assertEquals("PUBLIC", result.get(0).getSecurityRanking());
  }

  // --- upload with index records and extracted files ---

  @Test
  public void testUpload_whenZipExtractsFiles_thenProcessesThem()
    throws IOException {
    File tempFile = File.createTempFile("test", ".zip");
    tempFile.deleteOnExit();

    when(apiToolBox.getCurrentLibraryRoot(containerNodeRef)).thenReturn(
      libraryNodeRef
    );

    NodeRef extractedNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "extracted-1"
    );
    Map<String, NodeRef> extractedFiles = new HashMap<>();
    extractedFiles.put("/doc.txt", extractedNode);

    when(
      zipService.extract(
        eq(libraryNodeRef),
        eq(containerNodeRef),
        eq(tempFile),
        any(),
        any(),
        any()
      )
    ).thenReturn(extractedFiles);

    IndexRecord indexRecord = mock(IndexRecord.class);
    when(indexRecord.getName()).thenReturn("/doc.txt");
    when(indexRecord.getOriLang()).thenReturn(null);
    when(indexRecord.getDocLang()).thenReturn("en");
    when(indexRecord.getNoContent()).thenReturn("N");

    List<IndexRecord> indexRecords = Collections.singletonList(indexRecord);
    List<ValidationMessage> messages = new ArrayList<>();

    List<UploadedEntry> result = bulkService.upload(
      containerNodeRef,
      tempFile,
      indexRecords,
      messages
    );

    assertNotNull(result);
  }

  // --- getCompressedEntries ---

  @Test
  public void testGetCompressedEntries_whenFileHasEntries_thenReturnsList()
    throws IOException {
    File tempFile = File.createTempFile("test", ".zip");
    tempFile.deleteOnExit();

    CompressedEntry entry = mock(CompressedEntry.class);
    when(entry.getFileName()).thenReturn("file.txt");
    when(zipService.getCompressedEntries(any(File.class), any())).thenReturn(
      Collections.singletonList(entry)
    );

    List<ValidationMessage> messages = new ArrayList<>();
    List<CompressedEntry> result = bulkService.getCompressedEntries(
      containerNodeRef,
      tempFile,
      messages
    );
    assertEquals(1, result.size());
  }
}
