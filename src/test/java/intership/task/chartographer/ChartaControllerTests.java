package intership.task.chartographer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(args = {"./src/test/storage"})
@AutoConfigureMockMvc
public class ChartaControllerTests {
    @Autowired
    private MockMvc mockMvc;

    private ResultActions setFragment(String request, BufferedImage image) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", byteArrayOutputStream);
        return mockMvc.perform(post("/chartas/" + request).content(byteArrayOutputStream.toByteArray()));
    }

    private void setFragmentOK(String id, int x, int y, BufferedImage image) throws Exception {
        setFragment(id + "/?x=" + x + "&y=" + y + "&width=" + image.getWidth() + "&height=" + image.getHeight(),
                image).andExpect(status().isOk());
    }

    private ResultActions newCharta(String request) throws Exception {
        return mockMvc.perform(post("/chartas/" + request));
    }

    private String newChartaOK(int width, int height) throws Exception {
        return newCharta("?width=" + width + "&height=" + height).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private ResultActions deleteCharta(String id) throws Exception {
        return mockMvc.perform(delete("/chartas/" + id));
    }

    private void deleteChartaOK(String id) throws Exception {
        deleteCharta(id).andExpect(status().isOk());
    }

    private ResultActions getFragment(String request) throws Exception {
        return mockMvc.perform(get("/chartas/" + request));
    }

    private BufferedImage getFragmentOK(String id, int x, int y, int width, int height) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(getFragment(id + "/?x=" + x + "&y=" +
                y + "&width=" + width + "&height=" + height).
                andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray()));
    }

    private void setImage() throws IOException {
        if (image == null) {
            image = ImageIO.read(new File("./src/test/static/a.bmp"));
        }
    }
    private BufferedImage image;

    @Test
    public void NewCharta_InvalidInput_400() throws Exception {
        List<ResultActions> requests = List.of(
                newCharta(""),
                newCharta("?width=aaa&height=bbb"),
                newCharta("?width=1"),
                newCharta("?height=1"),
                newCharta("?width=-10&height=100"),
                newCharta("?width=10&height=-100"),
                newCharta("?width=0&height=100"),
                newCharta("?width=20001&height=100"),
                newCharta("?width=200&height=50001")
        );

        for (ResultActions request : requests) {
            request.andExpect(status().isBadRequest());
        }
    }

    @Test
    public void NewCharta_NormalInput_201() throws Exception {
        String[] id = {
                newChartaOK(100, 100),
                newChartaOK(100, 50000),
                newChartaOK(20000, 100)};

        for (String i : id) {
            deleteChartaOK(i);
        }
    }

    @Test
    public void SetFragment_NoSuchCharta_404() throws Exception {
        setImage();
        List<ResultActions> requests = List.of(
                setFragment("100/?x=0&y=0&width=" + image.getWidth()
                        + "&height=" + image.getHeight(), image),
                setFragment("aaaa/?x=0&y=0&width=" + image.getWidth()
                        + "&height=" + image.getHeight(), image));

        for (ResultActions request : requests) {
            request.andExpect(status().isNotFound());
        }
    }

    @Test
    public void SetFragment_InvalidInput_400() throws Exception {
        setImage();
        String id = newChartaOK(100, 100);

        try {
            List<ResultActions> requests = List.of(
                    setFragment(id + "/?x=aaa&y=0&width=" + image.getWidth()
                            + "&height=" + image.getHeight(), image),
                    setFragment(id + "/?x=0&y=0&width=-100"
                            + "&height=" + image.getHeight(), image),
                    setFragment(id + "/?x=0&y=0&width=aaa"
                            + "&height=" + image.getHeight(), image),
                    setFragment(id + "/?width=1", image),
                    setFragment(id + "/?x=100&y=0&width=" + image.getWidth()
                            + "&height=" + image.getHeight(), image)
            );
            for (ResultActions request : requests) {
                request.andExpect(status().isBadRequest());
            }
        } finally {
            deleteChartaOK(id);
        }
    }

    @Test
    public void SetFragment_InvalidImage_400() throws Exception {
        setImage();
        String id = newChartaOK(100, 100);
        try {
            setFragment(id + "/?x=0&y=0&width=" + (image.getWidth() - 10)
                    + "&height=" + image.getHeight(), image).andExpect(status().isBadRequest());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            mockMvc.perform(post("/chartas/" + id + "/?x=0&y=0&width=" + (image.getWidth() - 10)
                            + "&height=" + image.getHeight()).content(byteArrayOutputStream.toByteArray())).
                    andExpect(status().isBadRequest());
        } finally {
            deleteChartaOK(id);
        }
    }

    @Test
    public void SetFragment_NormalInput_200() throws Exception {
        setImage();
        String id = newChartaOK(100, 100);
        try {
            setFragmentOK(id, 0, 0, image);
            setFragmentOK(id, 99, 99, image);
        } finally {
            deleteChartaOK(id);
        }
    }

    @Test
    public void GetFragment_NoSuchCharta_404() throws Exception {
        setImage();
        List<ResultActions> requests = List.of(
                getFragment("100/?x=0&y=0&width=" + image.getWidth()
                        + "&height=" + image.getHeight()),
                getFragment("aaaa/?x=0&y=0&width=" + image.getWidth()
                        + "&height=" + image.getHeight()));

        for (ResultActions request : requests) {
            request.andExpect(status().isNotFound());
        }
    }

    @Test
    public void GetFragment_InvalidInput_400() throws Exception {
        setImage();
        String id = newChartaOK(100, 100);

        try {
            List<ResultActions> requests = List.of(
                    getFragment(id + "/?x=aaa&y=0&width=" + image.getWidth()
                            + "&height=" + image.getHeight()),
                    getFragment(id + "/?x=0&y=0&width=-100"
                            + "&height=" + image.getHeight()),
                    getFragment(id + "/?x=0&y=0&width=aaa"
                            + "&height=" + image.getHeight()),
                    getFragment(id + "/?width=1"),
                    getFragment(id + "/?x=100&y=0&width=" + image.getWidth()
                            + "&height=" + image.getHeight())
            );
            for (ResultActions request : requests) {
                request.andExpect(status().isBadRequest());
            }
        } finally {
            deleteChartaOK(id);
        }
    }

    @Test
    public void GetFragment_NormalInput_200() throws Exception {
        setImage();
        String id = newChartaOK(100, 100);
        try {
            getFragmentOK(id, 0, 0, 100, 100);
            getFragmentOK(id, 99, 99, 1000, 1);
        } finally {
            deleteChartaOK(id);
        }
    }

    private boolean equals(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }

        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Test
    public void SetGetFragment_NormalInput_200() throws Exception {
        setImage();
        String id = newChartaOK(100, 100);
        try {
            setFragmentOK(id, 0, 0, image);
            assert(equals(image.getSubimage(0, 0, 100, 100),
                    getFragmentOK(id, 0, 0, 100, 100)));
        } finally {
            deleteChartaOK(id);
        }
    }

    @Test
    public void SetGet1pxFragment_NormalInput_200() throws Exception {
        setImage();
        String id = newChartaOK(1, 1);
        try {
            setFragmentOK(id, -image.getWidth() / 2, -image.getHeight() / 2, image);
            assert(equals(image.getSubimage(image.getWidth() / 2, image.getHeight() / 2, 1, 1),
                    getFragmentOK(id, 0, 0, 1, 1)));
        } finally {
            deleteChartaOK(id);
        }
    }

    // There should be more tests
}
