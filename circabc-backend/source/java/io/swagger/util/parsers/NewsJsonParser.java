package io.swagger.util.parsers;

import io.swagger.model.I18nProperty;
import io.swagger.model.News;
import io.swagger.util.Converter;
import org.apache.commons.validator.routines.UrlValidator;
import org.joda.time.LocalDate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class NewsJsonParser {

    private static final String CONTENT = "content";
    private static final String ID = "id";
    private static final String PATTERN = "pattern";
    private static final String LAYOUT = "layout";
    private static final String SIZE = "size";
    private static final String DATE = "date";
    private static final String TITLE = "title";
    private static final String URL = "url";

    private NewsJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static News parse(WebScriptRequest req)
            throws IOException, ParseException, java.text.ParseException {

        News result = new News();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        String id = (String) json.get(ID);
        if (id != null) {
            result.setId(id);
        }

        String content = (String) json.get(CONTENT);
        if (content != null) {
            result.setContent(content);
        }

        String pattern = (String) json.get(PATTERN);
        if (pattern != null) {
            result.setPattern(News.PatternEnum.fromValue(pattern));

            if (result.getPattern().equals(News.PatternEnum.DATE)) {

                String date = (String) json.get(DATE);
                if (date != null) {
                    result.setDate(new LocalDate(Converter.convertStringToSimpleDate(date)));
                }
            }

            if (result.getPattern().equals(News.PatternEnum.IFRAME)) {
                result.setTitle(new I18nProperty());
            } else {
                JSONObject titles = (JSONObject) json.get(TITLE);
                if (titles == null) {
                    throw new ParseException(0, "Empty title");
                }

                result.setTitle(Converter.toI18NProperty(titles));
            }
        }

        String layout = (String) json.get(LAYOUT);
        if (layout != null) {
            result.setLayout(News.LayoutEnum.fromValue(layout));
        }

        Integer size = Integer.parseInt(json.get(SIZE).toString());
        result.setSize(size);

        String url = (String) json.get(URL);
        if (!"".equals(url) && url != null) {
            UrlValidator urlValidator = new UrlValidator();
            if (!urlValidator.isValid(url)) {
                throw new IOException("Invalid URL: " + url);
            }
            result.setUrl(url);
        }

        return result;
    }
}
