import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Kompilierbarkeitstest für die größeren Klassen/Interface-Beispiele aus den MD-Dateien.
 * Ausführen: java StructuralExamples.java
 */

// ── Modul 07: Interfaces ────────────────────────────────────────────────────

@FunctionalInterface
interface Rateable {
    int getStars();

    default String toStarString() { return "*".repeat(getStars()); }

    static Rateable ofStars(int stars) { return () -> stars; }
}

interface Printable {
    default void print() { System.out.println(this); }
}

interface Discountable {
    double getDiscount();
    default double getFinalPrice(double price) { return price * (1 - getDiscount()); }
}

class Product implements Rateable, Printable, Discountable, Comparable<Product> {
    private final String name;
    private final double price;
    private int stars;

    Product(String name, double price, int stars) {
        this.name = name; this.price = price; this.stars = stars;
    }

    @Override public int getStars()      { return stars; }
    @Override public double getDiscount(){ return 0.1; }
    @Override public int compareTo(Product o) { return Double.compare(this.price, o.price); }
    @Override public String toString()   { return name + "(" + price + "€, " + stars + "★)"; }
    public String getName()   { return name; }
    public double getPrice()  { return price; }
}

// ── Modul 07: Generics ──────────────────────────────────────────────────────

class Pair<A, B> {
    private final A first;
    private final B second;
    Pair(A first, B second) { this.first = first; this.second = second; }
    public A getFirst()  { return first; }
    public B getSecond() { return second; }
    @Override public String toString() { return "(" + first + ", " + second + ")"; }
}

// ── Modul 06: Inheritance ───────────────────────────────────────────────────

abstract class Shape {
    abstract double area();
    @Override public String toString() { return getClass().getSimpleName() + "[area=" + area() + "]"; }
}

final class Circle extends Shape {
    private final double r;
    Circle(double r) { this.r = r; }
    @Override public double area() { return Math.PI * r * r; }
}

final class Rectangle extends Shape {
    private final double w, h;
    Rectangle(double w, double h) { this.w = w; this.h = h; }
    @Override public double area() { return w * h; }
}

// ── Modul 05: Enums ─────────────────────────────────────────────────────────

enum Rating {
    ONE_STAR(1, "sehr schlecht"),
    TWO_STARS(2, "schlecht"),
    THREE_STARS(3, "ok"),
    FOUR_STARS(4, "gut"),
    FIVE_STARS(5, "sehr gut");

    private final int stars;
    private final String label;

    Rating(int stars, String label) { this.stars = stars; this.label = label; }
    public int getStars()    { return stars; }
    public String getLabel() { return label; }
}

// ── Modul 04: Records ───────────────────────────────────────────────────────

record Point(double x, double y) {
    // compact constructor
    Point { if (Double.isNaN(x) || Double.isNaN(y)) throw new IllegalArgumentException(); }
    double distance(Point other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }
}

// ── Modul 04: Sealed (Java 17) ──────────────────────────────────────────────

sealed interface JsonValue permits JsonNumber, JsonString, JsonNull {}
record JsonNumber(double value) implements JsonValue {}
record JsonString(String value) implements JsonValue {}
final class JsonNull implements JsonValue { static final JsonNull INSTANCE = new JsonNull(); }

// ── Hauptprogramm ───────────────────────────────────────────────────────────

class StructuralExamples {

    static int passed = 0, failed = 0;
    static void ok(String label, Object actual, Object expected) {
        if (Objects.equals(actual, expected)) { System.out.printf("  ✓ %s%n", label); passed++; }
        else { System.out.printf("  ✗ %s — erwartet %s, erhalten %s%n", label, expected, actual); failed++; }
    }

    void main() {
        testInterfaces();
        testGenerics();
        testInheritance();
        testEnums();
        testRecords();
        testFunctionalInterfaces();
        testSealed();

        System.out.printf("%n═══ Ergebnis: %d/%d bestanden ═══%n", passed, passed + failed);
        if (failed > 0) System.exit(1);
    }

    static void testInterfaces() {
        System.out.println("\n── Interfaces (Modul 07) ──");
        Rateable five = Rateable.ofStars(5);
        ok("ofStars(5).getStars()", five.getStars(), 5);
        ok("ofStars(5).toStarString()", five.toStarString(), "*****");

        Product p = new Product("Apple", 0.89, 4);
        ok("Product.toStarString()", p.toStarString(), "****");
        ok("Product.getFinalPrice(10)", p.getFinalPrice(10.0), 9.0);

        // Functional Interface als Lambda
        Rateable three = () -> 3;
        ok("Lambda Rateable", three.getStars(), 3);

        // Predicate composition
        Predicate<Product> isCheap    = prod -> prod.getPrice() < 1.0;
        Predicate<Product> isTopRated = prod -> prod.getStars() == 4;
        ok("Predicate.and()", isCheap.and(isTopRated).test(p), true);
        ok("Predicate.negate()", isCheap.negate().test(p), false);

        // Comparator
        List<Product> products = new ArrayList<>(List.of(
            new Product("Cherry", 1.5, 3),
            new Product("Apple",  0.89, 4),
            new Product("Banana", 0.99, 5)
        ));
        products.sort(Comparator.comparingDouble(Product::getPrice));
        ok("Comparator sort by price", products.get(0).getName(), "Apple");
    }

    static void testGenerics() {
        System.out.println("\n── Generics (Modul 07) ──");
        Pair<String, Integer> p = new Pair<>("Alter", 30);
        ok("Pair.getFirst()",  p.getFirst(),  "Alter");
        ok("Pair.getSecond()", p.getSecond(), 30);
        ok("Pair.toString()",  p.toString(),  "(Alter, 30)");

        // Generic method
        ok("max(3,7)",           max(3, 7),       7);
        ok("max(Anna,Bert)",     max("Anna", "Bert"), "Bert");

        // Bounded wildcard
        List<Product> list = List.of(
            new Product("A", 1.0, 3),
            new Product("B", 2.5, 5)
        );
        ok("sumPrices()", sumPrices(list), 3.5);
    }

    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    static double sumPrices(List<? extends Product> products) {
        return products.stream().mapToDouble(Product::getPrice).sum();
    }

    static void testInheritance() {
        System.out.println("\n── Vererbung / Polymorphismus (Modul 06) ──");
        Shape c = new Circle(1.0);
        Shape r = new Rectangle(3.0, 4.0);
        ok("Circle.area()",    c.area(),  Math.PI);
        ok("Rectangle.area()", r.area(), 12.0);

        // Polymorphismus
        List<Shape> shapes = List.of(new Circle(2), new Rectangle(2, 3));
        double total = shapes.stream().mapToDouble(Shape::area).sum();
        ok("Poly sum area", Math.round(total * 100) / 100.0, Math.round((4 * Math.PI + 6) * 100) / 100.0);

        // instanceof pattern matching [Java 16]
        Object obj = new Circle(5);
        if (obj instanceof Circle circ) {
            ok("instanceof pattern", circ.area() > 0, true);
        }
    }

    static void testEnums() {
        System.out.println("\n── Enums (Modul 05) ──");
        ok("Rating.FIVE_STARS.getStars()",  Rating.FIVE_STARS.getStars(),  5);
        ok("Rating.THREE_STARS.getLabel()", Rating.THREE_STARS.getLabel(), "ok");
        ok("Rating.values().length",        Rating.values().length,        5);
        ok("Rating.valueOf(ONE_STAR)",       Rating.valueOf("ONE_STAR"),    Rating.ONE_STAR);
        ok("Enum ordinal",                  Rating.TWO_STARS.ordinal(),    1);
    }

    static void testRecords() {
        System.out.println("\n── Records (Modul 04/05) ──");
        Point p1 = new Point(0, 0);
        Point p2 = new Point(3, 4);
        ok("Point.x()",          p1.x(),          0.0);
        ok("Point distance(3,4)", p1.distance(p2), 5.0);
        ok("Record equals",      new Point(1, 2).equals(new Point(1, 2)), true);
        ok("Record toString",    p1.toString(),   "Point[x=0.0, y=0.0]");
    }

    static void testFunctionalInterfaces() {
        System.out.println("\n── Functional Interfaces (Modul 07) ──");
        UnaryOperator<String> toUpper = String::toUpperCase;
        ok("UnaryOperator toUpper", toUpper.apply("hallo"), "HALLO");

        BinaryOperator<Integer> add = Integer::sum;
        ok("BinaryOperator add",    add.apply(3, 4), 7);

        Function<String, Integer> length = String::length;
        Function<Integer, String> intStr = Object::toString;
        Function<String, String> composed = length.andThen(intStr);
        ok("Function.andThen()", composed.apply("Hallo"), "5");

        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        int sum = nums.stream().reduce(0, Integer::sum);
        ok("Stream.reduce(BinaryOp)", sum, 15);
    }

    static void testSealed() {
        System.out.println("\n── Sealed Interface (Modul 07) ──");
        JsonValue num = new JsonNumber(42.0);
        JsonValue str = new JsonString("test");
        JsonValue nul = JsonNull.INSTANCE;

        String result = switch (num) {
            case JsonNumber n -> "number:" + (int)n.value();
            case JsonString s -> "string:" + s.value();
            case JsonNull   n -> "null";
        };
        ok("Sealed switch exhaustive", result, "number:42");
        ok("JsonString type", str instanceof JsonString, true);
        ok("JsonNull singleton", nul == JsonNull.INSTANCE, true);
    }
}
