import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;

public class RotatedTextPDF {

  public static void main(String[] args) {
    File file1 = new File("rotated_270.pdf");
    processFile(file1);

    File file2 = new File("hey_portrait.pdf");
    processFile(file2);

    File file3 = new File("rotated_90.pdf");
    processFile(file3);
  }

  private static void processFile(File file) {
    try (PDDocument document = Loader.loadPDF(file)) {
      int numberOfPages = document.getNumberOfPages();
      for (int curPageNum = 0; curPageNum < numberOfPages; curPageNum++) {
        System.out.println("Page " + (curPageNum + 1));
        PDPage page = document.getPage(curPageNum);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
            PDPageContentStream.AppendMode.APPEND, true, true)) {
          PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
          int iFontSize = 14;
          contentStream.setFont(font, iFontSize);

          final float fPageUpperRightX = page.getMediaBox().getUpperRightX();
          final float fPageUpperRightY = page.getMediaBox().getUpperRightY();

          final int iOrigAngle = page.getRotation();

          final String strText = "test text";

          final int iTextWidth = (int) ((font.getStringWidth(strText) / 1000) * iFontSize);

          float fAngle = 0;
          float fTextX = fPageUpperRightX - 15 - iTextWidth;
          float fTextY = fPageUpperRightY - 10 - iFontSize;
          float fTransformTextX = 0;
          float fTransformTextY = 0;

          if (iOrigAngle == 270) {
            fAngle = iOrigAngle;
            fTransformTextX = fPageUpperRightX - fPageUpperRightY;
            fTransformTextY = fPageUpperRightX;
          } else if (iOrigAngle == 90) {
            fAngle = iOrigAngle;
            fTransformTextX = fPageUpperRightY;
            fTransformTextY = fPageUpperRightY - fPageUpperRightX;
          }

          float fRectXPadding = 10;
          float fRectYPadding = 7;

          float fRectX = fTextX - fRectXPadding;
          float fRectY = fTextY - fRectYPadding;
          float fRectWidth = iTextWidth + (fRectXPadding * 2);
          float fRectHeight = (iFontSize * 0.72f) + (fRectYPadding * 2);
          float fRectRadius = 10;

          // When iOrigAngle == 270 or 90:
          // fPageUpperRightX = 792
          // fPageUpperRightY = 612
          // fRectX = 718 (before the adjustment below)
          // fRectY = 581 (before the adjustment below)

          // When iOrigAngle == 0:
          // fPageUpperRightX = 612
          // fPageUpperRightY = 792
          // fRectX = 538
          // fRectY = 761

          final float fUpperRightXRectXDiff = fPageUpperRightX - fRectX;
          final float fUpperRightYRectYDiff = fPageUpperRightY - fRectY;

          if (iOrigAngle == 270) {
            final float fAddToRectX = fUpperRightXRectXDiff - fUpperRightYRectYDiff;
            final float fAddToRectY = fUpperRightXRectXDiff - fRectY;
            fRectX = fRectX + fAddToRectX;
            fRectY = fRectY + fAddToRectY;
          } else if (iOrigAngle == 90) {
            final float fAddToRectX = fUpperRightYRectYDiff - fRectX;
            final float fAddToRectY = fUpperRightYRectYDiff - fUpperRightXRectXDiff;
            fRectX = fRectX + fAddToRectX;
            fRectY = fRectY + fAddToRectY;
          }

          setLineOfTextOnPage(contentStream, fTextX, fTextY, page.getRotation(), fTransformTextX, fTransformTextY,
              fAngle, strText);
          if (iOrigAngle != 0) {
            drawRoundedRectangle(contentStream, fRectX, fRectY, fRectWidth, fRectHeight, fRectRadius, iOrigAngle);
          } else {
            drawRoundedRectangle(contentStream, fRectX, fRectY, fRectWidth, fRectHeight, fRectRadius);
          }

          createTopLeftStamp(contentStream, fPageUpperRightX, fPageUpperRightY, iOrigAngle);

          createPageNumberFooter(contentStream, fPageUpperRightX, fPageUpperRightY, iOrigAngle);
        }
      }
      saveUpdatedDocument(document);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void setLineOfTextOnPage(PDPageContentStream p_contentStream, float p_fLinePosX, float p_fLinePosY,
                                          int p_iOrigAngle, float p_fTranslateX, float p_fTranslateY, float p_fAngle,
                                          String p_strText)
      throws IOException {
    p_contentStream.beginText();

    if (p_iOrigAngle != 0) {
      Matrix matrix = new Matrix();
      matrix.translate(p_fTranslateX, p_fTranslateY);
      matrix.rotate(Math.toRadians(p_fAngle));
      p_contentStream.setTextMatrix(matrix);
    }

    p_contentStream.newLineAtOffset(p_fLinePosX, p_fLinePosY);
    p_contentStream.showText(p_strText);
    p_contentStream.endText();
  }

  private static void drawRoundedRectangle(PDPageContentStream contentStream, float x, float y, float width,
                                           float height, float radius) throws IOException {
    contentStream.moveTo(x + radius, y);
    contentStream.lineTo(x + width - radius, y);
    contentStream.curveTo(x + width, y, x + width, y, x + width, y + radius);
    contentStream.lineTo(x + width, y + height - radius);
    contentStream.curveTo(x + width, y + height, x + width, y + height, x + width - radius, y + height);
    contentStream.lineTo(x + radius, y + height);
    contentStream.curveTo(x, y + height, x, y + height, x, y + height - radius);
    contentStream.lineTo(x, y + radius);
    contentStream.curveTo(x, y, x, y, x + radius, y);
    contentStream.closePath();
    contentStream.stroke();
  }

  private static void drawRoundedRectangle(PDPageContentStream contentStream, float x, float y, float width,
                                           float height, float radius, float p_iOrigAngle) throws IOException {
    contentStream.saveGraphicsState();

    // Translate to rotation point, rotate, then translate back
    contentStream.transform(Matrix.getTranslateInstance(x, y));
    contentStream.transform(Matrix.getRotateInstance(Math.toRadians(p_iOrigAngle), 0, 0));
    contentStream.transform(Matrix.getTranslateInstance(-x, -y));

    drawRoundedRectangle(contentStream, x, y, width, height, radius);

    contentStream.restoreGraphicsState();
  }

  private static void saveUpdatedDocument(PDDocument document) {
    // Save the updated document as "updated_test_x.pdf" where x is the next available number
    int fileIndex = 1;
    File outputFile;
    do {
      outputFile = new File("updated_test_" + fileIndex + ".pdf");
      fileIndex++;
    } while (outputFile.exists());
    try {
      document.save(outputFile);
      System.out.println("Updated PDF saved as " + outputFile.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void createPageNumberFooter(PDPageContentStream contentStream, final float fPageUpperRightX,
                                             final float fPageUpperRightY, final int iOrigAngle) throws IOException {
    PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    int iFontSize = 14;
    contentStream.setFont(font, iFontSize);

    final String strText = "test text footer";

    final int iTextWidth = (int) ((font.getStringWidth(strText) / 1000) * iFontSize);

    float fAngle = 0;
    float fTextX = (fPageUpperRightX / 2) - ((float) iTextWidth / 2);
    float fTextY = iFontSize;
    float fTransformTextX = 0;
    float fTransformTextY = 0;

    if (iOrigAngle == 270) {
      final float fAddToTransformX = fPageUpperRightY - fPageUpperRightX;
      final float fAddToTransformY = fAddToTransformX / 2;
      fAngle = iOrigAngle;
      fTransformTextX = fPageUpperRightX - fPageUpperRightY + fAddToTransformX;
      fTransformTextY = fPageUpperRightX + fAddToTransformY; // increase to move text left
    } else if (iOrigAngle == 90) {
      fAngle = iOrigAngle;
      fTransformTextX = fPageUpperRightY;
      fTransformTextY = fPageUpperRightY - fPageUpperRightX;
    }

    setLineOfTextOnPage(contentStream, fTextX, fTextY, iOrigAngle, fTransformTextX, fTransformTextY,
        fAngle, strText);

  }

  private static void createTopLeftStamp(PDPageContentStream contentStream, final float fPageUpperRightX,
                                         final float fPageUpperRightY, final int iOrigAngle) throws IOException {
    PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    int iFontSize = 14;
    contentStream.setFont(font, iFontSize);

    final String strText = "test text in the top left of the page, making this a little longer";

    final int iTextWidth = (int) ((font.getStringWidth(strText) / 1000) * iFontSize);

    float fAngle = 0;
    float fTextX = 10;
    float fTextY = fPageUpperRightY - 10 - iFontSize;
    float fTransformTextX = 0;
    float fTransformTextY = 0;

    float fXChange = fPageUpperRightY - fPageUpperRightX;

    if (iOrigAngle == 270) {
      fAngle = iOrigAngle;
      fTransformTextX = fPageUpperRightX - fPageUpperRightY;
      fTransformTextY = fPageUpperRightX + fXChange;
    } else if (iOrigAngle == 90) {
      fAngle = iOrigAngle;
      fTransformTextX = fPageUpperRightY;
      fTransformTextY = fPageUpperRightY - fPageUpperRightX;
    }

    float fRectXPadding = 10;
    float fRectYPadding = 7;

    float fRectX = fTextX - fRectXPadding;
    float fRectY = fTextY - fRectYPadding;
    float fRectWidth = iTextWidth + (fRectXPadding * 2);
    float fRectHeight = (iFontSize * 0.72f) + (fRectYPadding * 2);
    float fRectRadius = 10;

    // When iOrigAngle == 270 or 90:
    // fPageUpperRightX = 792
    // fPageUpperRightY = 612
    // fRectX = 718 (before the adjustment below)
    // fRectY = 581 (before the adjustment below)

    // When iOrigAngle == 0:
    // fPageUpperRightX = 612
    // fPageUpperRightY = 792
    // fRectX = 538
    // fRectY = 761

    final float fUpperRightXRectXDiff = fPageUpperRightX - fRectX;
    final float fUpperRightYRectYDiff = fPageUpperRightY - fRectY;

    if (iOrigAngle == 270) {
      final float fAddToRectX = fUpperRightXRectXDiff - fUpperRightYRectYDiff;
      final float fAddToRectY = fUpperRightXRectXDiff - fRectY;
      fRectX = fRectX + fAddToRectX; // decrease to move rectangle down.
      fRectY = fRectY + fAddToRectY + fXChange; // increase to move rectangle left.
    } else if (iOrigAngle == 90) {
      final float fAddToRectX = fUpperRightYRectYDiff - fRectX;
      final float fAddToRectY = fUpperRightYRectYDiff - fUpperRightXRectXDiff;
      fRectX = fRectX + fAddToRectX;
      fRectY = fRectY + fAddToRectY;
    }

    setLineOfTextOnPage(contentStream, fTextX, fTextY, iOrigAngle, fTransformTextX, fTransformTextY,
        fAngle, strText);
    if (iOrigAngle != 0) {
      drawRoundedRectangle(contentStream, fRectX, fRectY, fRectWidth, fRectHeight, fRectRadius, iOrigAngle);
    } else {
      drawRoundedRectangle(contentStream, fRectX, fRectY, fRectWidth, fRectHeight, fRectRadius);
    }

  }
}
