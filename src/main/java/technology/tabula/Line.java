package technology.tabula;

import java.util.ArrayList;
import java.util.List;

// TODO this class seems superfluous - get rid of it

@SuppressWarnings("serial")
public class Line extends Rectangle {

    List<TextChunk> textChunks = new ArrayList<>();
    public static final Character[] WHITE_SPACE_CHARS = {' ', '\t', '\r', '\n', '\f'};

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public List<TextChunk> getTextElements() {
        return textChunks;
    }

    public void setTextElements(List<TextChunk> textChunks) {
        this.textChunks = textChunks;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public void addTextChunk(int index, TextChunk textChunk) {
        if (index < 0) {
            throw new IllegalArgumentException("Index can't be less than 0.");
        }
        int textChunksAmount = textChunks.size();
        if (textChunksAmount < index + 1) {
            for (; textChunksAmount <= index; textChunksAmount++) {
                textChunks.add(null);
            }
            textChunks.set(index, textChunk);
        } else {
            textChunks.set(index, textChunks.get(index).merge(textChunk));
        }
        merge(textChunk);
    }

    public void addTextChunk(TextChunk textChunk) {
        if (textChunks.isEmpty()) {
            setRect(textChunk);
        } else {
            merge(textChunk);
        }
        textChunks.add(textChunk);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    static Line removeRepeatedCharacters(Line line, Character character, int minimalRunLength) {
        Line lineWithoutTheCharSequence = new Line();
        for (TextChunk textChunkOnTheLine : line.getTextElements()) {
            for (TextChunk textChunkSqueezed : textChunkOnTheLine.squeeze(character, minimalRunLength)) {
                lineWithoutTheCharSequence.addTextChunk(textChunkSqueezed);
            }
        }
        return lineWithoutTheCharSequence;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String s = super.toString();
        sb.append(s, 0, s.length() - 1);
        sb.append(",chunks=");
        for (TextChunk te : this.textChunks) {
            sb.append("'" + te.getText() + "', ");
        }
        sb.append(']');
        return sb.toString();
    }

}
