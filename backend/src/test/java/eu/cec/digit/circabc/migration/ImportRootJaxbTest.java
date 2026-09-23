package eu.cec.digit.circabc.migration;

import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.Statistics;
import eu.cec.digit.circabc.migration.entities.generated.VersionHistory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.*;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.Assert;
import org.junit.Test;

public class ImportRootJaxbTest {

  @Test
  public void testMarshalUnmarshalRoundTrip() throws Exception {
    // Build a simple ImportRoot
    Circabc circabc = new Circabc();
    InterestGroup ig = new InterestGroup();
    ig.setName(new NameProperty("TestIG"));

    Library library = new Library();
    Space space = new Space();
    space.setName(new NameProperty("Documents"));
    Content content = new Content();
    content.setName(new NameProperty("test.pdf"));
    content.setUri("https://example.com/content/test.pdf");
    library.getSpaces().add(space);
    library.getContents().add(content);
    ig.setLibrary(library);

    Category category = new Category();
    category.setName(new NameProperty("TestCategory"));
    category.getInterestGroups().add(ig);
    CategoryHeader header = new CategoryHeader();
    header.setName(new NameProperty("TestHeader"));
    header.getCategories().add(category);
    circabc.getCategoryHeaders().add(header);

    ImportRoot root = new ImportRoot(
      circabc,
      new Persons(),
      new LogFile(),
      new VersionHistory(),
      new Statistics()
    );

    // Marshal to XML
    JAXBContext ctx = JAXBContext.newInstance(ImportRoot.class);
    Marshaller m = ctx.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    StringWriter sw = new StringWriter();
    m.marshal(root, sw);
    String xml = sw.toString();

    Assert.assertTrue(xml.contains("TestIG"));
    Assert.assertTrue(xml.contains("Documents"));
    Assert.assertTrue(xml.contains("test.pdf"));
    Assert.assertTrue(xml.contains("TestCategory"));

    // Unmarshal back
    Unmarshaller um = ctx.createUnmarshaller();
    ImportRoot parsed = (ImportRoot) um.unmarshal(new StringReader(xml));

    Assert.assertNotNull(parsed.getCircabc());
    Assert.assertEquals(1, parsed.getCircabc().getCategoryHeaders().size());
    CategoryHeader parsedHeader = parsed
      .getCircabc()
      .getCategoryHeaders()
      .get(0);
    Assert.assertEquals(
      "TestHeader",
      parsedHeader.getName().getValue().toString()
    );
    Category parsedCat = parsedHeader.getCategories().get(0);
    Assert.assertEquals(
      "TestCategory",
      parsedCat.getName().getValue().toString()
    );
    InterestGroup parsedIg = parsedCat.getInterestGroups().get(0);
    Assert.assertEquals("TestIG", parsedIg.getName().getValue().toString());
    Assert.assertNotNull(parsedIg.getLibrary());
    Assert.assertEquals(1, parsedIg.getLibrary().getSpaces().size());
    Assert.assertEquals(1, parsedIg.getLibrary().getContents().size());
  }
}
