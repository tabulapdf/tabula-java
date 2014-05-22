package org.nerdpower.tabula;

import java.util.List;

public class TableWithRulingLines extends Table {

    List<Ruling> verticalRulings, horizontalRulings;
    
    public TableWithRulingLines() {
        // TODO Auto-generated constructor stub
    }

    public TableWithRulingLines(Rectangle area, Page page, List<Cell> cells,
            List<Ruling> horizontalRulings,
            List<Ruling> verticalRulings) {
        super();
        this.setRect(area);
        this.page = page;
        this.verticalRulings = verticalRulings;
        this.horizontalRulings = horizontalRulings;
        // TODO process cells
        
    }
    
    
    
}
