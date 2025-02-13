package com.github.captcha4j.core.image.gimpy;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Overlays a warped grid to the image.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:subhajitdas298@gmail.com">Subhajit Das</a>
 */
public class FishEyeGimpyRenderer implements GimpyRenderer {

    /**
     * The horizontal color
     */
    private final Color _hColor;
    /**
     * The vertical color
     */
    private final Color _vColor;

    /**
     * Default constructor
     */
    public FishEyeGimpyRenderer() {
        this(Color.BLACK, Color.BLACK);
    }

    /**
     * Constructor with customized horizontal color and vertical color
     *
     * @param hColor the horizontal color
     * @param vColor the vertical color
     */
    public FishEyeGimpyRenderer(Color hColor, Color vColor) {
        _hColor = hColor;
        _vColor = vColor;
    }

    /**
     * Applies the rendering to the image
     *
     * @param image the image
     */
    @Override
    public void gimp(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();

        int hstripes = height / 7;
        int vstripes = width / 7;

        // Calculate space between lines
        int hspace = height / (hstripes + 1);
        int vspace = width / (vstripes + 1);

        Graphics2D graph = (Graphics2D) image.getGraphics();
        // Draw the horizontal stripes
        for (int i = hspace; i < height; i = i + hspace) {
            graph.setColor(_hColor);
            graph.drawLine(0, i, width, i);
        }

        // Draw the vertical stripes
        for (int i = vspace; i < width; i = i + vspace) {
            graph.setColor(_vColor);
            graph.drawLine(i, 0, i, height);
        }

        // Create a pixel array of the original image.
        // we need this later to do the operations on..
        int[] pix = new int[height * width];
        int j = 0;

        for (int j1 = 0; j1 < width; j1++) {
            for (int k1 = 0; k1 < height; k1++) {
                pix[j] = image.getRGB(j1, k1);
                j++;
            }
        }

        double distance = ranInt(width / 4, width / 3);

        // put the distortion in the (dead) middle
        int wMid = image.getWidth() / 2;
        int hMid = image.getHeight() / 2;

        // again iterate over all pixels..
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {

                int relX = x - wMid;
                int relY = y - hMid;

                double d1 = Math.sqrt(relX * relX + relY * relY);
                if (d1 < distance) {

                    int j2 = wMid
                            + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (x - wMid));
                    int k2 = hMid
                            + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (y - hMid));
                    image.setRGB(x, y, pix[j2 * height + k2]);
                }
            }
        }

        graph.dispose();
    }

    /**
     * Gets a random integer between start and end
     *
     * @param start the starting integer
     * @param end   the ending integer
     * @return the random integer
     */
    private int ranInt(int start, int end) {
        double d = Math.random();
        return Double.valueOf(start + ((end - start) + 1) * d).intValue();
    }

    /**
     * Calculates the fish eye formulae
     *
     * @param s the fish eye parameter
     * @return calculated value
     */
    private double fishEyeFormula(double s) {
        // implementation of:
        // g(s) = - (3/4)s3 + (3/2)s2 + (1/4)s, with s from 0 to 1.
        if (s < 0.0D) {
            return 0.0D;
        }
        if (s > 1.0D) {
            return s;
        }

        return -0.75D * s * s * s + 1.5D * s * s + 0.25D * s;
    }

}
