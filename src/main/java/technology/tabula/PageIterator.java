package technology.tabula;

import java.io.IOException;
import java.util.Iterator;

public class PageIterator implements Iterator<Page> {

    private ObjectExtractor objectExtractor;
    private Iterator<Integer> pageIndexIterator;

    public PageIterator(ObjectExtractor objectExtractor, Iterable<Integer> pages) {
        super();
        this.objectExtractor = objectExtractor;
        this.pageIndexIterator = pages.iterator();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    @Override
    public boolean hasNext() {
        return pageIndexIterator.hasNext();
    }

    @Override
    public Page next() {
        Page nextPage = null;
        if (!this.hasNext()) {
            throw new IllegalStateException();
        }
        try {
            nextPage = objectExtractor.extractPage(pageIndexIterator.next());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nextPage;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
