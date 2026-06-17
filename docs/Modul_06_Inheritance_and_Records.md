# Modul 6: Implement Inheritance and Use Records

## Übersicht

Dieses Modul ist eines der umfangreichsten der Schulung. Es behandelt Vererbung, Polymorphismus, abstrakte und sealed Klassen, Records, Factory-Methoden und Pattern Matching für Switch.

| Thema | Dauer |
|---|---|
| Extend Classes and Reuse Code through Inheritance | 11m |
| Instantiate Classes and Access Objects, Rules of Reference Type Casting | 15m |
| Verify Object Type using instanceof, Perform Reference Type Casting | 10m |
| Reference Code within Object `[Java 25]` | 3m |
| Define Subclass Constructors and Flexible Constructor Bodies `[Java 25]` | 4m |
| Class Object and Initialization summary `[Java 25]` | 5m |
| Override Methods and Using Polymorphism | 12m |
| Define Abstract, Final, Sealed Classes and Interfaces | 15m |
| Override Object Class Operations | 20m |
| Define Record Classes, Pattern Matching for Switch | 13m |
| Define and Use Factory Methods | 8m |
| Practice 6-1: Create Food and Drink Classes That Extend Product | 19m |
| Practice 6-2: Override Methods and Use Polymorphism | 1h 12m |
| Practice 6-3: Create Factory Methods | 16m |
| Practice 6-4: Implement Sealed Classes | 4m |
| Practice 6-5: Explore Java Records | 16m |
| Skill Check: Implement Inheritance and Use Records (mind. 80%) | — |

---

## 1. Vererbung — Klassen erweitern

### **[Anfänger]** Grundlagen der Vererbung

Vererbung ermöglicht es, Eigenschaften und Methoden einer Klasse in einer anderen wiederzuverwenden:

```java
// Superklasse (Elternklasse)
public class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name  = name;
        this.price = price;
    }

    public String getName()  { return name; }
    public double getPrice() { return price; }

    public String toString() {
        return "Product[name=" + name + ", price=" + price + "]";
    }
}

// Subklasse (Kindklasse) mit extends
public class Food extends Product {
    private LocalDate bestBefore;

    public Food(String name, double price, LocalDate bestBefore) {
        super(name, price);           // Superklassen-Konstruktor aufrufen
        this.bestBefore = bestBefore;
    }

    public LocalDate getBestBefore() { return bestBefore; }
}
```

Regeln:
- Eine Klasse kann nur **eine** direkte Superklasse haben (`extends`)
- Alle Klassen erben implizit von `Object`
- `super` verweist auf die Superklasse

### **[Fortgeschritten]** Referenz-Typ-Casting

```java
Product p = new Food("Bread", 2.50, LocalDate.now().plusDays(7)); // Upcasting: immer sicher
Food f = (Food) p;  // Downcasting: manuell, kann zur ClassCastException führen

// Sicher prüfen vor dem Cast
if (p instanceof Food food) {          // Pattern Matching [Java 16]
    System.out.println(food.getBestBefore());
}
```

### **[Professionell]** Flexible Constructor Bodies `[Java 25]`

Ab Java 25 können Subklassen-Konstruktoren vor `super()` validieren:

```java
public class Food extends Product {
    private final LocalDate bestBefore;

    public Food(String name, double price, LocalDate bestBefore) {
        // Java 25: Berechnung/Validierung VOR super() erlaubt
        var validDate = bestBefore != null ? bestBefore : LocalDate.now();
        super(name, price);
        this.bestBefore = validDate;
    }
}
```

---

## 2. Methoden überschreiben und Polymorphismus

### **[Anfänger]** Methoden überschreiben (`@Override`)

```java
public class Food extends Product {
    private LocalDate bestBefore;

    public Food(String name, double price, LocalDate bestBefore) {
        super(name, price);
        this.bestBefore = bestBefore;
    }

    @Override
    public String toString() {
        return "Food[name=" + getName()
             + ", price=" + getPrice()
             + ", bestBefore=" + bestBefore + "]";
    }
}
```

Regeln:
- Gleiche Methodensignatur (Name + Parameter)
- Rückgabetyp darf eine Subklasse sein (kovarianter Rückgabetyp)
- Zugriffsmodifier darf nicht restriktiver werden
- `@Override` prüft zur Compile-Zeit — immer verwenden!

### **[Fortgeschritten]** Polymorphismus

```java
List<Product> products = new ArrayList<>();
products.add(new Food("Bread", 2.50, LocalDate.now().plusDays(7)));
products.add(new Drink("Water", 0.99, 0.5));
products.add(new Product("Unknown", 1.0));

// Polymorphismus: welche toString()-Version aufgerufen wird,
// entscheidet das echte Objekt zur Laufzeit (Late Binding)
for (Product p : products) {
    System.out.println(p);  // Ruft Food-, Drink- oder Product-toString() auf
}
```

### **[Professionell]** `super` zum Wiederverwenden von Superklassen-Methoden

```java
@Override
public String toString() {
    return super.toString()               // Product-Anteil wiederverwenden
         + ", bestBefore=" + bestBefore;
}
```

---

## 3. Abstrakte Klassen, Final und Sealed Classes

### **[Anfänger]** Abstrakte Klassen

Abstrakte Klassen können nicht instanziiert werden — sie dienen als Vorlage:

```java
public abstract class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name  = name;
        this.price = price;
    }

    // Abstrakte Methode: muss in jeder konkreten Subklasse implementiert werden
    public abstract double getDiscount();

    // Konkrete Methode: wird vererbt
    public double getFinalPrice() {
        return price - getDiscount();
    }
}

// Konkrete Subklasse muss getDiscount() implementieren
public class Food extends Product {
    @Override
    public double getDiscount() {
        return getPrice() * 0.1;
    }
}
```

### **[Fortgeschritten]** `final`-Klassen und -Methoden

```java
public final class ImmutableProduct extends Product {  // Keine Subklassen möglich
    // ...
}

public class Product {
    public final String getType() {    // Methode kann nicht überschrieben werden
        return "Product";
    }
}
```

### **[Professionell]** Sealed Classes `[Java 17]`

Sealed Classes begrenzen, welche Klassen eine Klasse erweitern dürfen:

```java
// Nur Food, Drink und Service dürfen Product erweitern
public sealed class Product permits Food, Drink, Service {
    // ...
}

public final class Food extends Product { ... }    // final: selbst nicht mehr erweiterbar
public final class Drink extends Product { ... }
public non-sealed class Service extends Product { ... } // non-sealed: wieder offen

// Pattern Matching für Sealed Classes [Java 21]:
// Compiler weiß, dass alle Fälle abgedeckt sind → kein default nötig
double discount = switch (product) {
    case Food f    -> f.getPrice() * 0.10;
    case Drink d   -> d.getPrice() * 0.05;
    case Service s -> 0.0;
};
```

---

## 4. Records `[Java 16]` → `[Java 17 LTS]`

### **[Anfänger]** Record-Grundlagen

```java
public record Product(String name, double price, Rating rating) { }

// Automatisch generiert:
// - Konstruktor mit allen Feldern
// - Getter: name(), price(), rating()
// - equals(), hashCode(), toString()
Product p = new Product("Apple", 0.89, Rating.FIVE_STARS);
System.out.println(p.name());   // "Apple"
System.out.println(p);          // Product[name=Apple, price=0.89, rating=FIVE_STARS]
```

### **[Fortgeschritten]** Records mit Validierung und zusätzlichen Methoden

```java
public record Product(String name, double price, Rating rating) {

    // Kompakter Konstruktor: Parameter sind implizit vorhanden
    public Product {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name darf nicht leer sein");
        if (price < 0)
            throw new IllegalArgumentException("Preis darf nicht negativ sein");
        // Normalisierung
        name = name.trim();
    }

    // Statische Factory-Methode
    public static Product of(String name, double price) {
        return new Product(name, price, Rating.ONE_STAR);
    }

    // Zusätzliche Instanzmethode
    public double getFinalPrice() {
        return price * (1 - rating.getDiscount());
    }
}
```

### **[Professionell]** Pattern Matching für Switch mit Records `[Java 21]`

```java
sealed interface Shape permits Circle, Rectangle, Triangle { }
record Circle(double radius) implements Shape { }
record Rectangle(double width, double height) implements Shape { }
record Triangle(double base, double height) implements Shape { }

double area = switch (shape) {
    case Circle(var r)            -> Math.PI * r * r;
    case Rectangle(var w, var h)  -> w * h;
    case Triangle(var b, var h)   -> 0.5 * b * h;
};
```

---

## 5. Factory-Methoden

### **[Anfänger]** Statische Factory-Methoden

```java
public abstract class Product {

    // Factory-Methode: steuert Objekterstellung zentral
    public static Product of(String name, double price, String type) {
        return switch (type.toLowerCase()) {
            case "food"  -> new Food(name, price, LocalDate.now().plusDays(30));
            case "drink" -> new Drink(name, price, 0.5);
            default      -> throw new IllegalArgumentException("Unbekannter Typ: " + type);
        };
    }
}

Product p = Product.of("Bread", 2.50, "food");
```

### **[Fortgeschritten]** Factory-Methode mit Pattern Matching `[Java 21]`

```java
public static Product createWithDiscount(Object source) {
    return switch (source) {
        case String s   -> Product.of(s, 1.0, "food");
        case Double d   -> new Food("Unnamed", d, LocalDate.now());
        case null       -> throw new IllegalArgumentException("null nicht erlaubt");
        default         -> throw new IllegalArgumentException("Unbekannter Typ");
    };
}
```

---

## Übungsaufgaben

### **[Anfänger]** Practice 6-1: Create Food and Drink Classes That Extend Product (ca. 19 Minuten)

1. Erstelle eine abstrakte Klasse `Product` mit `name`, `price`, `rating` und einer abstrakten Methode `getDiscount()`.
2. Erstelle `Food extends Product`:
   - Zusatzfeld: `LocalDate bestBefore`
   - `getDiscount()`: 10% bei Rating 4+, sonst 0%
   - Überschreibe `toString()`
3. Erstelle `Drink extends Product`:
   - Zusatzfeld: `double volume` (in Litern)
   - `getDiscount()`: 5% immer
   - Überschreibe `toString()`
4. Erstelle in `main()` je 2 Food- und 2 Drink-Objekte und gib sie aus.

---

### **[Fortgeschritten]** Practice 6-2: Override Methods and Use Polymorphism (ca. 72 Minuten)

1. Erstelle eine `List<Product>` mit Food- und Drink-Objekten gemischt.
2. Gib alle Produkte polymorphisch aus — jedes soll seine eigene `toString()` verwenden.
3. Berechne den Gesamtpreis aller Produkte mit `getFinalPrice()`.
4. Filtere alle Food-Objekte mit Pattern Matching `instanceof` `[Java 16]`.
5. Sortiere die Liste nach Preis (Comparator).
6. Implementiere `equals()` und `hashCode()` in Product so, dass zwei Produkte gleich sind, wenn Name und Preis übereinstimmen.
7. Prüfe mit Pattern Matching für Switch `[Java 21]`, welchen Rabatt ein Produkt erhält.

---

### **[Fortgeschritten]** Practice 6-3: Create Factory Methods (ca. 16 Minuten)

1. Füge in `Product` eine statische Methode `Product.of(String name, double price, String type)` hinzu.
2. Der `type`-Parameter ("food" / "drink") entscheidet die Subklasse.
3. Wirf `IllegalArgumentException` bei unbekanntem Typ.
4. Teste mit verschiedenen Eingaben.

---

### **[Professionell]** Practice 6-4: Implement Sealed Classes (ca. 4 Minuten)

1. Wandle `Product` in eine `sealed class` um mit `permits Food, Drink`.
2. Markiere `Food` und `Drink` als `final`.
3. Verwende in `getDiscount()` einen Switch-Ausdruck ohne `default`, der alle Fälle abdeckt.

---

### **[Professionell]** Practice 6-5: Explore Java Records (ca. 16 Minuten)

1. Erstelle einen `record Review(String author, int stars, String comment)` mit Validierung (stars 1–5).
2. Erstelle einen `record ProductReview(Product product, List<Review> reviews)`.
3. Schreibe eine Methode, die den Durchschnitt der Bewertungen berechnet.
4. **Bonus:** Verwende Record Patterns im Switch `[Java 21]`:
   ```java
   switch (review) {
       case Review(var a, 5, var c) -> "Top-Bewertung von " + a;
       case Review(var a, var s, _) when s < 3 -> "Kritik von " + a;
       default -> "Normale Bewertung";
   }
   ```

---

## Multiple-Choice-Fragen

### [Anfänger]

**Frage 1:** Was bewirkt `extends` in Java?

- A) Implementiert ein Interface
- B) **Erstellt eine Subklasse, die alle public/protected-Member der Superklasse erbt** ✓
- C) Kopiert den Quellcode der Superklasse
- D) Erlaubt mehrfache Vererbung

---

**Frage 2:** Was macht `super(name, price)` im Subklassen-Konstruktor?

- A) Erstellt ein neues Objekt der Superklasse
- B) Ruft eine Methode der Superklasse auf
- C) **Ruft den Konstruktor der direkten Superklasse auf** ✓
- D) Verweist auf die Superklassen-Referenz

---

**Frage 3:** Was ist Polymorphismus in Java?

- A) Mehrere Klassen mit demselben Namen
- B) Eine Klasse mit mehreren Konstruktoren
- C) **Die Fähigkeit, über eine Superklassen-Referenz verschiedene Subklassen-Methoden aufzurufen** ✓
- D) Das Kopieren von Klassen

---

### [Fortgeschritten]

**Frage 4:** Was ist der Unterschied zwischen einer abstrakten und einer konkreten Klasse?

- A) Abstrakte Klassen haben keine Felder
- B) Konkrete Klassen können nicht vererbt werden
- C) **Abstrakte Klassen können nicht instanziiert werden und können abstrakte Methoden definieren** ✓
- D) Es gibt keinen Unterschied außer dem Schlüsselwort

---

**Frage 5:** Was bewirkt `final` bei einer Klasse?

- A) Die Klasse kann keine Felder haben
- B) Die Klasse kann nicht instanziiert werden
- C) Alle Methoden der Klasse sind final
- D) **Die Klasse kann nicht als Superklasse verwendet werden (keine Subklassen möglich)** ✓

---

**Frage 6:** Was ist ein Record? `[Java 16]`

- A) Eine abstrakte Klasse ohne Methoden
- B) Ein Interface für Datenklassen
- C) **Eine kompakte, automatisch immutable Datenklasse mit generierten Boilerplate-Methoden** ✓
- D) Ein Enum mit Feldern

---

### [Professionell]

**Frage 7:** Was ist eine Sealed Class? `[Java 17]`

- A) Eine Klasse mit nur privaten Feldern
- B) Eine Klasse, die nicht instanziiert werden kann
- C) **Eine Klasse, die explizit festlegt, welche anderen Klassen sie erweitern dürfen** ✓
- D) Eine Klasse mit finalen Methoden

---

**Frage 8:** Warum ist Pattern Matching für Switch mit Sealed Classes so mächtig? `[Java 21]`

- A) Es ist schneller als normale if-else-Ketten
- B) Es erlaubt den Zugriff auf private Felder
- C) **Der Compiler prüft Vollständigkeit — bei allen erlaubten Subklassen ist kein `default` nötig** ✓
- D) Es funktioniert nur mit Records

---

**Frage 9:** Was sind Record Patterns? `[Java 21]`

- A) Reguläre Ausdrücke für String-Matching
- B) Eine Möglichkeit, Records zu validieren
- C) **Destrukturierung eines Records direkt im Pattern — `case Point(var x, var y)`** ✓
- D) Factory-Methoden für Records

---

## Skill Check: Typische Prüfungsfragen

### [Anfänger]
1. Was ist der Unterschied zwischen Vererbung und Komposition?
2. Wann muss `super()` im Subklassen-Konstruktor aufgerufen werden?
3. Was ist der Unterschied zwischen Überschreiben (`@Override`) und Überladen?

### [Fortgeschritten]
4. Was ist der Unterschied zwischen einer abstrakten Klasse und einem Interface?
5. Was ist kovarianter Rückgabetyp beim Überschreiben?
6. Wann sollte man `final` für eine Klasse oder Methode verwenden?

### [Professionell]
7. Was sind die drei erlaubten Modifier für direkte Subklassen einer Sealed Class (`final`, `sealed`, `non-sealed`) und was bedeuten sie jeweils?
8. Was ist der Unterschied zwischen einem Record und einer Klasse mit `@Value` (Lombok)?
9. Erkläre, wie Flexible Constructor Bodies `[Java 25]` den Entwurf von Klassenhierarchien verbessern.
