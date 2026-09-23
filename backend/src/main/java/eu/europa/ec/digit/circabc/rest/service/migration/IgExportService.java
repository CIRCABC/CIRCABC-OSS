package eu.europa.ec.digit.circabc.rest.service.migration;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service responsible for exporting an Interest Group to ImportRoot JAXB format.
 * Uses the original schema format with &lt;guest&gt;, &lt;registredUsers&gt;, and &lt;accessProfile&gt; elements.
 */
public interface IgExportService {
  /**
   * Exports the given Interest Group into the {@link ImportRoot} JAXB structure using the original
   * migration schema format (with {@code <guest>}, {@code <registredUsers>}, and
   * {@code <accessProfile>} elements).
   *
   * @param igRef the {@link NodeRef} of the Interest Group to export
   * @return the {@link ImportRoot} representation of the Interest Group, ready to be marshalled to XML
   */
  ImportRoot exportIg(NodeRef igRef);
}
