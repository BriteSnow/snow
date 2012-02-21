package com.britesnow.snow.test.util.image;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.britesnow.snow.util.FileUtil;
import com.britesnow.snow.util.image.ImageProcessor;

public class ImageProcessorTest {
    private static String   TEST_IMAGE_FOLDER = "src/test/resources/util.image/";
    private static String[] TEST_IMAGES       = { "test-image-1.png", "test-image-2.jpg" };

    @Test
    public void testImageResize() {
        try {
            ////init
            ImageProcessor ip = new ImageProcessor();
            File imageFolder = new File(TEST_IMAGE_FOLDER);

            for (String testImageName : TEST_IMAGES) {
                File srcImageFile = new File(imageFolder, testImageName);
                System.out.println("path: " +srcImageFile.getAbsolutePath());
                File dstImageFile = new File(srcImageFile.getParentFile() + "/gen_"
                                        + FileUtil.getFileNameAndExtension(testImageName)[0] + ".png");

                ip.resizeImage(srcImageFile, dstImageFile, 50, 50);
                assertTrue("generated image did not get created", dstImageFile.exists());
            }

            ////resizing and testing

            ////cleanup
            for (File file : imageFolder.listFiles()) {
                if (file.getName().startsWith("gen_")) {
                    file.delete();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
