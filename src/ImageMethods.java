import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageMethods {

    private static int[][] gradientAngle;

    private static int[][] sobelX = new int[][]{
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
    };
    private static int[][] sobelY = new int[][]{
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };


    public static BufferedImage greyImage(BufferedImage image) throws IOException {
        try {

            int width = image.getWidth();
            int height = image.getHeight();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Color c = new Color(image.getRGB(i, j));

                    int red = (int) (c.getRed() * 0.2126);
                    int green = (int) (c.getGreen() * 0.7152);
                    int blue = (int) (c.getBlue() * 0.0722);

                    Color newColor = new Color(red + green + blue,
                            red + green + blue, red + green + blue);
                    image.setRGB(i, j, newColor.getRGB());
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return image;
    }


    public static double[][] gaussianKernel() {
        double sigma = 2;
        int W = 3;
        double[][] kernel = new double[W][W];

        double mean = W / 2;
        double sum = 0.0;

        for (int x = 0; x < W; ++x) {
            for (int y = 0; y < W; ++y) {

                kernel[x][y] = Math.exp(-0.5 * (Math.pow((x - mean) / sigma, 2.0) + Math.pow((y - mean) / sigma, 2.0)))
                        / (2 * Math.PI * sigma * sigma);

                sum += kernel[x][y];
            }
        }

        // Normalize the kernel
        for (int x = 0; x < W; ++x)
            for (int y = 0; y < W; ++y)
                kernel[x][y] /= sum;

        return kernel;
    }


    static BufferedImage gaussianSmoothing(BufferedImage img) {

        double[][] kernel = gaussianKernel(); // create kernel method
        double[] filter = {-1, 0, 1};
        kernel = GausX(kernel, filter);



        int height = img.getHeight();
        int width = img.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double r = 0;
                double g = 0;
                double b = 0;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {

                        if ((y == 0 && x == 0 && (i == 0 || j == 0)) || (x == 0 && y == (height - 1) && (i == 2 || j == 0)) ||
                                (y == 0 && x == (width - 1) && (i == 0 || j == 2)) ||
                                (y == (height - 1) && x == (width - 1) && (i == 2 || j == 2)) ||
                                (y == 0 && x != 0 && x != (width - 1) && i == 0) || (x == 0 && y != 0 && y != (height - 1) && j == 0)
                                || (y == (height - 1) && x != 0 && x != (width - 1) && i == 2) ||
                                (x == (width - 1) && y != 0 && y != (height - 1) && j == 2)) {
                            ;
                        } else {

                            Color c = new Color(img.getRGB(x + (j - 1), y + (i - 1)));
                            r += c.getRed() * kernel[i][j];
                            g += c.getGreen() * kernel[i][j];
                            b += c.getBlue() * kernel[i][j];
                        }
                    }
                }

                img.setRGB(x, y, new Color((int) r, (int) g, (int) b).getRGB());
            }
        }
        return img;
    }


    static BufferedImage thresholding(BufferedImage img) {

        int T2 = 40;
        int T1 = 20;

        // set up white color
        int white = new Color(255, 255, 255).getRGB();
        // set up black color
        int black = new Color(0, 0, 0).getRGB();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color color = new Color(img.getRGB(x, y));

                if (color.getRed() < T1) {
                    img.setRGB(x, y, black);
                } else if (color.getRed() >= T2) {
                    img.setRGB(x, y, white);
                } else {
                    if (y >= 1 && y <= img.getHeight() - 2 && x >= 1 && x <= img.getWidth() - 2) {
                        boolean hasQualifiedNeighbor = false;

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                if (x >= 0 && x < gradientAngle.length && x + i >= 0 && x + i < gradientAngle.length
                                        && y >= 0 && y < gradientAngle[0].length && y + j >= 0 && y + j < gradientAngle[0].length) {
                                    //
                                    if (new Color(img.getRGB(x + i, y + j)).getRed() > T2
                                            && Math.abs(gradientAngle[x][y] - gradientAngle[x + i][y + j]) <= 45)
                                        hasQualifiedNeighbor = true;
                                }

                            }
                        }
                        if (hasQualifiedNeighbor) img.setRGB(x, y, white);
                        else img.setRGB(x, y, black);
                    }
                }
            }
        }
        return img;
    }

    private static void nonMaximaSuppression(BufferedImage img, int[][] sobelX_after, int[][] sobelY_after) {
        int height = img.getHeight();
        int width = img.getWidth();
        // get the gradient angle
        gradientAngle = new int[height][width];
        int[][] averageAngle = new int[height][width];
        // calculate the gradient angle
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x >= 4 && x <= width - 5 && y >= 4 && y <= height - 5) {
                    gradientAngle[y][x] = (int) ((180 / Math.PI) * Math.atan2((sobelY_after[y][x]), (sobelX_after[y][x])));
                }
            }
        }
        // calculate the average angle sector
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x >= 4 && x <= width - 5 && y >= 4 && y <= height - 5) {
                    if ((gradientAngle[y][x] >= -22.5 && gradientAngle[y][x] < 22.5)
                            || gradientAngle[y][x] >= 157.5 || gradientAngle[y][x] < -157.5) {
                        averageAngle[y][x] = 0;
                    } else if ((gradientAngle[y][x] >= 22.5 && gradientAngle[y][x] < 67.5) ||
                            (gradientAngle[y][x] >= -157.5 && gradientAngle[y][x] < -112.5)) {
                        averageAngle[y][x] = 45;
                    } else if ((gradientAngle[y][x] >= 67.5 && gradientAngle[y][x] < 112.5) ||
                            (gradientAngle[y][x] >= -112.5 && gradientAngle[y][x] < -67.5)) {
                        averageAngle[y][x] = 90;
                    } else if ((gradientAngle[y][x] >= 112.5 && gradientAngle[y][x] < 157.5) ||
                            (gradientAngle[y][x] >= -67.5 && gradientAngle[y][x] < -22.5)) {
                        averageAngle[y][x] = 135;
                    }
                }
            }
        }


        // apply non-maxima suppression
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x >= 5 && x <= width - 6 && y >= 5 && y <= height - 6) {
                    // sector 0 - compare center pixel with left and right pixel
                    if (averageAngle[y][x] == 0) {
                        if (((x - 1) >= 0 && img.getRGB(x, y) < img.getRGB(x - 1, y)) ||
                                ((x + 1) <= (width - 1) && img.getRGB(x, y) < img.getRGB(x + 1, y))) {
                            img.setRGB(x, y, 0);
                        }
                    }
                    // sector 1 - compare center pixel with up right and down left pixel
                    else if (averageAngle[y][x] == 45) {
                        if (((x - 1) >= 0 && (y - 1) >= 0 && img.getRGB(x, y) < img.getRGB(x - 1, y - 1)) ||
                                ((x + 1) <= (height - 1) && (y + 1) <= (height - 1) &&
                                        img.getRGB(x, y) < img.getRGB(x + 1, y + 1))) {
                            img.setRGB(x, y, 0);
                        }
                    }
                    // sector 2 - compare center pixel with up and down pixel
                    else if (averageAngle[y][x] == 90) {
                        if (((y - 1) >= 0 && img.getRGB(x, y) < img.getRGB(x, y - 1)) ||
                                ((y + 1) <= (height - 1) && img.getRGB(x, y) < img.getRGB(x, y + 1))) {
                            img.setRGB(x, y, 0);
                        }
                    }
                    // sector 3 - compare center pixel with up left and down right pixel
                    else if (averageAngle[y][x] == 135) {
                        if (((x - 1) >= 0 && (y + 1) <= (height - 1) && img.getRGB(x, y) < img.getRGB(x - 1, y + 1)) ||
                                ((x + 1) <= (width - 1) && (y - 1) >= 0 && img.getRGB(x, y) < img.getRGB(x + 1, y - 1))) {
                            img.setRGB(x, y, 0);
                        }
                    }
                } else img.setRGB(x, y, 0);
            }
        }


    }

    static BufferedImage getGredients(BufferedImage img) {
        // use sobel operators to create two new image
        int[][] sobelX_after = getSobel(img, sobelX);
        int[][] sobelY_after = getSobel(img, sobelY);
        // calculate the magnitude
        getMagnitude(img, sobelX_after, sobelY_after);
        // apply non-maximum suppression
        nonMaximaSuppression(img, sobelX_after, sobelY_after);
        return img;
    }

    private static void getMagnitude(BufferedImage img, int[][] sobel_x, int[][] sobel_y) {
        int height = img.getHeight();
        int width = img.getWidth();
        int[][] matrix = new int[height][width];
        int max = Integer.MIN_VALUE;

        // get all magnitude values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int magnitude = (int) Math.sqrt(Math.pow(sobel_x[y][x], 2) + Math.pow(sobel_y[y][x], 2));
                matrix[y][x] = magnitude;
                if (magnitude > max) max = magnitude;
                //img.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        double normalization = max / 255.0;
        // normalize and draw the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = (int) (matrix[y][x] / normalization);
                img.setRGB(x, y, new Color(value, value, value).getRGB());
            }
        }
    }

    private static int[][] getSobel(BufferedImage img, int[][] sobel) {
        int height = img.getHeight();
        int width = img.getWidth();

        int[][] new_sobel = new int[height][width];
        int[][] res_sobel = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = 0;
                if (x >= 4 && x <= width - 5 && y >= 4 && y <= height - 5) {
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {

                            color += new Color(img.getRGB(x + j, y + i)).getRed() * sobel[i + 1][j + 1];
                        }
                    }
                }
                // take absolute value
                new_sobel[y][x] = Math.abs(color);
            }
        }
        // find max value in the matrix
        int max = Integer.MIN_VALUE;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (new_sobel[y][x] > max) max = new_sobel[y][x];
            }
        }

        double normalization = max / 255.0;
        // draw the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = (int) (new_sobel[y][x] / normalization);
                //img.setRGB(x, y, new Color(value, value, value).getRGB());
                res_sobel[y][x] = value;
            }
        }
        return res_sobel;
    }


    public static double[][] GausX(double[][] kernel, double[] filter) {
        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel.length; j++) {
                kernel[i][j] *= filter[j];
            }
        }
        return kernel;
    }

    public static double[][] GausY(double[][] kernel, double[] filter) {
        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel.length; j++) {
                kernel[i][j] *= filter[i];
            }
        }
        return kernel;
    }


}
