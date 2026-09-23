package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import org.junit.Test;

public class IndexHeaderColumnTest {

  @Test
  public void testConstructor_isPrivate() throws Exception {
    Constructor<IndexHeaderColumn> constructor =
      IndexHeaderColumn.class.getDeclaredConstructor();
    assertFalse(constructor.canAccess(null));
  }

  @Test
  public void testConstants_haveExpectedValues() {
    assertEquals("NAME", IndexHeaderColumn.NAME);
    assertEquals("TITLE", IndexHeaderColumn.TITLE);
    assertEquals("DESCRIPTION", IndexHeaderColumn.DESCRIPTION);
    assertEquals("AUTHOR", IndexHeaderColumn.AUTHOR);
    assertEquals("KEYWORDS", IndexHeaderColumn.KEYWORDS);
    assertEquals("STATUS", IndexHeaderColumn.STATUS);
    assertEquals("ISSUE DATE", IndexHeaderColumn.ISSUE_DATE);
    assertEquals("REFERENCE", IndexHeaderColumn.REFERENCE);
    assertEquals("EXPIRDATE", IndexHeaderColumn.EXPIRATION_DATE);
    assertEquals("SECRANK", IndexHeaderColumn.SECURITY_RANKING);
    assertEquals("ATTRI", IndexHeaderColumn.ATTRIPREFIX);
    assertEquals("TYPE", IndexHeaderColumn.TYPE_DOCUMENT);
    assertEquals("TRANSLATOR", IndexHeaderColumn.TRANSLATOR);
    assertEquals("LANG", IndexHeaderColumn.DOC_LANG);
    assertEquals("NOCONTENT", IndexHeaderColumn.NO_CONTENT);
    assertEquals("ORILANG", IndexHeaderColumn.ORI_LANG);
    assertEquals("RELTRANS", IndexHeaderColumn.REL_TRANS);
    assertEquals("OVERWRITE", IndexHeaderColumn.OVERWRITE);
  }

  @Test
  public void testAttriConstants_followNamingPattern() {
    assertEquals("ATTRI1", IndexHeaderColumn.ATTRI1);
    assertEquals("ATTRI10", IndexHeaderColumn.ATTRI10);
    assertEquals("ATTRI20", IndexHeaderColumn.ATTRI20);
  }
}
