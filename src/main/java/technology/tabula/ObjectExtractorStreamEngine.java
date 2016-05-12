package technology.tabula;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

class ObjectExtractorStreamEngine extends PDFGraphicsStreamEngine {
	
	private ArrayList<Shape> clippingPaths;
	private boolean debugClippingPaths;
	private boolean extractRulingLines;
	
	public ObjectExtractorStreamEngine(PDPage page) {
		super(page);
	}

	public ObjectExtractorStreamEngine(PDDocument pdf_document) throws IOException {
        this(pdf_document, null, true, false);
    }

    public ObjectExtractorStreamEngine(PDDocument pdf_document, boolean debugClippingPaths) throws IOException {
        this(pdf_document, null, true, debugClippingPaths);
    }

    public ObjectExtractorStreamEngine(PDDocument pdf_document, String password) throws IOException {
        this(pdf_document, password, true, false);
    }

    public ObjectExtractorStreamEngine(PDDocument pdf_document, String password, boolean extractRulingLines, boolean debugClippingPaths)
            throws IOException {
    	
        this.clippingPaths = new ArrayList<Shape>();
        this.debugClippingPaths = debugClippingPaths;
        this.extractRulingLines = extractRulingLines;

        this.initialize();

        if (pdf_document.isEncrypted()) {
            try {
                pdf_document
                        .openProtection(new StandardDecryptionMaterial(password));
            } catch (BadSecurityHandlerException e) {
                // TODO Auto-generated catch block
                throw new IOException("BadSecurityHandler");
            } catch (CryptographyException e) {
                throw new IOException("Document is encrypted");
            }
        }
        this.pdf_document = pdf_document;
        this.pdf_document_pages = this.pdf_document.getDocumentCatalog()
                .getAllPages();

    }

	@Override
	public void appendRectangle(Point2D arg0, Point2D arg1, Point2D arg2,
			Point2D arg3) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clip(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closePath() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void curveTo(float arg0, float arg1, float arg2, float arg3,
			float arg4, float arg5) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawImage(PDImage arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endPath() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillAndStrokePath(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillPath(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Point2D getCurrentPoint() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lineTo(float arg0, float arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveTo(float arg0, float arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shadingFill(COSName arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void strokePath() throws IOException {
		// TODO Auto-generated method stub

	}

}
