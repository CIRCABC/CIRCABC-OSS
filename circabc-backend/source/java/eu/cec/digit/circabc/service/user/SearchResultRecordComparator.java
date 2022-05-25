package eu.cec.digit.circabc.service.user;

import java.io.Serializable;
import java.util.Comparator;

public class SearchResultRecordComparator implements Comparator<SearchResultRecord>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5428412809586963529L;

    private SearchResultRecordComparator() {
    }

    public static SearchResultRecordComparator getInstance() {
        return SearchResultRecordComparatorHolder.INSTANCE;
    }

    @Override
    public int compare(SearchResultRecord first, SearchResultRecord second) {
        return first.getLastName().compareTo(second.getLastName());
    }

    private static class SearchResultRecordComparatorHolder {

        public static final SearchResultRecordComparator INSTANCE = new SearchResultRecordComparator();
    }
}
