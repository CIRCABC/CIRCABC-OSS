/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConvertRowToColumn<T> {

    public final List<List<T>> convertToHorizontalRead(final List<T> list, final int maxColumn) {
        return splitInSubLists(list, maxColumn);
    }

    public final List<List<T>> convertToVerticalRead(final List<T> list, final int maxColumn) {
        final List<List<T>> subList = splitInSubLists(list, maxColumn);
        final List<List<T>> subListVertical = convertRowToColumn(subList);
        return subListVertical;
    }

    private List<List<T>> splitInSubLists(final List<T> list, final int maxColumn) {
        final List<List<T>> subLists = new ArrayList<>(maxColumn);
        final Iterator<T> listIterator = list.iterator();
        final int maxSubListSize = (list.size() / maxColumn) + 1;
        while (listIterator.hasNext()) {
            final List<T> subList = new ArrayList<>(maxSubListSize);
            int currentElementCount = 1;
            while (listIterator.hasNext() && currentElementCount <= maxSubListSize) {
                subList.add(listIterator.next());
                currentElementCount++;
            }
            subLists.add(subList);
        }
        return subLists;
    }

    private List<List<T>> convertRowToColumn(final List<List<T>> subList) {
        final List<List<T>> columns = new ArrayList<>();
        final int max = getMaxSize(subList);

        for (int i = 1; i <= max; i++) {
            columns.add(new ArrayList<T>());
        }

        for (final List<T> rowElement : subList) {
            int columnPos = 0;
            for (final T columnElement : rowElement) {
                columns.get(columnPos).add(columnElement);
                columnPos++;
            }
        }
        return columns;
    }

    private int getMaxSize(final List<List<T>> subList) {
        int max = 0;
        for (final List<T> row : subList) {
            if (max < row.size()) {
                max = row.size();
            }
        }
        return max;
    }
}
