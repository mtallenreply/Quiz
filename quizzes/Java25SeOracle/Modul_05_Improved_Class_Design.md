# Modul 5: Improved Class Design

## Übersicht

Dieses Modul vertieft den Klassenentwurf: Konstruktoren überladen, Unveränderlichkeit erzwingen, Enums einsetzen und verstehen, wie Java Speicher verwaltet und Parameter übergibt.

| Thema | Dauer |
|---|---|
| Overload Methods by Defining and Reusing Constructors | 17m |
| Define Encapsulation and Immutability using Access Modifiers | 15m |
| Define and Use Enums | 19m |
| Explain Java Memory Allocation and Parameter Passing | 11m |
| Explain Java Memory Cleanup | 11m |
| Practice 5-1: Create Enumeration to Represent Product Rating | 11m |
| Practice 5-2: Add Custom Constructors to the Product Class | 20m |
| Practice 5-3: Make Product Objects Immutable | 10m |
| Skill Check: Improved Class Design (mind. 80%) | — |

---

## 1. Konstruktoren überladen und wiederverwenden

### **[Anfänger]** Konstruktor-Überladung

Mehrere Konstruktoren mit unterschiedlichen Parameterlisten ermöglichen flexible Objekterstellung:

```java
public class Product {
    String name;
    double price;
    int rating;

    Product(String name) {
        this.name   = name;
        this.price  = 0.0;
        this.rating = 1;
    }

    Product(String name, double price) {
        this.name   = name;
        this.price  = price;
        this.rating = 1;
    }

    Product(String name, double price, int rating) {
        this.name   = name;
        this.price  = price;
        this.rating = rating;
    }
}
```

### **[Fortgeschritten]** Konstruktoren mit `this()` wiederverwenden

Statt Code zu duplizieren, kann ein Konstruktor einen anderen aufrufen. `this()` muss die **erste Anweisung** im Konstruktor sein:

```java
public class Product {
    String name;
    double price;
    int rating;

    Product(String name) {
        this(name, 0.0);           // ruft Konstruktor 2 auf
    }

    Product(String name, double price) {
        this(name, price, 1);      // ruft Konstruktor 3 auf
    }

    Product(String name, double price, int rating) {
        this.name   = name;
        this.price  = price;
        this.rating = rating;
    }
}
```

Vorteil: Validierungslogik nur einmal im "Haupt-Konstruktor" schreiben.

### **[Professionell]** Flexible Constructor Bodies `[Java 25]`

Ab Java 25 dürfen vor dem `super()`- oder `this()`-Aufruf Anweisungen stehen, solange sie nicht auf `this` zugreifen (JEP 492):

```java
public class DiscountedProduct extends Product {
    DiscountedProduct(String name, double price) {
        // Java 25: Berechnung VOR super() erlaubt
        var validated = price > 0 ? price : 0.01;
        super(name, validated, 3);
    }
}
```

Vor Java 25 war das ein Kompilierungsfehler.

---

## 2. Kapselung und Unveränderlichkeit

### **[Anfänger]** Kapselung mit Access Modifiers

Felder `private` machen und nur über Methoden zugänglich:

```java
public class Product {
    private String name;
    private double price;

    public String getName()  { return name; }
    public double getPrice() { return price; }

    public void setPrice(double price) {
        if (price >= 0) this.price = price;
    }
}
```

### **[Fortgeschritten]** Unveränderliche Klassen (Immutability)

Eine immutable Klasse kann nach der Erstellung nicht mehr verändert werden:

```java
public final class Product {           // final: keine Subklassen möglich
    private final String name;         // final: nur einmal setzbar
    private final double price;
    private final int rating;

    public Product(String name, double price, int rating) {
        this.name   = name;
        this.price  = price;
        this.rating = rating;
    }

    // Nur Getter, keine Setter
    public String getName()  { return name; }
    public double getPrice() { return price; }
    public int getRating()   { return rating; }

    // "Änderung" erzeugt ein neues Objekt
    public Product withPrice(double newPrice) {
        return new Product(this.name, newPrice, this.rating);
    }
}
```

Vorteile von Immutability:
- **Thread-sicher** ohne Synchronisierung
- **Kein defensives Kopieren** nötig
- **Hashcode stabil** → sicher als Map-Key verwendbar

### **[Professionell]** Records als kompakte immutable Klassen `[Java 16]` → `[Java 17 LTS]`

Records sind syntaktischer Zucker für unveränderliche Datenklassen:

```java
// Ersetzt ~50 Zeilen Boilerplate:
public record Product(String name, double price, int rating) {

    // Kompakter Konstruktor für Validierung
    public Product {
        if (price < 0) throw new IllegalArgumentException("Negativer Preis: " + price);
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating 1–5: " + rating);
    }

    // Zusätzliche Methoden erlaubt
    public double getDiscount() {
        return rating == 5 ? price * 0.2 : 0.0;
    }
}

// Automatisch generiert: Konstruktor, Getter (name(), price(), rating()), equals(), hashCode(), toString()
Product p = new Product("Apple", 0.89, 5);
System.out.println(p.name());     // "Apple"
System.out.println(p);            // Product[name=Apple, price=0.89, rating=5]
```

---

## 3. Enums definieren und verwenden

### **[Anfänger]** Grundlegende Enums

Enums definieren eine feste Menge benannter Konstanten:

```java
public enum Rating {
    ONE_STAR, TWO_STARS, THREE_STARS, FOUR_STARS, FIVE_STARS
}

Rating r = Rating.FIVE_STARS;
System.out.println(r);           // "FIVE_STARS"
System.out.println(r.name());    // "FIVE_STARS"
System.out.println(r.ordinal()); // 4 (Index, 0-basiert)
```

### **[Fortgeschritten]** Enums mit Feldern und Methoden

Enums können wie Klassen Felder, Konstruktoren und Methoden haben:

```java
public enum Rating {
    ONE_STAR(1, 0.0),
    TWO_STARS(2, 0.0),
    THREE_STARS(3, 0.0),
    FOUR_STARS(4, 0.1),
    FIVE_STARS(5, 0.2);

    private final int stars;
    private final double discount;

    Rating(int stars, double discount) {
        this.stars    = stars;
        this.discount = discount;
    }

    public int getStars()       { return stars; }
    public double getDiscount() { return discount; }

    public String toStarString() {
        return "*".repeat(stars);
    }
}

Rating r = Rating.FOUR_STARS;
System.out.println(r.toStarString());  // "****"
System.out.println(r.getDiscount());   // 0.1
```

### **[Professionell]** Enums mit abstrakten Methoden und Switch-Integration `[Java 21]`

```java
public enum Rating {
    ONE_STAR   { @Override public String describe() { return "Enttäuschend";  } },
    TWO_STARS  { @Override public String describe() { return "Unterdurchschnittlich"; } },
    THREE_STARS{ @Override public String describe() { return "Durchschnittlich"; } },
    FOUR_STARS { @Override public String describe() { return "Gut";           } },
    FIVE_STARS { @Override public String describe() { return "Ausgezeichnet"; } };

    public abstract String describe();
}

// Pattern Matching für Enums [Java 21]
Rating r = Rating.FIVE_STARS;
String msg = switch (r) {
    case FIVE_STARS -> "Empfehlung des Hauses!";
    case FOUR_STARS -> "Sehr empfehlenswert";
    case Rating rr when rr.ordinal() < 2 -> "Nicht empfehlenswert";
    default -> "Neutral";
};
```

---

## 4. Speicherverwaltung und Parameterübergabe

### **[Anfänger]** Stack und Heap

| Bereich | Inhalt | Lebensdauer |
|---|---|---|
| **Stack** | Lokale Variablen, Methodenaufrufe, primitive Werte | Bis Methode endet |
| **Heap** | Alle Objekte (`new`), statische Felder | Bis Garbage Collector sie entfernt |

```java
void methode() {
    int x = 42;              // Stack: primitiver Wert
    Product p = new Product(); // Stack: Referenz p; Heap: Product-Objekt
}
// Methode endet: x und p vom Stack entfernt; Product-Objekt bleibt (noch)
```

### **[Fortgeschritten]** Java ist immer "Pass by Value"

Java übergibt immer **Kopien** — bei Objekten eine Kopie der **Referenz**, nicht des Objekts:

```java
void verdoppelPreis(Product p) {
    p.setPrice(p.getPrice() * 2);  // Wirkt! Wir arbeiten am echten Objekt
}

void ersetzProdukt(Product p) {
    p = new Product("Neu", 1.0, 1); // Wirkt NICHT nach außen: nur lokale Kopie geändert
}

Product apple = new Product("Apple", 1.0, 5);
verdoppelPreis(apple);
System.out.println(apple.getPrice()); // 2.0 (Objekt wurde verändert)

ersetzProdukt(apple);
System.out.println(apple.getName());  // "Apple" (Referenz außen unverändert)
```

### **[Professionell]** Garbage Collection und `finalize()`

Der **Garbage Collector (GC)** gibt Speicher automatisch frei, wenn keine Referenz mehr auf ein Objekt zeigt:

```java
Product p = new Product("Apple", 0.89, 5);
p = null;  // Objekt hat jetzt keine Referenz mehr → GC kann es freigeben

// System.gc() ist nur ein Hinweis — keine Garantie
System.gc();
```

Wichtige Punkte:
- `finalize()` ist **deprecated** seit Java 9 — nicht mehr verwenden
- **WeakReference / SoftReference**: Objekte die der GC bei Bedarf freigeben darf
- Moderne GC-Algorithmen: G1GC (Standard), ZGC (low-latency), Shenandoah

```java
import java.lang.ref.WeakReference;

Product p = new Product("Temp", 1.0, 1);
WeakReference<Product> ref = new WeakReference<>(p);
p = null;
// ref.get() kann nach GC null zurückgeben
```

---

## 5. Methoden überladen (Method Overloading)

### **[Anfänger]** Grundregeln der Methoden-Überladung

Mehrere Methoden dürfen denselben Namen haben, wenn sie sich in der **Parameterliste** unterscheiden (Anzahl, Typen oder Reihenfolge der Parameter). Der Rückgabetyp allein reicht nicht zur Unterscheidung:

```java
public class Calculator {
    // Gleicher Name, unterschiedliche Parametertypen
    int add(int a, int b)        { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; } // unterschiedliche Anzahl

    // FEHLER – nur Rückgabetyp unterscheidet sich (kein gültiges Überladen):
    // double add(int a, int b) { return a + b; }
}

Calculator calc = new Calculator();
System.out.println(calc.add(1, 2));        // 3   – int-Version
System.out.println(calc.add(1.5, 2.5));    // 4.0 – double-Version
System.out.println(calc.add(1, 2, 3));     // 6   – drei-Parameter-Version
```

Java wählt die passende Methode zur **Kompilierzeit** anhand der Argumenttypen (statisches Dispatch).

### **[Fortgeschritten]** Überladungsauflösung und Typpromotion

Gibt es keine exakte Übereinstimmung, versucht Java automatisch Typen zu erweitern (widening):

```java
public class Printer {
    void print(int n)    { System.out.println("int: " + n);    }
    void print(double d) { System.out.println("double: " + d); }
    void print(Object o) { System.out.println("Object: " + o); }
}

Printer p = new Printer();
p.print(42);       // "int: 42"      – exakte Übereinstimmung
p.print(3.14);     // "double: 3.14" – exakte Übereinstimmung
byte b = 5;
p.print(b);        // "int: 5"       – byte → int (widening)
p.print("Hallo");  // "Object: Hallo"– String passt zu Object
```

Wichtige Regeln:
- Widening (z.B. `int` → `long` → `double`) hat Vorrang vor Autoboxing (`int` → `Integer`).
- Autoboxing hat Vorrang vor Varargs.
- Bei Mehrdeutigkeit gibt es einen Kompilierungsfehler.

### **[Professionell]** Überladung in der Praxis und Fallstricke

```java
public class Formatter {
    String format(String s, int repeat) {
        return s.repeat(repeat);
    }

    String format(String s, String separator) {
        return s + separator;
    }

    // Überladung mit null ist mehrdeutig:
    // format(null, 2)   → String-Version (null passt zu String)
    // format(null, null) → KOMPILIERUNGSFEHLER (mehrdeutig)
}

// Konstruktoren überladen folgt denselben Regeln wie Methoden:
public class Product {
    Product(String name)                      { this(name, 0.0, 1); }
    Product(String name, double price)        { this(name, price, 1); }
    Product(String name, double price, int r) { /* Haupt-Konstruktor */ }
}
```

Merkhilfe für die Prüfung: Überladen = gleicher Name, **andere Signatur** (Parametertypen/-anzahl/-reihenfolge). Rückgabetyp und `throws`-Klausel zählen **nicht** zur Signatur.

---

## 6. Varargs-Methoden

### **[Anfänger]** Varargs – variable Argumentanzahl

Mit `...` (Ellipsis) nimmt eine Methode eine beliebige Anzahl von Argumenten desselben Typs entgegen. Intern wird daraus ein Array:

```java
public class Stats {
    int sum(int... numbers) {        // "numbers" ist ein int[]
        int total = 0;
        for (int n : numbers) total += n;
        return total;
    }
}

Stats s = new Stats();
System.out.println(s.sum());            // 0   – null Argumente
System.out.println(s.sum(1, 2, 3));     // 6
System.out.println(s.sum(10, 20, 30, 40)); // 100

// Auch ein bestehendes Array darf übergeben werden:
int[] arr = {5, 10, 15};
System.out.println(s.sum(arr));         // 30
```

### **[Fortgeschritten]** Varargs-Regeln und Einschränkungen

```java
public class Logger {
    // Varargs muss der LETZTE Parameter sein:
    void log(String level, String... messages) {
        for (String msg : messages) {
            System.out.println("[" + level + "] " + msg);
        }
    }

    // Länge und null-Sicherheit prüfen:
    double average(double... values) {
        if (values == null || values.length == 0) return 0.0;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }
}

Logger log = new Logger();
log.log("INFO", "Start", "Running", "Done");  // drei Meldungen
log.log("WARN");                               // Varargs-Array ist leer (length == 0)

// FEHLER – nur ein Varargs-Parameter erlaubt, muss am Ende stehen:
// void wrong(int... a, String... b) { }   // Kompilierungsfehler
// void wrong(int... a, String b)    { }   // Kompilierungsfehler
```

Wichtige Einschränkungen:
- Pro Methode ist nur **ein** Varargs-Parameter erlaubt.
- Varargs muss der **letzte** Parameter der Methode sein.
- Varargs und Überladung können mehrdeutig werden – der Compiler warnt.

### **[Professionell]** Varargs, Überladung und Heap Pollution

```java
public class Combiner {
    // Wenn Varargs und eine Überladung ohne Varargs existieren,
    // hat die exakte Überladung Vorrang:
    String join(String a, String b)    { return a + "-" + b; }       // (1)
    String join(String... parts)       { return String.join("-", parts); } // (2)

    // join("x", "y") ruft (1) auf – exakte Übereinstimmung hat Vorrang
    // join("x", "y", "z") ruft (2) auf
}

// Heap Pollution: Generics + Varargs können unsichere Situationen erzeugen.
// @SafeVarargs unterdrückt die Warnung, wenn die Methode typsicher ist:
@SafeVarargs
static <T> java.util.List<T> listOf(T... items) {
    return java.util.Arrays.asList(items);
}

java.util.List<String> names = listOf("Alice", "Bob", "Charlie");
System.out.println(names); // [Alice, Bob, Charlie]
```

Merkhilfe für die Prüfung: Varargs = letzter Parameter + `Type...` + wird intern zum Array. Überladungsauflösung bevorzugt exakte Methoden vor Varargs-Methoden.

---

## Übungsaufgaben

### **[Anfänger]** Practice 5-1: Create Enumeration to Represent Product Rating (ca. 11 Minuten)

**Ziel:** Ein Enum für Produktbewertungen erstellen und verwenden.

1. Erstelle ein Enum `Rating` mit den Werten `ONE_STAR` bis `FIVE_STARS`.
2. Füge jedem Enum-Wert ein Feld `stars` (int, 1–5) und einen Konstruktor hinzu.
3. Ersetze in der `Product`-Klasse das Feld `int rating` durch `Rating rating`.
4. Gib in `main()` alle Enum-Werte mit einer for-each-Schleife aus:
   ```java
   for (Rating r : Rating.values()) {
       System.out.println(r + " = " + r.getStars() + " Sterne");
   }
   ```
5. Erstelle ein Produkt mit `Rating.FIVE_STARS` und gib dessen Bewertung aus.

---

### **[Fortgeschritten]** Practice 5-2: Add Custom Constructors to the Product Class (ca. 20 Minuten)

**Ziel:** Konstruktoren sinnvoll überladen und `this()` zur Code-Wiederverwendung einsetzen.

1. Füge der `Product`-Klasse drei Konstruktoren hinzu:
   - Nur `name` → Preis = 0.0, Rating = `ONE_STAR`
   - `name` + `price` → Rating = `ONE_STAR`
   - `name` + `price` + `Rating` → vollständig
2. Verwende `this(...)` um Duplikation zu vermeiden — nur der dritte Konstruktor darf direkt Felder setzen.
3. Validierung ausschließlich im dritten Konstruktor: Preis ≥ 0, Rating nicht null.
4. Teste alle drei Konstruktoren und gib die Objekte aus.
5. **Erweiterung:** Füge eine statische Factory-Methode `Product.of(String name)` hinzu.

---

### **[Professionell]** Practice 5-3: Make Product Objects Immutable (ca. 10 Minuten)

**Ziel:** Die `Product`-Klasse vollständig unveränderlich machen.

1. Markiere die Klasse als `final`.
2. Mache alle Felder `private final`.
3. Entferne alle Setter-Methoden.
4. Füge `with`-Methoden hinzu, die ein neues Objekt zurückgeben:
   ```java
   public Product withRating(Rating newRating) {
       return new Product(this.name, this.price, newRating);
   }
   ```
5. Implementiere `equals()`, `hashCode()` und `toString()` korrekt.
6. **Bonus:** Ersetze die gesamte Klasse durch einen `record` und vergleiche den Code-Umfang.
7. **Bonus `[Java 25]`:** Teste Flexible Constructor Bodies: Führe vor dem `super()`-Aufruf in einer Subklasse eine Berechnung durch.

---

## Multiple-Choice-Fragen

### [Anfänger]

**Frage 1:** Was ist ein Enum in Java?

- A) Eine Unterklasse von `int`
- B) Ein Interface mit konstanten Methoden
- C) **Eine Klasse mit einer festen, benannten Menge von Konstanten** ✓
- D) Ein primitiver Datentyp

---

**Frage 2:** Was gibt `Rating.FIVE_STARS.ordinal()` zurück, wenn `FIVE_STARS` der letzte von 5 Werten ist?

- A) 5
- B) **4** ✓
- C) 1
- D) "FIVE_STARS"

> *`ordinal()` gibt den 0-basierten Index zurück.*

---

**Frage 3:** Was ist der Unterschied zwischen Stack und Heap?

- A) Stack für Objekte, Heap für primitive Typen
- B) **Stack für lokale Variablen und Methodenaufrufe, Heap für alle Objekte** ✓
- C) Stack ist größer als Heap
- D) Heap wird nach jeder Methode geleert

---

### [Fortgeschritten]

**Frage 4:** Was muss bei der Verwendung von `this()` in einem Konstruktor beachtet werden?

- A) `this()` darf nur im letzten Konstruktor stehen
- B) `this()` kann überall in einem Konstruktor stehen
- C) **`this()` muss die erste Anweisung im Konstruktor sein** ✓
- D) `this()` ruft den Konstruktor der Superklasse auf

---

**Frage 5:** Was ist der Vorteil einer immutable Klasse?

- A) Sie verbraucht weniger Speicher
- B) Sie läuft immer schneller
- C) Sie kann vererbt werden
- D) **Sie ist von Natur aus thread-sicher und kann gefahrlos als Map-Key verwendet werden** ✓

---

**Frage 6:** Was beschreibt "Pass by Value" in Java bei Objektreferenzen?

- A) Das Objekt selbst wird kopiert und übergeben
- B) Änderungen am Parameter haben nie Auswirkungen auf das Original
- C) **Eine Kopie der Referenz wird übergeben — Änderungen am Objekt wirken, Neuzuweisung der Referenz nicht** ✓
- D) Primitive und Objekte werden gleich behandelt

---

### [Professionell]

**Frage 7:** Was ist ein Record in Java? `[Java 16]`

- A) Eine Klasse ohne Konstruktor
- B) Ein Interface für Datenklassen
- C) **Eine kompakte, automatisch immutable Datenklasse mit generierten Getter, equals, hashCode und toString** ✓
- D) Ein Enum mit Methoden

---

**Frage 8:** Was erlaubt Flexible Constructor Bodies in Java 25? `[Java 25]`

- A) Konstruktoren ohne Parameter zu definieren
- B) `super()` wegzulassen
- C) Mehrere `this()`-Aufrufe in einem Konstruktor
- D) **Anweisungen vor dem `super()`- oder `this()`-Aufruf, solange nicht auf `this` zugegriffen wird** ✓

---

**Frage 9:** Welche Aussage über Garbage Collection ist korrekt?

- A) `System.gc()` garantiert sofortige Speicherfreigabe
- B) Objekte werden freigegeben, sobald eine Methode endet
- C) **Objekte werden freigegeben, sobald keine erreichbare Referenz mehr auf sie zeigt** ✓
- D) `finalize()` ist die empfohlene Methode zur Ressourcenfreigabe

---

**Frage 10:** Welche der folgenden Methoden-Definitionen stellt ein gültiges Überladen dar?

- A) `int add(int a, int b)` und `double add(int a, int b)` (nur Rückgabetyp verschieden)
- B) `void print(String s)` und `void print(String s)` (identisch)
- C) **`void print(int n)` und `void print(double d)` (unterschiedliche Parametertypen)** ✓
- D) `int add(int a, int b)` und `int add(int b, int a)` (nur Parameternamen vertauscht)

> *Nur Unterschiede in Parametertypen, -anzahl oder -reihenfolge (bei verschiedenen Typen) sind gültig. Rückgabetyp und Parameternamen zählen nicht.*

---

**Frage 11:** In welcher Reihenfolge versucht Java Methoden-Überladung aufzulösen?

- A) Autoboxing → Widening → Varargs
- B) Varargs → Widening → Autoboxing
- C) **Widening → Autoboxing → Varargs** ✓
- D) Autoboxing → Varargs → Widening

> *Java bevorzugt Widening vor Autoboxing, und Autoboxing vor Varargs.*

---

**Frage 12:** Welche Regel gilt für Varargs-Parameter?

- A) Eine Methode darf beliebig viele Varargs-Parameter haben
- B) Varargs kann an beliebiger Position in der Parameterliste stehen
- C) Varargs ist immer mindestens 1 Element lang
- D) **Eine Methode darf höchstens einen Varargs-Parameter haben, und dieser muss der letzte sein** ✓

---

**Frage 13:** Was trifft auf den Aufruf `log("INFO")` zu, wenn die Signatur `void log(String level, String... messages)` lautet?

- A) Kompilierungsfehler, da `messages` mindestens einen Wert braucht
- B) Laufzeitfehler (NullPointerException)
- C) **Gültig – `messages` ist ein leeres Array (length == 0)** ✓
- D) Gültig – `messages` ist `null`

---

**Frage 14:** Was bewirkt `@SafeVarargs` bei einer Varargs-Methode mit generischem Typ?

- A) Es erzwingt zur Laufzeit Typprüfungen
- B) Es verbietet die Übergabe von Arrays
- C) Es macht die Methode automatisch thread-sicher
- D) **Es unterdrückt die Compiler-Warnung über potenzielle Heap Pollution bei typsicherer Implementierung** ✓

---

## Skill Check: Typische Prüfungsfragen

### [Anfänger]
1. Was ist der Unterschied zwischen einem Konstruktor und einer Methode?
2. Was ist ein Enum und wann setzt man ihn ein?
3. Was passiert mit einem Objekt, wenn die Methode endet, die es erstellt hat?
4. Was ist Method Overloading und welche Bedingungen müssen erfüllt sein?
5. Was ist ein Varargs-Parameter und wie deklariert man ihn?

### [Fortgeschritten]
6. Warum ist `this()` nützlich beim Überladen von Konstruktoren?
7. Was sind die vier Eigenschaften einer immutable Klasse?
8. Was bedeutet "Java ist Pass by Value" — auch für Objekte?
9. In welcher Reihenfolge löst Java Überladung auf (Widening, Autoboxing, Varargs)?
10. Welche Einschränkungen gelten für Varargs-Parameter in einer Methodensignatur?

### [Professionell]
11. Was ist der Unterschied zwischen einem Record und einer immutable Klasse mit `final`-Feldern?
12. Was ist der Vorteil von Flexible Constructor Bodies in Java 25 gegenüber früheren Versionen?
13. Erkläre den Unterschied zwischen `WeakReference`, `SoftReference` und einer normalen starken Referenz.
14. Warum kann die Kombination aus Varargs und Überladung problematisch sein? Nenne ein Beispiel.
15. Was bedeutet Heap Pollution im Kontext von Generics und Varargs, und wie verhindert `@SafeVarargs` die Warnung?
