package intership.task.chartographer;

import intership.task.chartographer.domain.Charta;
import intership.task.chartographer.domain.Fragment;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;

@Repository
public class ChartaRepository {
    private final File storage;
    private final String prefix = "charta";
    private final String splitBy = "_";

    public ChartaRepository(ApplicationArguments args) {
        if (args.getSourceArgs().length != 1) {
            throw new IllegalArgumentException("Usage: java -jar chartographer-1.0.0.jar /path/to/content/folder");
        }
        storage = new File(args.getSourceArgs()[0]);
        if (!storage.isDirectory()) {
            throw new IllegalArgumentException("No such directory: " + args.getSourceArgs()[0]);
        }
    }

    public boolean save(Charta charta) {
        long id = Arrays.stream(Objects.requireNonNull(storage.list())).mapToLong(str -> {
            if (str.startsWith(prefix)) {
                return Long.parseLong(str.split(splitBy)[1]);
            }
            return 0L;
        }).max().orElse(0L);
        id++;
        charta.setId(id);
        File file = new File(storage, setStringByCharta(charta));

        return file.mkdir();
    }

    private String setStringByCharta(Charta charta) {
        return prefix + splitBy + charta.getId() + splitBy + charta.getHeight() + splitBy + charta.getWidth();
    }

    private Charta getChartaFromString(String str) {
        String[] strings = str.split(splitBy);
        if (strings.length != 4) {
            return null;
        }
        return new Charta(Long.parseLong(strings[1]), Integer.parseInt(strings[3]), Integer.parseInt(strings[2]));
    }

    public Charta find(long id) {
        String startsWith = prefix + splitBy + id;
        return getChartaFromString(Arrays.stream(Objects.requireNonNull(storage.list())).
                filter(str -> str.startsWith(startsWith)).max(String::compareTo).orElse(""));
    }

    public boolean setFragment(Charta charta, Fragment fragment, InputStream inputStream) {
        DataInputStream is = new DataInputStream(inputStream);
        try {
            // BMP info
            int bmp = is.readShort();
            bmp = (bmp & 0xFF) << 8 | (bmp & 0xFF00) >>> 8;
            if (bmp != 0x4D42) {
                throw new IllegalArgumentException("Wrong image format.");
            }
            is.skipBytes(8);
            int offBits = Integer.reverseBytes(is.readInt());

            is.skipBytes(4);
            int width = Integer.reverseBytes(is.readInt());
            int height = Integer.reverseBytes(is.readInt());

            if (width != fragment.getWidth() || Math.abs(height) != fragment.getHeight()) {
                throw new IllegalArgumentException("Wrong image size.");
            }

            is.skipBytes(offBits - 14 - 12);

            //Fragment saving
            ChartaStorage chartaStorage = new ChartaStorage(charta, new File(storage, setStringByCharta(charta)));
            int bytesPerPixel = 3;
            byte[] row = new byte[(fragment.getWidth() * bytesPerPixel + 3) / 4 * 4];

            int y, end, inc;
            if (height < 0) {
                y = 0;
                end = fragment.getHeight();
                inc = 1;
            } else {
                y = fragment.getHeight() - 1;
                end = -1;
                inc = -1;
            }

            for (; y != end; y += inc) {
                is.readFully(row);
                if (y + fragment.getY() < charta.getHeight()) {
                    for (int x = 0; x < fragment.getWidth() && x + fragment.getX() < charta.getWidth(); x++) {
                        int color =
                                (row[x * bytesPerPixel + 2] & 0xFF) << 16
                                        | (row[x * bytesPerPixel + 1] & 0xFF) << 8
                                        | (row[x * bytesPerPixel] & 0xFF);
                        chartaStorage.writePixel(x + fragment.getX(), y + fragment.getY(), color);
                    }
                }
            }
            chartaStorage.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public byte[] getFragment(Charta charta, Fragment fragment) {
        BufferedImage image = new BufferedImage(fragment.getWidth(), fragment.getHeight(), BufferedImage.TYPE_INT_RGB);
        ChartaStorage chartaStorage = new ChartaStorage(charta, new File(storage, setStringByCharta(charta)));
        byte[] data = null;

        try {
            for (int y = fragment.getY(); y < fragment.getHeight() && y < charta.getHeight(); y++) {
                for (int x = fragment.getX(); x < fragment.getWidth() && x < charta.getWidth(); x++) {
                    image.setRGB(x - fragment.getX(), y - fragment.getY(), chartaStorage.readPixel(x, y));
                }
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                ImageIO.write(image, "bmp", outputStream);
                data = outputStream.toByteArray();
            }
        } catch (IOException e) {
            // ignored
        }

        return data;
    }

    public void delete(Charta charta) {
        File chartaFile = new File(storage, setStringByCharta(charta));
        if (chartaFile.isDirectory()) {
            File[] files = chartaFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean ignored = file.delete();
                }
            }
            boolean ignored = chartaFile.delete();
        }
    }
}
