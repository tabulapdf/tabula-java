package technology.tabula;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

@SuppressWarnings("serial")
public class TextElement extends Rectangle implements HasText {

    private final String text;
    private final PDFont font;
    private float fontSize;
    private float widthOfSpace, dir;
    private static final float AVERAGE_CHAR_TOLERANCE = 0.3f;

    public TextElement(float y, float x, float width, float height,
                       PDFont font, float fontSize, String c, float widthOfSpace) {
        this(y, x, width, height, font, fontSize, c, widthOfSpace, 0f);
    }

    public TextElement(float y, float x, float width, float height,
                       PDFont font, float fontSize, String c, float widthOfSpace, float dir) {
        super();
        this.setRect(x, y, width, height);
        this.text = c;
        this.widthOfSpace = widthOfSpace;
        this.fontSize = fontSize;
        this.font = font;
        this.dir = dir;
    }

    @Override public String getText() {
        return text;
    }

    public float getDirection() {
        return dir;
    }

    public float getWidthOfSpace() {
        return widthOfSpace;
    }

    public PDFont getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        String s = super.toString();
        sb.append(s.substring(0, s.length() - 1));
        sb.append(String.format(",text=\"%s\"]", this.getText()));
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + java.lang.Float.floatToIntBits(dir);
        result = prime * result + ((font == null) ? 0 : font.hashCode());
        result = prime * result + java.lang.Float.floatToIntBits(fontSize);
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + java.lang.Float.floatToIntBits(widthOfSpace);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TextElement other = (TextElement) obj;
        if (java.lang.Float.floatToIntBits(dir) != java.lang.Float
                .floatToIntBits(other.dir))
            return false;
        if (font == null) {
            if (other.font != null)
                return false;
        } else if (!font.equals(other.font))
            return false;
        if (java.lang.Float.floatToIntBits(fontSize) != java.lang.Float
                .floatToIntBits(other.fontSize))
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return java.lang.Float.floatToIntBits(widthOfSpace) == java.lang.Float
                .floatToIntBits(other.widthOfSpace);
    }

    public static List<TextChunk> mergeWords(List<TextElement> textElements) {
        return mergeWords(textElements, new ArrayList<Ruling>());
    }

    /**
     * heuristically merge a list of TextElement into a list of TextChunk
     * ported from from PDFBox's PDFTextStripper.writePage, with modifications.
     * Here be dragons
     */
    public static List<TextChunk> mergeWords(List<TextElement> textElements, List<Ruling> verticalRulings) {

        List<TextChunk> textChunks = new ArrayList<>();

        if (textElements.isEmpty()) {
            return textChunks;
        }

        // it's a problem that this `remove` is side-effecty
        // other things depend on `textElements` and it can sometimes lead to the first textElement in textElement
        // not appearing in the final output because it's been removed here.
        // https://github.com/tabulapdf/tabula-java/issues/78
        List<TextElement> copyOfTextElements = new ArrayList<>(textElements);
        textChunks.add(new TextChunk(copyOfTextElements.remove(0)));
        TextChunk firstTC = textChunks.get(0);

        float previousAveCharWidth = (float) firstTC.getWidth();
        float endOfLastTextX = firstTC.getRight();
        float maxYForLine = firstTC.getBottom();
        float maxHeightForLine = (float) firstTC.getHeight();
        float minYTopForLine = firstTC.getTop();
        float lastWordSpacing = -1;
        float wordSpacing, deltaSpace, averageCharWidth, deltaCharWidth;
        float expectedStartOfNextWordX, dist;
        TextElement sp, prevChar;
        TextChunk currentChunk;
        boolean sameLine, acrossVerticalRuling;

        for (TextElement chr : copyOfTextElements) {
            currentChunk = textChunks.get(textChunks.size() - 1);
            prevChar = currentChunk.textElements.get(currentChunk.textElements.size() - 1);

            // if same char AND overlapped, skip
            if ((chr.getText().equals(prevChar.getText())) && (prevChar.overlapRatio(chr) > 0.5)) {
                continue;
            }

            // if chr is a space that overlaps with prevChar, skip
            if (chr.getText().equals(" ") && Utils.feq(prevChar.getLeft(), chr.getLeft()) && Utils.feq(prevChar.getTop(), chr.getTop())) {
                continue;
            }

            // Resets the average character width when we see a change in font
            // or a change in the font size
            if ((chr.getFont() != prevChar.getFont()) || !Utils.feq(chr.getFontSize(), prevChar.getFontSize())) {
                previousAveCharWidth = -1;
            }

            // is there any vertical ruling that goes across chr and prevChar?
            acrossVerticalRuling = false;
            for (Ruling r : verticalRulings) {
                if (
                        (verticallyOverlapsRuling(prevChar, r) && verticallyOverlapsRuling(chr, r)) &&
                                (prevChar.x < r.getPosition() && chr.x > r.getPosition()) || (prevChar.x > r.getPosition() && chr.x < r.getPosition())
                        ) {
                    acrossVerticalRuling = true;
                    break;
                }
            }

            // Estimate the expected width of the space based on the
            // space character with some margin.
            wordSpacing = chr.getWidthOfSpace();
            deltaSpace = 0;
            if (java.lang.Float.isNaN(wordSpacing) || wordSpacing == 0) {
                deltaSpace = java.lang.Float.MAX_VALUE;
            } else if (lastWordSpacing < 0) {
                deltaSpace = wordSpacing * 0.5f; // 0.5 == spacing tolerance
            } else {
                deltaSpace = ((wordSpacing + lastWordSpacing) / 2.0f) * 0.5f;
            }

            // Estimate the expected width of the space based on the
            // average character width with some margin. This calculation does not
            // make a true average (average of averages) but we found that it gave the
            // best results after numerous experiments. Based on experiments we also found that
            // .3 worked well.
            if (previousAveCharWidth < 0) {
                averageCharWidth = (float) (chr.getWidth() / chr.getText().length());
            } else {
                averageCharWidth = (float) ((previousAveCharWidth + (chr.getWidth() / chr.getText().length())) / 2.0f);
            }
            deltaCharWidth = averageCharWidth * AVERAGE_CHAR_TOLERANCE;

            // Compares the values obtained by the average method and the wordSpacing method and picks
            // the smaller number.
            expectedStartOfNextWordX = -java.lang.Float.MAX_VALUE;

            if (endOfLastTextX != -1) {
                expectedStartOfNextWordX = endOfLastTextX + Math.min(deltaCharWidth, deltaSpace);
            }

            // new line?
            sameLine = true;
            if (!Utils.overlap(chr.getBottom(), chr.height, maxYForLine, maxHeightForLine)) {
                endOfLastTextX = -1;
                expectedStartOfNextWordX = -java.lang.Float.MAX_VALUE;
                maxYForLine = -java.lang.Float.MAX_VALUE;
                maxHeightForLine = -1;
                minYTopForLine = java.lang.Float.MAX_VALUE;
                sameLine = false;
            }

            endOfLastTextX = chr.getRight();

            // should we add a space?
            if (!acrossVerticalRuling &&
                    sameLine &&
                    expectedStartOfNextWordX < chr.getLeft() &&
                    !prevChar.getText().endsWith(" ")) {

                sp = new TextElement(prevChar.getTop(),
                        prevChar.getLeft(),
                        expectedStartOfNextWordX - prevChar.getLeft(),
                        (float) prevChar.getHeight(),
                        prevChar.getFont(),
                        prevChar.getFontSize(),
                        " ",
                        prevChar.getWidthOfSpace());

                currentChunk.add(sp);
            } else {
                sp = null;
            }

            maxYForLine = Math.max(chr.getBottom(), maxYForLine);
            maxHeightForLine = (float) Math.max(maxHeightForLine, chr.getHeight());
            minYTopForLine = Math.min(minYTopForLine, chr.getTop());

            dist = chr.getLeft() - (sp != null ? sp.getRight() : prevChar.getRight());

            if (!acrossVerticalRuling &&
                    sameLine &&
                    (dist < 0 ? currentChunk.verticallyOverlaps(chr) : dist < wordSpacing)) {
                currentChunk.add(chr);
            } else { // create a new chunk
                textChunks.add(new TextChunk(chr));
            }

            lastWordSpacing = wordSpacing;
            previousAveCharWidth = (float) (sp != null ? (averageCharWidth + sp.getWidth()) / 2.0f : averageCharWidth);
        }


        List<TextChunk> textChunksSeparatedByDirectionality = new ArrayList<>();
        // count up characters by directionality
        for (TextChunk chunk : textChunks) {
            // choose the dominant direction
            boolean isLtrDominant = chunk.isLtrDominant() != -1; // treat neutral as LTR
            TextChunk dirChunk = chunk.groupByDirectionality(isLtrDominant);
            textChunksSeparatedByDirectionality.add(dirChunk);
        }

        return textChunksSeparatedByDirectionality;
    }

    private static boolean verticallyOverlapsRuling(TextElement te, Ruling r) {
        return Math.max(0, Math.min(te.getBottom(), r.getY2()) - Math.max(te.getTop(), r.getY1())) > 0;
    }

}
