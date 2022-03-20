package intership.task.chartographer.domain;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;

public class Charta {
    private Long id;

    @Positive
    @Max(20000)
    private int width;

    @Positive
    @Max(50000)
    private int height;

    public Charta(Long id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
