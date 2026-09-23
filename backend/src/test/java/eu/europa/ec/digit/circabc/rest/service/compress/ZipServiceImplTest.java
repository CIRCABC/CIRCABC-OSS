package eu.europa.ec.digit.circabc.rest.service.compress;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ZipServiceImplTest {

  private ZipServiceImpl zipService;
  private NodeService nodeService;
  private ContentService contentService;
  private MimetypeService mimetypeService;
  private FileFolderService fileFolderService;
  private PermissionService permissionService;
  private DictionaryService dictionaryService;
  private ApiToolBox apiToolBox;

  @Before
  public void setUp() throws Exception {
    zipService = new ZipServiceImpl();
    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    mimetypeService = mock(MimetypeService.class);
    fileFolderService = mock(FileFolderService.class);
    permissionService = mock(PermissionService.class);
    dictionaryService = mock(DictionaryService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("nodeService", nodeService);
    setField("contentService", contentService);
    setField("mimetypeService", mimetypeService);
    setField("fileFolderService", fileFolderService);
    setField("permissionService", permissionService);
    setField("dictionaryService", dictionaryService);
    setField("apiToolBox", apiToolBox);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ZipServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(zipService, value);
  }

  @Test
  public void testAddingFilesIntoArchive_whenFilesProvided_thenCreatesZip()
    throws Exception {
    File tempFile = File.createTempFile("test-content", ".txt");
    tempFile.deleteOnExit();
    try (FileWriter fw = new FileWriter(tempFile)) {
      fw.write("hello world");
    }

    File compressedFile = File.createTempFile("test-archive", ".zip");
    compressedFile.deleteOnExit();

    zipService.addingFileIntoArchive(tempFile, compressedFile);

    assertTrue(compressedFile.exists());
    assertTrue(compressedFile.length() > 0);
  }

  @Test
  public void testAddingFilesIntoArchive_whenMultipleFiles_thenAllIncluded()
    throws Exception {
    File file1 = File.createTempFile("file1", ".txt");
    file1.deleteOnExit();
    try (FileWriter fw = new FileWriter(file1)) {
      fw.write("content1");
    }
    File file2 = File.createTempFile("file2", ".txt");
    file2.deleteOnExit();
    try (FileWriter fw = new FileWriter(file2)) {
      fw.write("content2");
    }

    File compressedFile = File.createTempFile("test-archive", ".zip");
    compressedFile.deleteOnExit();

    List<File> files = Arrays.asList(file1, file2);
    zipService.addingFilesIntoArchive(files, compressedFile);

    assertTrue(compressedFile.exists());
    assertTrue(compressedFile.length() > 0);
  }

  @Test
  public void testAddingFilesIntoArchive_withIndexFile_thenIndexIncluded()
    throws Exception {
    File file1 = File.createTempFile("file1", ".txt");
    file1.deleteOnExit();
    try (FileWriter fw = new FileWriter(file1)) {
      fw.write("content1");
    }

    File indexFile = File.createTempFile("index", ".txt");
    indexFile.deleteOnExit();
    try (FileWriter fw = new FileWriter(indexFile)) {
      fw.write("index content");
    }

    File compressedFile = File.createTempFile("test-archive", ".zip");
    compressedFile.deleteOnExit();

    zipService.addingFilesIntoArchive(
      Collections.singletonList(file1),
      compressedFile,
      indexFile
    );

    assertTrue(compressedFile.exists());
    assertTrue(compressedFile.length() > 0);
  }

  @Test
  public void testGetRelativeLibraryPath_whenNodeHasPath_thenReturnsRelativePath() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-node"
    );

    Path path = new Path();
    when(nodeService.getPath(nodeRef)).thenReturn(path);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(libraryRef);
    when(
      nodeService.getProperty(libraryRef, ContentModel.PROP_NAME)
    ).thenReturn(null);

    String result = zipService.getRelativeLibraryPath(nodeRef);

    assertNotNull(result);
    verify(nodeService).getPath(nodeRef);
    verify(apiToolBox).getCurrentLibraryRoot(nodeRef);
  }

  @Test
  public void testGetAbsoluteLibraryPath_whenLibraryHasName_thenIncludesLibraryName() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-node"
    );

    Path path = new Path();
    when(nodeService.getPath(nodeRef)).thenReturn(path);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(libraryRef);
    when(
      nodeService.getProperty(libraryRef, ContentModel.PROP_NAME)
    ).thenReturn("Library");

    String result = zipService.getAbsoluteLibraryPath(nodeRef);

    assertEquals("/Library", result);
  }

  @Test
  public void testGetAbsoluteLibraryPath_whenLibraryNameNull_thenReturnsEmpty() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-node"
    );

    Path path = new Path();
    when(nodeService.getPath(nodeRef)).thenReturn(path);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(libraryRef);
    when(
      nodeService.getProperty(libraryRef, ContentModel.PROP_NAME)
    ).thenReturn(null);

    String result = zipService.getAbsoluteLibraryPath(nodeRef);

    assertEquals("", result);
  }

  @Test
  public void testAddingFileIntoArchive_whenNodeRef_thenNodeNotExist_thenEmptyZip() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node"
    );

    when(nodeService.exists(nodeRef)).thenReturn(false);

    File compressedFile;
    try {
      compressedFile = File.createTempFile("test-archive", ".zip");
      compressedFile.deleteOnExit();
    } catch (IOException e) {
      fail("Could not create temp file");
      return;
    }

    zipService.addingFileIntoArchive(nodeRef, compressedFile);

    verify(nodeService).exists(nodeRef);
  }

  @Test
  public void testAddingFileIntoArchive_whenNodeNotReadable_thenSkipped() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node"
    );

    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.DENIED);

    File compressedFile;
    try {
      compressedFile = File.createTempFile("test-archive", ".zip");
      compressedFile.deleteOnExit();
    } catch (IOException e) {
      fail("Could not create temp file");
      return;
    }

    zipService.addingFileIntoArchive(nodeRef, compressedFile);

    verify(permissionService).hasPermission(nodeRef, PermissionService.READ);
  }

  @Test
  public void testAddingFileIntoArchive_whenContentNode_thenAddsToZip()
    throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-node"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-node"
    );

    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    TypeDefinition typeDef = mock(TypeDefinition.class);
    when(dictionaryService.getType(ContentModel.TYPE_CONTENT)).thenReturn(
      typeDef
    );

    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn(null);

    ContentReader contentReader = mock(ContentReader.class);
    ContentData contentData = mock(ContentData.class);
    when(
      contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentReader);
    when(contentReader.getContentData()).thenReturn(contentData);
    when(contentData.getSize()).thenReturn(5L);
    byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
    when(contentReader.getContentInputStream()).thenReturn(
      new ByteArrayInputStream(content)
    );

    Path path = new Path();
    when(nodeService.getPath(nodeRef)).thenReturn(path);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(libraryRef);
    when(
      nodeService.getProperty(libraryRef, ContentModel.PROP_NAME)
    ).thenReturn(null);

    File compressedFile = File.createTempFile("test-archive", ".zip");
    compressedFile.deleteOnExit();

    zipService.addingFileIntoArchive(nodeRef, compressedFile);

    assertTrue(compressedFile.length() > 0);
    verify(contentService).getReader(nodeRef, ContentModel.PROP_CONTENT);
  }

  @Test
  public void testAddingFileIntoArchive_whenSecurityRankingSensitive_thenSkipped()
    throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "sensitive-node"
    );

    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    TypeDefinition typeDef = mock(TypeDefinition.class);
    when(dictionaryService.getType(ContentModel.TYPE_CONTENT)).thenReturn(
      typeDef
    );

    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn("SENSITIVE");

    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-node"
    );
    Path path = new Path();
    when(nodeService.getPath(nodeRef)).thenReturn(path);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(libraryRef);
    when(
      nodeService.getProperty(libraryRef, ContentModel.PROP_NAME)
    ).thenReturn(null);

    File compressedFile = File.createTempFile("test-archive", ".zip");
    compressedFile.deleteOnExit();

    zipService.addingFileIntoArchive(nodeRef, compressedFile);

    verify(contentService, never()).getReader(eq(nodeRef), any(QName.class));
  }

  @Test
  public void testAddingFileIntoArchive_whenSecurityRankingPublic_thenAllowed()
    throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "public-node"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-node"
    );

    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

    TypeDefinition typeDef = mock(TypeDefinition.class);
    when(dictionaryService.getType(ContentModel.TYPE_CONTENT)).thenReturn(
      typeDef
    );

    when(
      nodeService.getProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn("PUBLIC");

    ContentReader contentReader = mock(ContentReader.class);
    ContentData contentData = mock(ContentData.class);
    when(
      contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentReader);
    when(contentReader.getContentData()).thenReturn(contentData);
    when(contentData.getSize()).thenReturn(4L);
    when(contentReader.getContentInputStream()).thenReturn(
      new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8))
    );

    Path path = new Path();
    when(nodeService.getPath(nodeRef)).thenReturn(path);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(libraryRef);
    when(
      nodeService.getProperty(libraryRef, ContentModel.PROP_NAME)
    ).thenReturn(null);

    File compressedFile = File.createTempFile("test-archive", ".zip");
    compressedFile.deleteOnExit();

    zipService.addingFileIntoArchive(nodeRef, compressedFile);

    verify(contentService).getReader(nodeRef, ContentModel.PROP_CONTENT);
  }

  @Test
  public void testAddingFileIntoArchive_whenFolderWithChildren_thenRecurses()
    throws Exception {
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-node"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-node"
    );
    NodeRef libraryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "library-node"
    );

    when(nodeService.exists(folderRef)).thenReturn(true);
    when(
      permissionService.hasPermission(folderRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(folderRef)).thenReturn(ContentModel.TYPE_FOLDER);
    TypeDefinition folderTypeDef = mock(TypeDefinition.class);
    when(dictionaryService.getType(ContentModel.TYPE_FOLDER)).thenReturn(
      folderTypeDef
    );
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_FOLDER,
        ContentModel.TYPE_SYSTEM_FOLDER
      )
    ).thenReturn(false);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);
    when(nodeService.getChildAssocs(folderRef)).thenReturn(
      Collections.singletonList(childAssoc)
    );

    when(nodeService.exists(childRef)).thenReturn(false);

    Path folderPath = new Path();
    when(nodeService.getPath(folderRef)).thenReturn(folderPath);
    when(apiToolBox.getCurrentLibraryRoot(folderRef)).thenReturn(libraryRef);
    when(
      nodeService.getProperty(folderRef, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn(null);
    when(
      nodeService.getProperty(libraryRef, ContentModel.PROP_NAME)
    ).thenReturn(null);

    File compressedFile = File.createTempFile("test-archive", ".zip");
    compressedFile.deleteOnExit();

    zipService.addingFileIntoArchive(folderRef, compressedFile);

    verify(nodeService).getChildAssocs(folderRef);
  }

  // --- getCompressedEntries ---

  @Test
  public void testGetCompressedEntries_whenValidZip_thenReturnsEntries()
    throws Exception {
    File tempZip = createTempZipFile("test.txt", "hello");
    List<
      eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage
    > messages = new ArrayList<>();

    List<CompressedEntry> result = zipService.getCompressedEntries(
      tempZip,
      messages
    );

    assertEquals(1, result.size());
    assertEquals("test.txt", result.get(0).getFileName());
    assertTrue(messages.isEmpty());
    tempZip.delete();
  }

  @Test
  public void testGetCompressedEntries_whenInvalidFile_thenAddsErrorMessage()
    throws Exception {
    File tempFile = File.createTempFile("notazip", ".zip");
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write("not a zip".getBytes(StandardCharsets.UTF_8));
    }
    List<
      eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage
    > messages = new ArrayList<>();

    List<CompressedEntry> result = zipService.getCompressedEntries(
      tempFile,
      messages
    );

    assertTrue(result.isEmpty());
    assertFalse(messages.isEmpty());
    tempFile.delete();
  }

  // --- extract (boolean return) ---

  private File createTempZipFile(String entryName, String content)
    throws Exception {
    File tempZip = File.createTempFile("test", ".zip");
    try (
      java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
        new FileOutputStream(tempZip)
      )
    ) {
      zos.putNextEntry(new java.util.zip.ZipEntry(entryName));
      zos.write(content.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
    }
    return tempZip;
  }
}
