package eu.europa.ec.digit.circabc.rest.service.migration;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;

/**
 * Service responsible for importing an Interest Group from ImportRoot JAXB format.
 * Uses the original schema format with &lt;guest&gt;, &lt;registredUsers&gt;, and &lt;accessProfile&gt; elements.
 */
public interface IgImportService {
  /**
   * Imports one or more Interest Groups described by the given {@link ImportRoot} into the
   * specified target category. Any persons referenced by the import data are ensured to exist
   * before the Interest Groups and their content are created.
   *
   * @param categoryId the identifier of the target category into which the Interest Group(s)
   *     should be imported
   * @param importRoot the parsed JAXB representation of the import payload, containing the
   *     CIRCABC hierarchy, persons and Interest Group content to create
   * @return an {@link ImportResult} summarizing the outcome, including the number of nodes
   *     created and any errors encountered during the import
   */
  ImportResult importIg(String categoryId, ImportRoot importRoot);
}
