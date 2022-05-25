package eu.cec.digit.circabc.util.exporter;

import java.util.List;


/**
 * Responsible for crawling Repository contents and invoking the exporter with the contents found.
 *
 * @author David Caruana
 */
public interface ExporterCrawler {

    /**
     * Crawl Repository and export items found
     *
     * @param parameters crawler parameters
     * @param exporter   exporter to export via
     */
    void export(ExporterCrawlerParameters parameters, Exporter exporter,
                List<String> visitedMLNodeRefs);
}
