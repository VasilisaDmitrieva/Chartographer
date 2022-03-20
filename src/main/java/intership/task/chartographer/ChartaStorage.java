package intership.task.chartographer;

import intership.task.chartographer.domain.Charta;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ChartaStorage {
    private final File storage;
    private final Charta charta;
    private static final int stripeHeight = 100;
    private BufferedImage data;
    private int currentStripe;

    public ChartaStorage(Charta charta, File storage) {
        this.charta = charta;
        this.storage = storage;
        this.currentStripe = -1;
    }

    private void openStripe(int ind, boolean create) throws IOException {
        currentStripe = ind;
        File stripe = new File(storage, Integer.toString(ind));
        if (stripe.isFile()) {
            try (InputStream inputStream = new FileInputStream(stripe)) {
                data = ImageIO.read(inputStream);
            }
        } else {
            if (create) {
                if (!stripe.createNewFile()) {
                    throw new IOException(stripe.getPath() + " must be a file.");
                }
                data = new BufferedImage(charta.getWidth(), stripeHeight, BufferedImage.TYPE_INT_RGB);
            } else {
                data = null;
            }
        }
    }

    public void writePixel(int x, int y, int rgb) throws IOException {
        if (invalidCoordinates(x, y)) {
            return;
        }
        if (y / stripeHeight != currentStripe) {
            if (currentStripe != -1) {
                closeStripe();
            }
            openStripe(y / stripeHeight, true);
        }

        data.setRGB(x, y % stripeHeight, rgb);
    }

    public int readPixel(int x, int y) throws IOException {
        if (invalidCoordinates(x, y)) {
            return 0;
        }
        if (y / stripeHeight != currentStripe) {
            openStripe(y / stripeHeight, false);
        }
        // no such stripe
        if (data == null) {
            return 0;
        }

        return data.getRGB(x, y % stripeHeight);
    }

    private boolean invalidCoordinates(int x, int y) {
        return x < 0 || y < 0 || x >= charta.getWidth() || y >= charta.getHeight();
    }
    private void closeStripe() throws IOException {
        File stripe = new File(storage, Integer.toString(currentStripe));
        try (FileOutputStream outputStream = new FileOutputStream(stripe)) {
            ImageIO.write(data, "bmp", outputStream);
        }
        currentStripe = -1;
    }

    public void close() throws IOException {
        closeStripe();
    }
}
