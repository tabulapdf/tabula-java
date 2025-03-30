package technology.tabula;

import java.util.ArrayList;
import java.util.List;

public class Rulings {
    private List<Ruling> rulings = new ArrayList<>();
    private List<Ruling> cleanRulings = new ArrayList<>();
//    private float minCharWidth;
//    private float minCharHeight;

    public Rulings(List<Ruling> rulings) {
        this.rulings = rulings;
//        this.minCharWidth = minCharWidth;
//        this.minCharHeight = minCharHeight;
    }

    public List<Ruling> getRulings() {
        if (rulings == null || rulings.isEmpty()) {
            return rulings;
        }
        List<Ruling> verticalRulingLines = getVerticalRulings();
        List<Ruling> horizontalRulingLines = getHorizontalRulings();

        cleanRulings = new ArrayList<>(verticalRulingLines);
        cleanRulings.addAll(horizontalRulingLines);
        return cleanRulings;
    }

    public List<Ruling> getVerticalRulings() {
        List<Ruling> verticalRulings = new ArrayList<>();
        for (Ruling ruling : rulings) {
            if (ruling.vertical()) {
                verticalRulings.add(ruling);
            }
        }
        return Ruling.collapseOrientedRulings(verticalRulings);
    }

    public List<Ruling> getHorizontalRulings() {
        List<Ruling> horizontalRulings = new ArrayList<>();
        for (Ruling ruling : rulings) {
            if (ruling.vertical()) {
                horizontalRulings.add(ruling);
            }
        }
        return Ruling.collapseOrientedRulings(horizontalRulings);
    }

    public void addRuling(Ruling ruling) {
        if (ruling.oblique()) {
            throw new UnsupportedOperationException("Can't add an oblique ruling.");
        }
        rulings.add(ruling);
    }

    public List<Ruling> getUnprocessedRulings() {
        return rulings;
    }

}
