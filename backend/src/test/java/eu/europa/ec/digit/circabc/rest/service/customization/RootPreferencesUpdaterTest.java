package eu.europa.ec.digit.circabc.rest.service.customization;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.config.*;

public class RootPreferencesUpdaterTest {

  private RootPreferencesUpdater updater;
  private NodeService nodeService;
  private FileFolderService fileFolderService;
  private ContentService contentService;
  private MimetypeService mimetypeService;
  private ConfigService configService;
  private PermissionService permissionService;

  @Before
  public void setUp() {
    updater = new RootPreferencesUpdater();
    nodeService = mock(NodeService.class);
    fileFolderService = mock(FileFolderService.class);
    contentService = mock(ContentService.class);
    mimetypeService = mock(MimetypeService.class);
    configService = mock(ConfigService.class);
    permissionService = mock(PermissionService.class);

    updater.setNodeService(nodeService);
    updater.setFileFolderService(fileFolderService);
    updater.setContentService(contentService);
    updater.setMimetypeService(mimetypeService);
    updater.setConfigService(configService);
    updater.setPermissionService(permissionService);
    updater.setConfigCondition("testCondition");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateSpace_whenNullRootSpace_thenThrowsException() {
    updater.updateSpace(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateSpace_whenConfigConditionNull_thenThrowsException() {
    updater.setConfigCondition(null);
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");
    updater.updateSpace(rootSpace);
  }

  @Test
  public void testUpdateSpace_whenSpaceElement_thenCreatesFolder() {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef newFolder = new NodeRef("workspace://SpacesStore/folder-id");

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement spaceElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(spaceElement)
    );
    when(spaceElement.getName()).thenReturn("space");
    when(spaceElement.getAttribute("name")).thenReturn("testFolder");
    when(spaceElement.getChildren()).thenReturn(null);
    when(
      nodeService.getChildByName(
        rootSpace,
        ContentModel.ASSOC_CONTAINS,
        "testFolder"
      )
    ).thenReturn(null);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(newFolder);
    when(
      fileFolderService.create(
        rootSpace,
        "testFolder",
        ContentModel.TYPE_FOLDER
      )
    ).thenReturn(fileInfo);

    updater.updateSpace(rootSpace);

    verify(fileFolderService).create(
      rootSpace,
      "testFolder",
      ContentModel.TYPE_FOLDER
    );
  }

  @Test
  public void testUpdateSpace_whenSpaceAlreadyExists_thenDoesNotCreate() {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef existingFolder = new NodeRef(
      "workspace://SpacesStore/existing-folder"
    );

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement spaceElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(spaceElement)
    );
    when(spaceElement.getName()).thenReturn("space");
    when(spaceElement.getAttribute("name")).thenReturn("testFolder");
    when(spaceElement.getChildren()).thenReturn(null);
    when(
      nodeService.getChildByName(
        rootSpace,
        ContentModel.ASSOC_CONTAINS,
        "testFolder"
      )
    ).thenReturn(existingFolder);

    updater.updateSpace(rootSpace);

    verify(fileFolderService, never()).create(
      any(),
      any(String.class),
      any(QName.class)
    );
  }

  @Test(expected = ConfigException.class)
  public void testUpdateSpace_whenUnknownElement_thenThrowsConfigException() {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement unknownElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(unknownElement)
    );
    when(unknownElement.getName()).thenReturn("unknown");

    updater.updateSpace(rootSpace);
  }

  @Test(expected = ConfigException.class)
  public void testUpdateSpace_whenSpaceNameMissing_thenThrowsConfigException() {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement spaceElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(spaceElement)
    );
    when(spaceElement.getName()).thenReturn("space");
    when(spaceElement.getAttribute("name")).thenReturn(null);

    updater.updateSpace(rootSpace);
  }

  @Test
  public void testUpdateSpace_whenFileWithForceUpdate_thenWritesContent()
    throws Exception {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef contentRef = new NodeRef("workspace://SpacesStore/content-id");

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement fileElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(fileElement)
    );
    when(fileElement.getName()).thenReturn("file");
    when(fileElement.getAttribute("name")).thenReturn("test.txt");
    when(fileElement.getAttribute("revison")).thenReturn(null);
    when(fileElement.getAttribute("forceUpdate")).thenReturn("true");
    when(fileElement.getAttribute("versionable")).thenReturn(null);
    when(fileElement.getAttribute("editOnline")).thenReturn(null);
    when(fileElement.getAttribute("guestPerm")).thenReturn(null);
    when(fileElement.getAttribute("registredPerm")).thenReturn(null);
    when(fileElement.getValue()).thenReturn("classpath:test-resource.txt");

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(contentRef);
    when(
      nodeService.getChildByName(
        rootSpace,
        ContentModel.ASSOC_CONTAINS,
        "test.txt"
      )
    ).thenReturn(null);
    when(
      fileFolderService.create(rootSpace, "test.txt", ContentModel.TYPE_CONTENT)
    ).thenReturn(fileInfo);

    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("txt", "text/plain");
    when(mimetypeService.getMimetypesByExtension()).thenReturn(mimeMap);

    org.alfresco.repo.content.encoding.ContentCharsetFinder charsetFinder =
      mock(org.alfresco.repo.content.encoding.ContentCharsetFinder.class);
    when(mimetypeService.getContentCharsetFinder()).thenReturn(charsetFinder);
    when(charsetFinder.getCharset(any(), eq("text/plain"))).thenReturn(
      java.nio.charset.StandardCharsets.UTF_8
    );

    updater.updateSpace(rootSpace);

    verify(writer).setMimetype("text/plain");
    verify(writer).setEncoding("UTF-8");
    verify(writer).putContent(any(java.io.InputStream.class));
  }

  @Test
  public void testUpdateSpace_whenFileWithRevisionAndExistingOlderRevision_thenUpdates()
    throws Exception {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef contentRef = new NodeRef("workspace://SpacesStore/content-id");

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement fileElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(fileElement)
    );
    when(fileElement.getName()).thenReturn("file");
    when(fileElement.getAttribute("name")).thenReturn("test.xml");
    when(fileElement.getAttribute("revison")).thenReturn("2");
    when(fileElement.getAttribute("forceUpdate")).thenReturn(null);
    when(fileElement.getAttribute("versionable")).thenReturn(null);
    when(fileElement.getAttribute("editOnline")).thenReturn(null);
    when(fileElement.getAttribute("guestPerm")).thenReturn(null);
    when(fileElement.getAttribute("registredPerm")).thenReturn(null);
    when(fileElement.getValue()).thenReturn("classpath:test-resource.xml");

    when(
      nodeService.getChildByName(
        rootSpace,
        ContentModel.ASSOC_CONTAINS,
        "test.xml"
      )
    ).thenReturn(contentRef);
    when(
      nodeService.hasAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE)
    ).thenReturn(true);
    when(
      nodeService.getProperty(contentRef, CircabcModel.PROP_REVISION_NUMBER)
    ).thenReturn(1);

    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("xml", "text/xml");
    when(mimetypeService.getMimetypesByExtension()).thenReturn(mimeMap);

    org.alfresco.repo.content.encoding.ContentCharsetFinder charsetFinder =
      mock(org.alfresco.repo.content.encoding.ContentCharsetFinder.class);
    when(mimetypeService.getContentCharsetFinder()).thenReturn(charsetFinder);
    when(charsetFinder.getCharset(any(), eq("text/xml"))).thenReturn(
      java.nio.charset.StandardCharsets.UTF_8
    );

    updater.updateSpace(rootSpace);

    verify(writer).putContent(any(java.io.InputStream.class));
    verify(nodeService).addProperties(eq(contentRef), anyMap());
  }

  @Test
  public void testUpdateSpace_whenFileWithSameRevision_thenSkipsUpdate() {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef contentRef = new NodeRef("workspace://SpacesStore/content-id");

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement fileElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(fileElement)
    );
    when(fileElement.getName()).thenReturn("file");
    when(fileElement.getAttribute("name")).thenReturn("test.xml");
    when(fileElement.getAttribute("revison")).thenReturn("2");
    when(fileElement.getAttribute("forceUpdate")).thenReturn(null);
    when(fileElement.getAttribute("versionable")).thenReturn(null);
    when(fileElement.getAttribute("editOnline")).thenReturn(null);

    when(
      nodeService.getChildByName(
        rootSpace,
        ContentModel.ASSOC_CONTAINS,
        "test.xml"
      )
    ).thenReturn(contentRef);
    when(
      nodeService.hasAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE)
    ).thenReturn(true);
    when(
      nodeService.getProperty(contentRef, CircabcModel.PROP_REVISION_NUMBER)
    ).thenReturn(2);

    updater.updateSpace(rootSpace);

    verify(contentService, never()).getWriter(any(), any(), anyBoolean());
  }

  @Test
  public void testUpdateSpace_whenVersionableTrue_thenAddsAspect()
    throws Exception {
    NodeRef rootSpace = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef contentRef = new NodeRef("workspace://SpacesStore/content-id");

    Config config = mock(Config.class);
    ConfigElement rootElement = mock(ConfigElement.class);
    ConfigElement fileElement = mock(ConfigElement.class);

    when(
      configService.getConfig(
        eq("testCondition"),
        any(ConfigLookupContext.class)
      )
    ).thenReturn(config);
    when(config.getConfigElement("root")).thenReturn(rootElement);
    when(rootElement.getChildren()).thenReturn(
      Collections.singletonList(fileElement)
    );
    when(fileElement.getName()).thenReturn("file");
    when(fileElement.getAttribute("name")).thenReturn("doc.txt");
    when(fileElement.getAttribute("revison")).thenReturn(null);
    when(fileElement.getAttribute("forceUpdate")).thenReturn("true");
    when(fileElement.getAttribute("versionable")).thenReturn("true");
    when(fileElement.getAttribute("editOnline")).thenReturn(null);
    when(fileElement.getAttribute("guestPerm")).thenReturn(null);
    when(fileElement.getAttribute("registredPerm")).thenReturn(null);
    when(fileElement.getValue()).thenReturn("classpath:test-resource.txt");

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(contentRef);
    when(
      nodeService.getChildByName(
        rootSpace,
        ContentModel.ASSOC_CONTAINS,
        "doc.txt"
      )
    ).thenReturn(null);
    when(
      fileFolderService.create(rootSpace, "doc.txt", ContentModel.TYPE_CONTENT)
    ).thenReturn(fileInfo);
    when(
      nodeService.hasAspect(contentRef, ContentModel.ASPECT_VERSIONABLE)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE)
    ).thenReturn(false);

    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("txt", "text/plain");
    when(mimetypeService.getMimetypesByExtension()).thenReturn(mimeMap);

    org.alfresco.repo.content.encoding.ContentCharsetFinder charsetFinder =
      mock(org.alfresco.repo.content.encoding.ContentCharsetFinder.class);
    when(mimetypeService.getContentCharsetFinder()).thenReturn(charsetFinder);
    when(charsetFinder.getCharset(any(), eq("text/plain"))).thenReturn(
      java.nio.charset.StandardCharsets.UTF_8
    );

    updater.updateSpace(rootSpace);

    verify(nodeService).addAspect(
      eq(contentRef),
      eq(ContentModel.ASPECT_VERSIONABLE),
      eq(
        Collections.singletonMap(
          ContentModel.PROP_AUTO_VERSION,
          (Serializable) Boolean.TRUE
        )
      )
    );
  }

  @Test
  public void testGetConfigCondition() {
    updater.setConfigCondition("myCondition");
    assertEquals("myCondition", updater.getConfigCondition());
  }
}
