import org.junit.Test;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestBarcodeImages {
   static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

      final int frameSize = width * height;

      for (int j = 0, yp = 0; j < height; j++) {
         int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
         for (int i = 0; i < width; i++, yp++) {
            int y = (0xff & ((int) yuv420sp[yp])) - 16;
            if (y < 0)
               y = 0;
            if ((i & 1) == 0) {
               v = (0xff & yuv420sp[uvp++]) - 128;
               u = (0xff & yuv420sp[uvp++]) - 128;
            }

            int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v);
            int g = (y1192 - 833 * v - 400 * u);
            int b = (y1192 + 2066 * u);

            if (r < 0) r = 0;
            else if (r > 262143)
               r = 262143;
            if (g < 0) g = 0;
            else if (g > 262143)
               g = 262143;
            if (b < 0) b = 0;
            else if (b > 262143)
               b = 262143;

            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
         }
      }
   }

   /**
    * returns the size of a data buffer to hold an NV21-encoded image of the specified dimensions
    * http://www.codecodex.com/wiki/Decode_NV21-Encoded_Image_Data
    * @param Width
    * @param Height
    *
    * @return
    */
   public static int sizeofNV21(int Width, int Height)  {
      return
         Width * Height + ((Width + 1) / 2) * ((Height + 1) / 2) * 2;
   }

   /**
    * decodes NV21-encoded image data, which is the default android camera preview image format.
    *
    * @param SrcWidth  - with of image before rotation
    * @param SrcHeight - height of image before rotation
    * @param Data      - length = NV21DataSize(SrcWidth, SrcHeight)
    * @param Rotate - [0 .. 3], angle is 90° * Rotate clockwise
    * @param Alpha - set as alpha for all decoded pixels
    * @param Pixels - length = Width * Height of ARGB colour values
    */
   public static void decodeNV21(int SrcWidth, int SrcHeight, byte[] Data, int Rotate, int Alpha, int[] Pixels) {
      final int AlphaMask = Alpha << 24;
        /* Rotation involves accessing either the source or destination pixels in a
          non-sequential fashion. Since the source is smaller, I figure it's less
          cache-unfriendly to go jumping around that. */
      final int DstWidth = (Rotate & 1) != 0 ? SrcHeight : SrcWidth;
      final int DstHeight = (Rotate & 1) != 0 ? SrcWidth : SrcHeight;
      final boolean DecrementRow = Rotate > 1;
      final boolean DecrementCol = Rotate == 1 || Rotate == 2;
      final int LumaRowStride = (Rotate & 1) != 0 ? 1 : SrcWidth;
      final int LumaColStride = (Rotate & 1) != 0 ? SrcWidth : 1;
      final int ChromaRowStride = (Rotate & 1) != 0 ? 2 : SrcWidth;
      final int ChromaColStride = (Rotate & 1) != 0 ? SrcWidth : 2;
      int dst = 0;
      for (int row = DecrementRow ? DstHeight : 0; ; ) {
         if (row == (DecrementRow ? 0 : DstHeight))
            break;
         if (DecrementRow) {
            --row;
         } /*if*/
         for (int col = DecrementCol ? DstWidth : 0; ; ) {
            if (col == (DecrementCol ? 0 : DstWidth))
               break;
            if (DecrementCol) {
               --col;
            } /*if*/
            final int Y = 0xff & (int) Data[row * LumaRowStride + col * LumaColStride]; /* [0 .. 255] */
                /* U/V data follows entire luminance block, downsampled to half luminance
                  resolution both horizontally and vertically */
                /* decoding follows algorithm shown at
                  <http://www.mail-archive.com/android-developers@googlegroups.com/msg14558.html>,
                  except it gets red and blue the wrong way round (decoding NV12 rather than NV21) */
                /* see also good overview of YUV-family formats at <http://wiki.videolan.org/YUV> */
            final int Cr =
               (0xff & (int) Data[SrcHeight * SrcWidth + row / 2 * ChromaRowStride + col / 2 * ChromaColStride]) - 128;
                        /* [-128 .. +127] */
            final int Cb =
               (0xff & (int) Data[SrcHeight * SrcWidth + row / 2 * ChromaRowStride + col / 2 * ChromaColStride + 1]) - 128;
                        /* [-128 .. +127] */
            Pixels[dst++] =
               AlphaMask
                  |
                  Math.max
                     (
                        Math.min
                           (
                              (int) (
                                 Y
                                    +
                                    Cr
                                    +
                                    (Cr >> 1)
                                    +
                                    (Cr >> 2)
                                    +
                                    (Cr >> 6)
                              ),
                              255
                           ),
                        0
                     )
                     <<
                     16 /* red */
                  |
                  Math.max
                     (
                        Math.min
                           (
                              (int) (
                                 Y
                                    -
                                    (Cr >> 2)
                                    +
                                    (Cr >> 4)
                                    +
                                    (Cr >> 5)
                                    -
                                    (Cb >> 1)
                                    +
                                    (Cb >> 3)
                                    +
                                    (Cb >> 4)
                                    +
                                    (Cb >> 5)
                              ),
                              255
                           ),
                        0
                     )
                     <<
                     8 /* green */
                  |
                  Math.max
                     (
                        Math.min
                           (
                              (int) (
                                 Y
                                    +
                                    Cb
                                    +
                                    (Cb >> 2)
                                    +
                                    (Cb >> 3)
                                    +
                                    (Cb >> 5)
                              ),
                              255
                           ),
                        0
                     ); /* blue */
            if (!DecrementCol) {
               ++col;
            } /*if*/
         } /*for*/
         if (!DecrementRow) {
            ++row;
         } /*if*/
      } /*for*/
   }

   @Test
   public void testImageIO() {
      System.out.printf("ImageIO.getReaderFormatNames: %s\n", Arrays.asList(ImageIO.getReaderFormatNames()));
      System.out.printf("ImageIO.getReaderMIMETypes: %s\n", Arrays.asList(ImageIO.getReaderMIMETypes()));
      System.out.printf("ImageIO.getReaderFileSuffixes: %s\n", Arrays.asList(ImageIO.getReaderFileSuffixes()));
      System.out.printf("ImageIO.getWriterFormatNames: %s\n", Arrays.asList(ImageIO.getWriterFormatNames()));
      System.out.printf("ImageIO.getWriterMIMETypes: %s\n", Arrays.asList(ImageIO.getWriterMIMETypes()));
      System.out.printf("ImageIO.getWriterMIMETypes: %s\n", Arrays.asList(ImageIO.getWriterMIMETypes()));

   }

   @Test
   public void testDecodeNV21() throws Exception {
      String inputImage = "/tmp/image";
      FileInputStream fis = new FileInputStream(inputImage);
      int size = fis.available();
      byte[] androidData = new byte[size];
      int read = fis.read(androidData);
      System.out.printf("Read %d bytes from %s\n", read, inputImage);

      int width = 480;
      int height = 640;
      int[] argb = new int[width*height];

      System.out.printf("NV21 to ARGB has size: %d\n", argb.length);
      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
      WritableRaster raster = (WritableRaster) image.getData();
      raster.setPixels(0, 0, width, height, argb);
      ImageIO.write(image, "PNG", new File("/tmp/testDecodeNV21.png"));
   }

   @Test
   public void testRGB_565() throws Exception {
      String inputImage = "/tmp/image565";
      FileInputStream fis = new FileInputStream(inputImage);
      int size = fis.available();
      byte[] androidData = new byte[size];
      int read = fis.read(androidData);
      System.out.printf("Read %d bytes from %s\n", read, inputImage);

      int width = 480;
      int height = 640;
      int rgb[] = new int[androidData.length];
      for(int n = 0; n < androidData.length; n ++)
         rgb[n] = androidData[n];

      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
      WritableRaster raster = (WritableRaster) image.getData();
      raster.setPixels(0, 0, width, height, rgb);
      ImageIO.write(image, "PNG", new File("/tmp/testRGB_565.png"));
   }
   @Test
   public void testRGB4() throws Exception {
      String inputImage = "/tmp/image.rgb4";
      FileInputStream fis = new FileInputStream(inputImage);
      int size = fis.available();
      byte[] androidData = new byte[size];
      int read = fis.read(androidData);
      System.out.printf("Read %d bytes from %s\n", read, inputImage);

      int width = 480;
      int height = 640;
      int rgb[] = new int[androidData.length];
      for(int n = 0; n < androidData.length; n ++)
         rgb[n] = androidData[n];

      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
      WritableRaster raster = (WritableRaster) image.getData();
      raster.setPixels(0, 0, width, height, rgb);
      ImageIO.write(image, "PNG", new File("/tmp/testRGB4.png"));
      ImageIcon icon = new ImageIcon(image);
      JFrame frame = new JFrame();
      frame.getContentPane().setLayout(new FlowLayout());
      frame.getContentPane().add(new JLabel(icon));
      frame.pack();
      frame.setVisible(true);
      Thread.sleep(10*1000);
   }
   @Test
   public void testReadRGB_565() throws Exception {
      File inputImage = new File("/tmp/image565");
      BufferedImage bufImg = ImageIO.read(inputImage);
      ImageIcon icon = new ImageIcon(bufImg);
      JFrame frame = new JFrame();
      frame.getContentPane().setLayout(new FlowLayout());
      frame.getContentPane().add(new JLabel(icon));
      frame.pack();
      frame.setVisible(true);
   }
   @Test
   public void testReadRGB4() throws Exception {
      File inputImage = new File("/tmp/image.rgb4");
      BufferedImage bufImg = ImageIO.read(inputImage);
      ImageIcon icon = new ImageIcon(bufImg);
      JFrame frame = new JFrame();
      frame.getContentPane().setLayout(new FlowLayout());
      frame.getContentPane().add(new JLabel(icon));
      frame.pack();
      frame.setVisible(true);
   }
   @Test
   public void testCameraImageAsPNG() throws Exception {
      String inputImage = "/tmp/image";
      FileInputStream fis = new FileInputStream(inputImage);
      int size = fis.available();
      byte[] androidData = new byte[size];
      int read = fis.read(androidData);
      System.out.printf("Read %d bytes from %s\n", read, inputImage);

      int rgb[] = new int[640 * 480];
      decodeYUV420SP(rgb, androidData, 480, 640);
      int width = 483; // Dimensions of the image
      int height = 483;
      // Create a Buffered Image.
      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      // We need its raster to set the pixels' values.
      WritableRaster raster = image.getRaster();
      // Put the pixels on the raster, choosing a color depending on its coordinates.
      int pixel = 0;
      for (int h = 0; h < height; h++)
         for (int w = 0; w < width; w++) {
            int color = rgb[pixel++];
            int r = color >> 6 & 0xff0000;
            int b = color >> 2 & 0xff00;
            int g = color & 0x0ff;
            int[] rgbColor = {r, g, b};
            raster.setPixel(w, h, rgbColor);
         }

      // Store the image using the PNG format.
      ImageIO.write(image, "PNG", new File("/tmp/testCameraImageAsPNG.png"));
   }
}
