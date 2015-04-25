package technology.tabula;

import java.util.ArrayList;
import java.util.List;

// TODO this class seems superfluous - get rid of it

@SuppressWarnings("serial")
public class Line extends Rectangle {

    List<TextChunk> textChunks = new ArrayList<TextChunk>();
    public static final Character[] WHITE_SPACE_CHARS = { ' ', '\t', '\r', '\n', '\f' };
    

    public List<TextChunk> getTextElements() {
        return textChunks;
    }

    public void setTextElements(List<TextChunk> textChunks) {
        this.textChunks = textChunks;
    }

    public void addTextChunk(int i, TextChunk textChunk) {
        if (i < 0) {
            throw new IllegalArgumentException("i can't be less than 0");
        }

        int s = this.textChunks.size(); 
        if (s < i + 1) {
            for (; s <= i; s++) {
                this.textChunks.add(null);
            }
            this.textChunks.set(i, textChunk);
        }
        else {
            this.textChunks.set(i, this.textChunks.get(i).merge(textChunk));
        }
        this.merge(textChunk);
    }

    public void addTextChunk(TextChunk textChunk) {
        if (this.textChunks.isEmpty()) {
            this.setRect(textChunk);
        }
        else {
            this.merge(textChunk);
        }
        this.textChunks.add(textChunk);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String s = super.toString();
        sb.append(s.substring(0, s.length() - 1));
        sb.append(",chunks=");
        for (TextChunk te: this.textChunks) {
            sb.append("'" + te.getText() + "', ");
        }
        sb.append(']');
        return sb.toString();
    }

    static Line removeRepeatedCharacters(Line line, Character c, int minRunLength) {

        Line rv = new Line();
        
        for(TextChunk t: line.getTextElements()) {
            for (TextChunk r: t.squeeze(c, minRunLength)) {
                rv.addTextChunk(r);
            }
        }
        
        return rv;
    }
}
