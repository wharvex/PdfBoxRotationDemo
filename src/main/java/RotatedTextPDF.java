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
          int fontSize = 14;
          contentStream.setFont(font, fontSize);

          float pageUpperRightX = page.getMediaBox().getUpperRightX();
          float pageUpperRightY = page.getMediaBox().getUpperRightY();

          float x = 100;
          float y = 100;
          float angle = 100;

          float tX = 100;
          float tY = 100;

          int iOrigAngle = page.getRotation();

          String text =
                  "oA: " + iOrigAngle + "; " + "pX: " + x + "; pY: " + y + "; a: " + angle + "; tX: " + tX +
                      "; tY: " + tY;

          int textWidth = (int) ((font.getStringWidth(text) / 1000) * fontSize);

          x = pageUpperRightX - 10 - textWidth;
          y = pageUpperRightY - 10 - fontSize;
          tX = 0;
          tY = 0;

          if (iOrigAngle != 0) {
            if (iOrigAngle < 180) {
              angle = 10;
              tX = 10;
              tY = 10;
            } else {
              angle = 270;
              tX = 175; // increase to move up
              tY = 800; // increase to move left
            }
          }

          text =
              "oA: " + iOrigAngle + "; " + "pX: " + x + "; pY: " + y + "; a: " + angle + "; tX: " + tX +
                  "; tY: " + tY;

          setLineOfTextOnPage(contentStream, x, y, page.getRotation(), tX, tY, angle, text);
          drawRoundedRectangle(contentStream, x - 10, y - 7, textWidth, fontSize + 10, 10);

          String text2 = "Width: " + page.getMediaBox().getWidth() + ", Height: " + page.getMediaBox().getHeight();
          setLineOfTextOnPage(contentStream, pageUpperRightX / 2, pageUpperRightY / 2, 0, 0, 0, 0, text2);
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
}
