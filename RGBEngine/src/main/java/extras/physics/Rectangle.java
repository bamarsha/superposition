package extras.physics;

import engine.util.math.Vec2d;

import java.util.Objects;

public class Rectangle {

    public final Vec2d lowerLeft, upperRight;

    public Rectangle(Vec2d lowerLeft, Vec2d upperRight) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    public static Rectangle fromCenterSize(Vec2d center, Vec2d size) {
        return new Rectangle(center.sub(size.div(2)), center.add(size.div(2)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rectangle rectangle = (Rectangle) o;
        return Objects.equals(lowerLeft, rectangle.lowerLeft) &&
                Objects.equals(upperRight, rectangle.upperRight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerLeft, upperRight);
    }

    public boolean intersects(Rectangle other) {
        return lowerLeft.x <= other.upperRight.x && lowerLeft.y <= other.upperRight.y
                && upperRight.x >= other.lowerLeft.x && upperRight.y >= other.lowerLeft.y;
    }
}
