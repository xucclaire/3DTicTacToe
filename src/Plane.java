import java.util.HashMap;

public class Plane {

    private long positions;
    private String name;

    private Plane(String name) {
        this.positions = 0;
        this.name = name;
    }

    private Plane(long positions) {
        this.positions = positions;
        this.name = "";
    }

    public long positions() {
        return this.positions;
    }

    public String name() {
        return this.name;
    }

    public boolean contains(int position) {
        return Bit.isSet(this.positions, position);
    }

    public boolean contains(Line line) {
        return (this.positions & line.positions()) == line.positions();
    }

    public boolean intersects(Plane plane) {
        return (this.positions & plane.positions) != 0;
    }

    public boolean intersects(Line line) {
        return (this.positions & line.positions()) != 0;
    }

    public Line intersection(Plane plane) {
        return Line.find(this.positions & plane.positions);
    }

    public int intersection(Line line) {
        return Coordinate.msb(line.positions() & this.positions);
    }

    public boolean equals(Plane other) {
        return this.positions == other.positions;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Plane && this.equals((Plane) other);
    }

    @Override
    public String toString() {
        return this.name + ": " + Bit.toString(this.positions);
    }

    public String toString(int base) {
        return this.name + ": " + Bit.toString(this.positions, base);
    }


    private static enum Axis { X, Y, Z }
    private static final int N = Coordinate.N;

    private void set(int x, int y, int z) {
        this.positions = Bit.set(this.positions, Coordinate.position(x, y, z));
    }

    private static Plane Straight(Axis axis, int value) {
        String name = "";
        switch (axis) {
            case X: name = "YZ-Plane, X = " + value; break;
            case Y: name = "XZ-Plane, Y = " + value; break;
            case Z: name = "XY-Plane, Z = " + value; break;
        }

        Plane plane = new Plane(name);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (axis) {
                    case X: plane.set(value, i, j); break;
                    case Y: plane.set(i, value, j); break;
                    case Z: plane.set(i, j, value); break;
                }
            }
        }
        return plane;
    }

    private static Plane ForwardDiagonal(Axis axis) {
        String name = "";
        switch (axis) {
            case X: name = "YZ-Forward Diagonal"; break;
            case Y: name = "XZ-Forward Diagonal"; break;
            case Z: name = "XY-Forward Diagonal"; break;
        }

        Plane plane = new Plane(name);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (axis) {
                    case X: plane.set(i, j, j); break;
                    case Y: plane.set(j, i, j); break;
                    case Z: plane.set(j, j, i); break;
                }
            }
        }
        return plane;
    }

    private static Plane ReverseDiagonal(Axis axis) {
        String name = "";
        switch (axis) {
            case X: name = "YZ-Reverse Diagonal"; break;
            case Y: name = "XZ-Reverse Diagonal"; break;
            case Z: name = "XY-Reverse Diagonal"; break;
        }

        Plane plane = new Plane(name);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (axis) {
                    case X: plane.set(i, j, N-1-j); break;
                    case Y: plane.set(j, i, N-1-j); break;
                    case Z: plane.set(j, N-1-j, i); break;
                }
            }
        }
        return plane;
    }

    public static final Plane[] planes = new Plane[18];
    static {
        int count = 0;
        for (Axis axis : Axis.values()) {
            for (int value = 0; value < N; value++) {
                planes[count++] = Straight(axis, value);
            }
            planes[count++] = ForwardDiagonal(axis);
            planes[count++] = ReverseDiagonal(axis);
        }
        assert count == 18;
    }

    private static final HashMap<Long,Plane> map = new HashMap<>();
    static {
        for (Plane plane : planes) {
            map.put(plane.positions(), plane);
        }
    }

    public static Plane find(long positions) {
        return map.get(positions);
    }

    public static void main(String[] args) {
        int base = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        for (Plane plane : planes) {
            System.out.printf("%19s: %s\n", plane.name(), Bit.toString(plane.positions(), base));
        }
    }
}
