package extras.physics;

import engine.util.math.Vec2d;

import java.util.Objects;

public class Rectangle {

    public final Vec2d lowerLeft, upperRight;

    public Rectangle(Vec2d lowerLeft, Vec2d upperRight) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    public static Rectangle boundingBox(Vec2d... points) {
        if (points.length == 0) {
            throw new IllegalArgumentException("Points must be nonempty");
        }
        double left = points[0].x, right = points[0].x, bottom = points[0].y, top = points[0].y;
        for (Vec2d v : points) {
            left = Math.min(left, v.x);
            right = Math.max(right, v.x);
            bottom = Math.min(bottom, v.y);
            top = Math.max(top, v.y);
        }
        return new Rectangle(new Vec2d(left, bottom), new Vec2d(right, top));
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
