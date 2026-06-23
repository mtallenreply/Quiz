# Modul 7: Interfaces and Generics

## Übersicht

Interfaces definieren Verträge ohne Implementierung. Generics ermöglichen typensichere, wiederverwendbare Klassen und Methoden. Zusammen bilden sie das Fundament der Java-Standardbibliothek.

| Thema | Dauer |
|---|---|
| Describe Java interfaces | 9m |
| Implement Interfaces | 17m |
| Extend Interfaces and Use Functional Interfaces | 13m |
| Use Generics | 11m |
| Explore Commonly Used Java Interfaces and Use a Composition Pattern | 20m |
| Practice 7-1: Design the Rateable Interface | 26m |
| Practice 7-2: Process Products Review and Rating | 23m |
| Skill Check: Interfaces and Generics (mind. 80%) | — |

---

## 1. Java Interfaces

### **[Anfänger]** Interface-Grundlagen

Ein Interface definiert einen **Vertrag** — eine Menge von Methoden, die implementierende Klassen bereitstellen müssen:

```java
// Interface definieren
public interface Rateable {
    int getStars();           // implizit public abstract
    void setRating(int stars);
}

// Interface implementieren
public class Product implements Rateable {
    private int stars;

    @Override
    public int getStars() { return stars; }

    @Override
    public void setRating(int stars) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException();
        this.stars = stars;
    }
}
```

Unterschied zu abstrakten Klassen:

| | Interface | Abstrakte Klasse |
|---|---|---|
| Felder | nur `public static final` | beliebige Felder |
| Konstruktor | keiner | möglich |
| Vererbung | beliebig viele | nur eine |
| Verwendung | Vertrag / Fähigkeit | gemeinsame Basis |

### **[Fortgeschritten]** Default- und statische Methoden in Interfaces `[Java 8]`

```java
public interface Rateable {
    int getStars();

    // Default-Methode: hat eine Implementierung, kann überschrieben werden
    default String toStarString() {
        return "*".repeat(getStars());
    }

    // Statische Methode: gehört zum Interface, nicht zur Instanz
    static Rateable ofStars(int stars) {
        return () -> stars;  // Lambda als Implementierung (Functional Interface)
    }

    // Private Methode [Java 9]: nur intern nutzbar
    private void validate(int stars) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException();
    }
}
```

### **[Professionell]** Interfaces erweitern und Mehrfachimplementierung

```java
public interface Printable {
    void print();
}

// Interface erbt von mehreren Interfaces
public interface Rateable extends Printable, Comparable<Rateable> {
    int getStars();

    @Override
    default void print() {
        System.out.println(toStarString());
    }

    @Override
    default int compareTo(Rateable other) {
        return Integer.compare(this.getStars(), other.getStars());
    }
}

// Klasse implementiert mehrere Interfaces
public class Product implements Rateable, Printable, Cloneable {
    // ...
}
```

---

## 2. Functional Interfaces

### **[Anfänger]** Was ist ein Functional Interface?

Ein **Functional Interface** hat genau **eine abstrakte Methode** und kann als Lambda-Ausdruck verwendet werden:

```java
@FunctionalInterface
public interface Rateable {
    int getStars();  // einzige abstrakte Methode
    // Default-Methoden sind erlaubt
}

// Als Lambda:
Rateable fiveStar = () -> 5;
System.out.println(fiveStar.getStars());  // 5
```

### **[Fortgeschritten]** Eingebaute Functional Interfaces aus `java.util.function`

```java
// Predicate<T>: nimmt T, gibt boolean zurück
Predicate<Product> isCheap = p -> p.getPrice() < 2.0;
System.out.println(isCheap.test(new Product("Apple", 0.89, 5))); // true

// Function<T, R>: nimmt T, gibt R zurück
Function<Product, String> toLabel = p -> p.getName() + ": " + p.getPrice() + "€";
System.out.println(toLabel.apply(product));

// Consumer<T>: nimmt T, gibt nichts zurück
Consumer<Product> printer = p -> System.out.println(p);
printer.accept(product);

// Supplier<T>: gibt T zurück, nimmt nichts
Supplier<Product> factory = () -> new Product("Default", 0.0, 1);
Product p = factory.get();

// BiFunction<T, U, R>: nimmt T und U, gibt R zurück
BiFunction<String, Double, Product> creator = (name, price) ->
    new Product(name, price, 1);
```

### **[Professionell]** Komposition von Functional Interfaces

```java
Predicate<Product> isCheap    = p -> p.getPrice() < 2.0;
Predicate<Product> isTopRated = p -> p.getStars() == 5;

// AND: beide Bedingungen müssen wahr sein
Predicate<Product> isBargain = isCheap.and(isTopRated);

// OR: mindestens eine Bedingung wahr
Predicate<Product> isRecommended = isCheap.or(isTopRated);

// NOT: Umkehrung
Predicate<Product> isExpensive = isCheap.negate();

// Function-Komposition
Function<Product, String> getName    = Product::getName;
Function<String, Integer> strLength  = String::length;
Function<Product, Integer> nameLength = getName.andThen(strLength);
```

---

## 3. Generics

### **[Anfänger]** Warum Generics?

Ohne Generics:
```java
List list = new ArrayList();
list.add("Hallo");
list.add(42);
String s = (String) list.get(0);  // Manueller Cast nötig
String x = (String) list.get(1);  // ClassCastException zur Laufzeit!
```

Mit Generics:
```java
List<String> list = new ArrayList<>();
list.add("Hallo");
// list.add(42);  // Compilerfehler — sicher!
String s = list.get(0);  // Kein Cast nötig
```

### **[Fortgeschritten]** Eigene generische Klassen und Methoden

```java
// Generische Klasse
public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first  = first;
        this.second = second;
    }

    public A getFirst()  { return first; }
    public B getSecond() { return second; }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}

Pair<String, Integer> pair = new Pair<>("Alter", 30);
System.out.println(pair);  // (Alter, 30)

// Generische Methode
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
System.out.println(max(3, 7));          // 7
System.out.println(max("Anna", "Bert")); // "Bert"
```

### **[Professionell]** Wildcards und Bounded Type Parameters

```java
// Upper Bounded Wildcard: <? extends T>
// Lesen aus der Collection erlaubt, Schreiben verboten
public static double sumPrices(List<? extends Product> products) {
    return products.stream().mapToDouble(Product::getPrice).sum();
}
List<Food> foods = List.of(new Food("Bread", 2.5, LocalDate.now()));
sumPrices(foods);  // Funktioniert! Food extends Product

// Lower Bounded Wildcard: <? super T>
// Schreiben in die Collection erlaubt
public static void addProducts(List<? super Food> list) {
    list.add(new Food("Milk", 1.2, LocalDate.now()));
}

// Unbounded Wildcard: <?>
public static void printAll(List<?> list) {
    list.forEach(System.out::println);  // Nur lesen
}

// PECS-Prinzip: Producer Extends, Consumer Super
// Liest man (Producer): extends; Schreibt man (Consumer): super
```

---

## 4. Häufig verwendete Interfaces und Kompositions-Muster

### **[Anfänger]** `Comparable` und `Comparator`

```java
// Comparable: natürliche Sortierung in der Klasse selbst
public class Product implements Comparable<Product> {
    @Override
    public int compareTo(Product other) {
        return Double.compare(this.price, other.price);  // nach Preis sortieren
    }
}

List<Product> list = new ArrayList<>(products);
Collections.sort(list);  // verwendet compareTo()

// Comparator: externe Sortierregel
Comparator<Product> byName  = Comparator.comparing(Product::getName);
Comparator<Product> byPrice = Comparator.comparingDouble(Product::getPrice).reversed();
Comparator<Product> combined = byPrice.thenComparing(byName);

list.sort(combined);
```

### **[Fortgeschritten]** Kompositions-Muster (Composition Pattern)

Statt Vererbung: Verhalten durch Interfaces zusammensetzen:

```java
public interface Discountable {
    double getDiscount();
    default double getFinalPrice(double price) {
        return price * (1 - getDiscount());
    }
}

public interface Printable {
    default void print() {
        System.out.println(this);
    }
}

// Klasse kombiniert mehrere Fähigkeiten durch Interfaces
public class Product implements Rateable, Discountable, Printable {
    // Jedes Interface bringt Verhalten mit
}
```

### **[Professionell]** `Iterable` und eigene Iterator-Implementierung

```java
public class ProductList implements Iterable<Product> {
    private final List<Product> products = new ArrayList<>();

    public void add(Product p) { products.add(p); }

    @Override
    public Iterator<Product> iterator() {
        return products.iterator();
    }
}

// Verwendung in for-each
ProductList list = new ProductList();
for (Product p : list) {
    System.out.println(p);
}
```

---

## Übungsaufgaben

### **[Anfänger]** Practice 7-1: Design the Rateable Interface (ca. 26 Minuten)

1. Erstelle ein Interface `Rateable<T>` mit:
   - `Rating getRating()`
   - `T applyRating(Rating rating)` — gibt ein Objekt mit der neuen Bewertung zurück
2. Füge eine Default-Methode `getAverageRating(List<Rateable<?>> items)` hinzu, die den Durchschnitt berechnet.
3. Lass `Product` das Interface implementieren:
   ```java
   @Override
   public Product applyRating(Rating rating) {
       return new Product(this.name, this.price, rating);
   }
   ```
4. Teste mit verschiedenen Produkten.

---

### **[Fortgeschritten]** Practice 7-2: Process Products Review and Rating (ca. 23 Minuten)

1. Erstelle ein `Review`-Record mit `author`, `rating` (Rating-Enum) und `comment`.
2. Erstelle ein `Reviewable`-Interface mit `void addReview(Review review)` und `double getAverageRating()`.
3. Lass `Product` `Reviewable` implementieren (intern eine `List<Review>`).
4. Schreibe eine generische Methode:
   ```java
   public static <T extends Reviewable> T getBestRated(List<T> items) {
       // gibt das Element mit dem höchsten Durchschnittsrating zurück
   }
   ```
5. **Professionell:** Erstelle ein `Printable`-Interface und implementiere eine Default-Methode, die alle Reviews formatiert ausgibt.
6. **Professionell:** Verwende `Comparator.comparing()` + `Comparator.reversed()` um Reviews nach Datum zu sortieren.

---

## Multiple-Choice-Fragen

### [Anfänger]

**Frage 1:** Was ist der Hauptunterschied zwischen einem Interface und einer abstrakten Klasse?

- A) Interfaces haben keine Methoden
- B) Abstrakte Klassen können keine Felder haben
- C) **Eine Klasse kann mehrere Interfaces implementieren, aber nur eine abstrakte Klasse erweitern** ✓
- D) Interfaces können instanziiert werden, abstrakte Klassen nicht

---

**Frage 2:** Was ist ein Functional Interface?

- A) Ein Interface mit genau einer Default-Methode
- B) Ein Interface ohne Methoden
- C) **Ein Interface mit genau einer abstrakten Methode, verwendbar als Lambda-Ausdruck** ✓
- D) Ein Interface, das `java.util.function` erweitert

---

**Frage 3:** Was macht `Predicate<T>.test(T t)`?

- A) Gibt `T` zurück
- B) Druckt `t` aus
- C) Transformiert `t`
- D) **Prüft eine Bedingung und gibt `boolean` zurück** ✓

---

### [Fortgeschritten]

**Frage 4:** Was ist eine Default-Methode in einem Interface? `[Java 8]`

- A) Eine Methode, die immer `null` zurückgibt
- B) Eine abstrakte Methode mit Standardsignatur
- C) **Eine Methode mit konkreter Implementierung im Interface, die überschrieben werden kann** ✓
- D) Eine private Methode im Interface

---

**Frage 5:** Was ist das PECS-Prinzip bei Generics?

- A) Performance, Efficiency, Concurrency, Safety
- B) **Producer Extends, Consumer Super — gibt an, wann `extends` oder `super` als Wildcard verwendet wird** ✓
- C) Public, Encapsulated, Concrete, Static
- D) Ein Designmuster für Fabrikmethoden

---

**Frage 6:** Was ist der Unterschied zwischen `Comparable` und `Comparator`?

- A) `Comparator` ist schneller als `Comparable`
- B) `Comparable` ist für Strings, `Comparator` für Zahlen
- C) **`Comparable` definiert die natürliche Sortierung in der Klasse; `Comparator` ist eine externe, austauschbare Sortierregel** ✓
- D) Es gibt keinen Unterschied

---

### [Professionell]

**Frage 7:** Was ist eine Upper Bounded Wildcard `<? extends T>`?

- A) Erlaubt Objekte die T implementieren, zu schreiben
- B) Begrenzt den Typ auf Superklassen von T
- C) **Erlaubt das Lesen von T und seinen Subtypen; Schreiben in die Collection ist verboten** ✓
- D) Entspricht `<T extends Object>`

---

**Frage 8:** Was gibt `Function<A,B>.andThen(Function<B,C>)` zurück?

- A) `Function<A,C>` die erst A→B, dann B→C anwendet ✓
- B) `BiFunction<A,B,C>`
- C) `Function<B,C>`
- D) `Consumer<A>`

> *`andThen` komponiert zwei Funktionen zu einer neuen Funktion.*

---

**Frage 9:** Was ist das Kompositions-Muster (Composition Pattern)?

- A) Mehrere Klassen zu einer verschmelzen
- B) Eine Klasse durch Vererbung aus mehreren Klassen aufbauen
- C) **Verhalten durch Kombination mehrerer Interfaces statt durch tiefe Vererbungshierarchien zusammensetzen** ✓
- D) Designmuster für Factory-Methoden

---

## Skill Check: Typische Prüfungsfragen

### [Anfänger]
1. Was ist der Unterschied zwischen `implements` und `extends`?
2. Kann eine Klasse mehrere Interfaces implementieren? Kann sie mehrere Klassen erweitern?
3. Was ist `@FunctionalInterface` und wozu dient die Annotation?

### [Fortgeschritten]
4. Was sind Default-Methoden in Interfaces und welches Problem lösen sie?
5. Was ist der Unterschied zwischen `Predicate`, `Function`, `Consumer` und `Supplier`?
6. Was ist Type Erasure bei Generics und welche Konsequenzen hat sie zur Laufzeit?

### [Professionell]
7. Erkläre das PECS-Prinzip mit einem konkreten Beispiel.
8. Was passiert, wenn zwei Interfaces dieselbe Default-Methode definieren und eine Klasse beide implementiert?
9. Wie unterscheidet sich `Comparator.comparing(Product::getName).thenComparing(Product::getPrice)` von einem manuell implementierten Comparator?
