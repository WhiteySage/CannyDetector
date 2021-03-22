import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class EdgeDetector {
    private static final File input = new File("test.jpg");


    public static void main(String[] args) throws IOException {

        // read the image
        BufferedImage image = ImageIO.read(input);

        //GreyScale
        image = ImageMethods.greyImage(image);
        File grey = new File("grey.jpg");
        ImageIO.write(image, "jpg", grey);

        // apply Gaussian filter
        image = ImageMethods.gaussianSmoothing(image);
        File gaus = new File("gaus.jpg");
        ImageIO.write(image, "jpg", gaus);

        // gradient operations - including non-maxima suppression
        image = ImageMethods.getGredients(image);
        File suppression = new File("suppression.jpg");
        ImageIO.write(image, "jpg", suppression);

        // thresholding
        image = ImageMethods.thresholding(image);
        File thresh = new File("thresh.jpg");
        ImageIO.write(image, "jpg", thresh);

        System.out.println("Done");

    }



}