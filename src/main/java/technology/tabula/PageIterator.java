package technology.tabula;

import java.io.IOException;
import java.util.Iterator;

public class PageIterator implements Iterator<Page> {

    private ObjectExtractor oe;
    private Iterator<Integer> pageIndexIterator;
    
    public PageIterator(ObjectExtractor oe, Iterable<Integer> pages) {
        super();
        this.oe = oe;
        this.pageIndexIterator = pages.iterator();
    }

    @Override
    public boolean hasNext() {
        return this.pageIndexIterator.hasNext();
    }

    @Override
    public Page next() {
        Page page = null;
        if (!this.hasNext()) {
            throw new IllegalStateException();
        }
        try {
            page = oe.extractPage(this.pageIndexIterator.next());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return page;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();

    }

}