package intership.task.chartographer;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

public class Fragment {
    @Positive
    private int width;
    @Positive
    private int height;
    @PositiveOrZero
    private int x;
    @PositiveOrZero
    private int y;

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
