package technology.tabula;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextStripper extends PDFTextStripper {

    private static final String NBSP = "\u00A0";
    private static final float AVG_HEIGHT_MULT_THRESHOLD = 6.0f;
    private static final float MAX_BLANK_FONT_SIZE = 40.0f;
    private static final float MIN_BLANK_FONT_SIZE = 2.0f;
    private final PDDocument document;
    private final ArrayList<TextElement> textElements;
    private final RectangleSpatialIndex<TextElement> spatialIndex;
    private float minCharWidth = Float.MAX_VALUE;
    private float minCharHeight = Float.MAX_VALUE;
    private float totalHeight = 0.0f;
    private int countHeight = 0;

    public TextStripper(PDDocument document, int pageNumber) throws IOException {
        super();
        this.document = document;
        this.setStartPage(pageNumber);
        this.setEndPage(pageNumber);
        this.textElements = new ArrayList<>();
        this.spatialIndex = new RectangleSpatialIndex<>();
    }

    public void process() throws IOException {
        this.getText(this.document);
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException
    {
        for (TextPosition textPosition: textPositions)
        {
            if (textPosition == null) {
                continue;
            }

            String c = textPosition.getUnicode();

            // if c not printable, return
            if (!isPrintable(c)) {
                continue;
            }

            Float h = textPosition.getHeightDir();

            if (c.equals(NBSP)) { // replace non-breaking space for space
                c = " ";
            }

            float wos = textPosition.getWidthOfSpace();

            TextElement te = new TextElement(Utils.round(textPosition.getYDirAdj() - h, 2),
                    Utils.round(textPosition.getXDirAdj(), 2), Utils.round(textPosition.getWidthDirAdj(), 2),
                    Utils.round(textPosition.getHeightDir(), 2), textPosition.getFont(), textPosition.getFontSizeInPt(), c,
                    // workaround a possible bug in PDFBox:
                    // https://issues.apache.org/jira/browse/PDFBOX-1755
                    wos, textPosition.getDir());

            this.minCharWidth = (float) Math.min(this.minCharWidth, te.getWidth());
            this.minCharHeight = (float) Math.min(this.minCharHeight, te.getHeight());

            countHeight++;
            totalHeight += te.getHeight();
            float avgHeight = totalHeight / countHeight;
            
            //We have an issue where tall blank cells throw off the row height calculation
            //Introspect a blank cell a bit here to see if it should be thrown away
            if ((te.getText() == null || te.getText().trim().equals(""))) {
                //if the cell height is more than AVG_HEIGHT_MULT_THRESHOLDxaverage, throw it away
                if (avgHeight > 0
                        && te.getHeight() >= (avgHeight * AVG_HEIGHT_MULT_THRESHOLD)) {
                    continue;
                }
                
                //if the font size is outside of reasonable ranges, throw it away
                if (textPosition.getFontSizeInPt() > MAX_BLANK_FONT_SIZE || textPosition.getFontSizeInPt() < MIN_BLANK_FONT_SIZE) {
                    continue;
                }
            }
            
            this.spatialIndex.add(te);
            this.textElements.add(te);
        }
    }

  @Override
  protected float computeFontHeight(PDFont font) throws IOException
  {
    BoundingBox bbox = font.getBoundingBox();
    if (bbox.getLowerLeftY() < Short.MIN_VALUE)
    {
      // PDFBOX-2158 and PDFBOX-3130
      // files by Salmat eSolutions / ClibPDF Library
      bbox.setLowerLeftY(- (bbox.getLowerLeftY() + 65536));
    }
    // 1/2 the bbox is used as the height todo: why?
    float glyphHeight = bbox.getHeight() / 2;

    // sometimes the bbox has very high values, but CapHeight is OK
    PDFontDescriptor fontDescriptor = font.getFontDescriptor();
    if (fontDescriptor != null)
    {
      float capHeight = fontDescriptor.getCapHeight();
      if (Float.compare(capHeight, 0) != 0 &&
        (capHeight < glyphHeight || Float.compare(glyphHeight, 0) == 0))
      {
        glyphHeight = capHeight;
      }
      // PDFBOX-3464, PDFBOX-448:
      // sometimes even CapHeight has very high value, but Ascent and Descent are ok
      float ascent = fontDescriptor.getAscent();
      float descent = fontDescriptor.getDescent();
      if (ascent > 0 && descent < 0 &&
        ((ascent - descent) / 2 < glyphHeight || Float.compare(glyphHeight, 0) == 0))
      {
        glyphHeight = (ascent - descent) / 2;
      }
    }

    // transformPoint from glyph space -> text space
    float height;
    if (font instanceof PDType3Font)
    {
      height = font.getFontMatrix().transformPoint(0, glyphHeight).y;
    }
    else
    {
      height = glyphHeight / 1000;
    }

    return height;
  }

    private boolean isPrintable(String s) {
        Character c;
        Character.UnicodeBlock block;
        boolean printable = false;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            block = Character.UnicodeBlock.of(c);
            printable |= !Character.isISOControl(c) && block != null && block != Character.UnicodeBlock.SPECIALS;
        }
        return printable;
    }

    public List<TextElement> getTextElements() {
        return this.textElements;
    }

    public RectangleSpatialIndex<TextElement> getSpatialIndex() {
        return spatialIndex;
    }

    public float getMinCharWidth() {
        return minCharWidth;
    }

    public float getMinCharHeight() {
        return minCharHeight;
    }
}
