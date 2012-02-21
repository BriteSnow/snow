/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.util.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class ImageProcessor {
    static private Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
    
    public static int BUFFER_SIZE = 2048 * 2;

    public void resizeImage(File srcImageFile, File dstImageFile, int width, int height) throws Exception {
        FileInputStream imageInput = new FileInputStream(srcImageFile);
        InputStream newImageInput = scaleImageWithMultisteps(imageInput, width, height);

        FileOutputStream fos = new FileOutputStream(dstImageFile);
        BufferedInputStream newImageBis = new BufferedInputStream(newImageInput);

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = buffer.length;
            while (true) {
                len = newImageBis.read(buffer);
                if (len == -1)
                    break;
                fos.write(buffer, 0, len);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            fos.close();
            newImageBis.close();
        }

    }

    private InputStream scaleImageWithMultisteps(InputStream imageInput, int width, int height) throws Exception {

        InputStream imageStream = new BufferedInputStream(imageInput);
        Image image = (Image) ImageIO.read(imageStream);

        int thumbWidth = width;
        int thumbHeight = height;

        // Make sure the aspect ratio is maintained, so the image is not skewed
        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }

        //create the original BufferedImage
        BufferedImage imageBi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageG = imageBi.createGraphics();
        imageG.drawImage(image, 0, 0, imageWidth, imageHeight, null);

        BufferedImage thumbImage = getScaledInstance(imageBi, thumbWidth, thumbHeight,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
        /* --------- //Blur the original image for better scaling --------- */

        // Write the scaled image to the outputstream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //PNGImageWriter pngWriter = new PNGImageWriter()

        ImageIO.write(thumbImage, "PNG", out);

        // Read the outputstream into the inputstream for the return value
        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

        imageG.dispose();

        return bis;
    }

    /**
     * Convenience method that returns a scaled instance of the provided {@code
     * BufferedImage}.
     * 
     * @param img
     *            the original image to be scaled
     * @param targetWidth
     *            the desired width of the scaled instance, in pixels
     * @param targetHeight
     *            the desired height of the scaled instance, in pixels
     * @param hint
     *            one of the rendering hints that corresponds to {@code
     *            RenderingHints.KEY_INTERPOLATION} (e.g. {@code
     *            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR}, {@code
     *            RenderingHints.VALUE_INTERPOLATION_BILINEAR}, {@code
     *            RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality
     *            if true, this method will use a multi-step scaling technique
     *            that provides higher quality than the usual one-step technique
     *            (only useful in downscaling cases, where {@code targetWidth}
     *            or {@code targetHeight} is smaller than the original
     *            dimensions, and generally only when the {@code BILINEAR} hint
     *            is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    private static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint,
                                                   boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    /*
    private InputStream scaleImageWithBlurTrick(InputStream imageInput, int width, int height) throws Exception {

        InputStream imageStream = new BufferedInputStream(imageInput);
        Image image = (Image) ImageIO.read(imageStream);

        int thumbWidth = width;
        int thumbHeight = height;

        // Make sure the aspect ratio is maintained, so the image is not skewed
        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }
        

        //This is not perfect, but better than not doing it.
        BufferedImage bi = new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageG = bi.createGraphics();
        imageG.drawImage(image, 0, 0, imageWidth, imageHeight,null);
        bi = blurImage(bi);

        BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D thumbG = thumbImage.createGraphics();
        thumbG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        thumbG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        thumbG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        thumbG.drawImage(bi, 0, 0, thumbWidth, thumbHeight, null);
        
        // Write the scaled image to the outputstream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //PNGImageWriter pngWriter = new PNGImageWriter()

        
          JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
          JPEGEncodeParam param = encoder.
          getDefaultJPEGEncodeParam(thumbImage); int quality = 100; // Use
          between 1 and 100, with 100 being highest quality quality =
          Math.max(0, Math.min(quality, 100)); param.setQuality((float)quality
          / 100.0f, false); encoder.setJPEGEncodeParam(param);
          encoder.encode(thumbImage); ImageIO.write(thumbImage, "JPG" , out);
         

        ImageIO.write(thumbImage, "PNG", out);

        // Read the outputstream into the inputstream for the return value
        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

        imageG.dispose();
        thumbG.dispose();

        return bis;
    }
    */
    
    /*
    @SuppressWarnings("unchecked")
    private static BufferedImage blurImage(BufferedImage image) {
        float ninth = 1.0f / 9.0f;
        float[] blurKernel = { ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth };

        Map map = new HashMap();

        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RenderingHints hints = new RenderingHints(map);
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
        return op.filter(image, null);
    }
    */
}
