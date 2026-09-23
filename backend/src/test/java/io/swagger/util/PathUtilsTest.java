package io.swagger.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.junit.Test;

public class PathUtilsTest {

  private Path createPath(String... elementStrings) {
    Path path = new Path();
    for (String s : elementStrings) {
      Element element = mock(Element.class);
      when(element.getElementString()).thenReturn(s);
      path.append(element);
    }
    return path;
  }

  @Test
  public void testGetFullPath_whenIncludeFirstSlash_thenStartsWithSlash() {
    Path path = createPath(
      "/",
      "{http://www.alfresco.org/model/content/1.0}company_home",
      "{http://www.alfresco.org/model/content/1.0}circabc",
      "{http://www.alfresco.org/model/content/1.0}myCategory"
    );

    String result = PathUtils.getFullPath(path, true);

    assertEquals("/company_home/circabc/myCategory", result);
  }

  @Test
  public void testGetFullPath_whenExcludeFirstSlash_andNoSlashElement_thenNoLeadingSlash() {
    // Without the "/" element, the first real element is at i=0 == start, so no slash
    Path path = createPath(
      "{http://www.alfresco.org/model/content/1.0}company_home",
      "{http://www.alfresco.org/model/content/1.0}circabc"
    );

    String result = PathUtils.getFullPath(path, false);

    assertEquals("company_home/circabc", result);
  }

  @Test
  public void testGetCircabcPath_whenIncludeFirstSlash_thenSkipsCompanyHomeAndCircabc() {
    // "/" = index 0, "company_home" = index 1, "circabc" = index 2 (start=2 skips these)
    Path path = createPath(
      "/",
      "{http://www.alfresco.org/model/content/1.0}company_home",
      "{http://www.alfresco.org/model/content/1.0}circabc",
      "{http://www.alfresco.org/model/content/1.0}myCategory",
      "{http://www.alfresco.org/model/content/1.0}myIG"
    );

    String result = PathUtils.getCircabcPath(path, true);

    assertEquals("/circabc/myCategory/myIG", result);
  }

  @Test
  public void testGetCircabcPath_whenExcludeFirstSlash_thenNoLeadingSlash() {
    Path path = createPath(
      "/",
      "{http://www.alfresco.org/model/content/1.0}company_home",
      "{http://www.alfresco.org/model/content/1.0}circabc",
      "{http://www.alfresco.org/model/content/1.0}myCategory"
    );

    String result = PathUtils.getCircabcPath(path, false);

    assertEquals("circabc/myCategory", result);
  }

  @Test
  public void testGetCategoryPath_whenValidPath_thenSkipsUpToCircabc() {
    // "/" = 0, "company_home" = 1, "circabc" = 2, "myCategory" = 3 (start=3 skips these)
    Path path = createPath(
      "/",
      "{http://www.alfresco.org/model/content/1.0}company_home",
      "{http://www.alfresco.org/model/content/1.0}circabc",
      "{http://www.alfresco.org/model/content/1.0}myCategory",
      "{http://www.alfresco.org/model/content/1.0}myIG"
    );

    String result = PathUtils.getCategoryPath(path, true);

    assertEquals("/myCategory/myIG", result);
  }

  @Test
  public void testGetInterestGroupPath_whenValidPath_thenSkipsUpToCategory() {
    // "/" = 0, "company_home" = 1, "circabc" = 2, "myCategory" = 3, "myIG" = 4 (start=4)
    Path path = createPath(
      "/",
      "{http://www.alfresco.org/model/content/1.0}company_home",
      "{http://www.alfresco.org/model/content/1.0}circabc",
      "{http://www.alfresco.org/model/content/1.0}myCategory",
      "{http://www.alfresco.org/model/content/1.0}myIG",
      "{http://www.alfresco.org/model/content/1.0}Library"
    );

    String result = PathUtils.getInterestGroupPath(path, true);

    assertEquals("/myIG/Library", result);
  }

  @Test
  public void testGetLibraryPath_whenValidPath_thenSkipsUpToIG() {
    // "/" = 0, "company_home" = 1, "circabc" = 2, "myCategory" = 3, "myIG" = 4, "Library" = 5 (start=5)
    Path path = createPath(
      "/",
      "{http://www.alfresco.org/model/content/1.0}company_home",
      "{http://www.alfresco.org/model/content/1.0}circabc",
      "{http://www.alfresco.org/model/content/1.0}myCategory",
      "{http://www.alfresco.org/model/content/1.0}myIG",
      "{http://www.alfresco.org/model/content/1.0}Library",
      "{http://www.alfresco.org/model/content/1.0}docs"
    );

    String result = PathUtils.getLibraryPath(path, true);

    assertEquals("/Library/docs", result);
  }

  @Test
  public void testGetPath_whenNullElementString_thenSkipped() {
    Path path = new Path();
    Element nullElement = mock(Element.class);
    when(nullElement.getElementString()).thenReturn(null);
    Element validElement = mock(Element.class);
    when(validElement.getElementString()).thenReturn(
      "{http://www.alfresco.org/model/content/1.0}test"
    );
    path.append(nullElement);
    path.append(validElement);

    String result = PathUtils.getPath(path, 0, false);

    assertEquals("test", result);
  }

  @Test
  public void testGetPath_whenContainsElement_thenSkipped() {
    Path path = new Path();
    Element containsElement = mock(Element.class);
    when(containsElement.getElementString()).thenReturn(
      "{http://www.alfresco.org/model/content/1.0}contains"
    );
    Element validElement = mock(Element.class);
    when(validElement.getElementString()).thenReturn(
      "{http://www.alfresco.org/model/content/1.0}folder"
    );
    path.append(containsElement);
    path.append(validElement);

    String result = PathUtils.getPath(path, 0, false);

    assertEquals("folder", result);
  }

  @Test
  public void testGetPath_whenEmptyPath_thenReturnsEmpty() {
    Path path = new Path();

    String result = PathUtils.getFullPath(path, true);

    assertEquals("", result);
  }

  @Test
  public void testGetPath_whenStartBeyondElements_thenReturnsEmpty() {
    Path path = createPath(
      "/",
      "{http://www.alfresco.org/model/content/1.0}company_home"
    );

    String result = PathUtils.getPath(path, 10, true);

    assertEquals("", result);
  }

  @Test
  public void testGetPath_whenElementWithoutNamespace_thenUsedAsIs() {
    Path path = new Path();
    Element element = mock(Element.class);
    when(element.getElementString()).thenReturn("plainName");
    path.append(element);

    String result = PathUtils.getPath(path, 0, false);

    assertEquals("plainName", result);
  }
}
