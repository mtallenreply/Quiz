# Modul 4: Classes and Objects

## Übersicht

Dieses Modul ist der Kern der objektorientierten Programmierung in Java. Es erklärt, wie Klassen entworfen werden, wie Objekte erstellt und verwendet werden, und wie Javadoc zur Dokumentation des eigenen Codes eingesetzt wird. Das durchgängige Praxisbeispiel ist eine **Product Management Application**.

| Thema | Dauer |
|---|---|
| Java Classes and Objects: Part 1 | 19m |
| Java Classes and Objects: Part 2 | 20m |
| Java Classes and Objects: Part 3 | 10m |
| Java Classes and Objects: Part 4 | 12m |
| Practice 4-1: Create the Product Management Application | 27m |
| Practice 4-2: Enhance the Product Class | 16m |
| Practice 4-3: Document Classes | 10m |
| Skill Check: Classes and Objects (mind. 80%) | — |

---

## 1. Java Classes and Objects: Part 1 — Grundlagen

### Was ist eine Klasse?

Eine Klasse ist eine **Blaupause** (Blueprint) für Objekte. Sie definiert:
- **Felder (Fields/Attributes):** Die Daten, die ein Objekt enthält
- **Methoden (Methods):** Das Verhalten eines Objekts
- **Konstruktoren:** Wie ein Objekt erzeugt wird

```java
// Klasse definieren
public class Product {
    // Felder (Instanzvariablen)
    String name;
    double price;
    int rating;

    // Methode
    void printInfo() {
        System.out.println("Produkt: " + name + ", Preis: " + price);
    }
}
```

### Was ist ein Objekt?

Ein Objekt ist eine **Instanz** einer Klasse — ein konkretes Exemplar der Blaupause.

```java
// Objekt erzeugen mit new
Product apple = new Product();
apple.name  = "Apple";
apple.price = 0.89;
apple.rating = 5;

Product coffee = new Product();
coffee.name  = "Coffee";
coffee.price = 4.99;

// Methode aufrufen
apple.printInfo();   // "Produkt: Apple, Preis: 0.89"
coffee.printInfo();  // "Produkt: Coffee, Preis: 4.99"
```

### Referenzen vs. Werte

Objekte werden nicht direkt gespeichert — es werden **Referenzen** (Zeiger) auf den Speicherbereich gespeichert:

```java
Product a = new Product();
a.name = "Apple";

Product b = a;       // b zeigt auf DASSELBE Objekt wie a
b.name = "Birne";

System.out.println(a.name);  // "Birne" ← beide Variablen zeigen auf dasselbe Objekt!
```

---

## 2. Java Classes and Objects: Part 2 — Felder und Methoden im Detail

### Instanzfelder vs. statische Felder

```java
public class Product {
    // Instanzfeld: jedes Objekt hat seinen eigenen Wert
    String name;
    double price;

    // Statisches Feld: wird von ALLEN Instanzen geteilt
    static int productCount = 0;
}

Product p1 = new Product();
Product p2 = new Product();
Product.productCount = 10; // Zugriff über den Klassennamen (empfohlen)
```

### Instanzmethoden vs. statische Methoden

```java
public class Product {
    String name;
    double price;

    // Instanzmethode: kann auf Instanzfelder zugreifen
    double getPriceWithTax(double taxRate) {
        return price * (1 + taxRate);
    }

    // Statische Methode: kein Zugriff auf Instanzfelder
    static String formatPrice(double price) {
        return String.format("%.2f €", price);
    }
}

// Verwendung
Product p = new Product();
p.price = 10.0;
p.getPriceWithTax(0.19);       // Instanzmethode → über Objekt aufrufen
Product.formatPrice(10.0);     // Statische Methode → über Klasse aufrufen
```

### Methoden mit Rückgabewert und Parametern

```java
public class Product {
    String name;
    double price;

    // Rückgabetyp void: kein Rückgabewert
    void setPrice(double newPrice) {
        price = newPrice;
    }

    // Rückgabetyp String: gibt einen String zurück
    String getDescription() {
        return name + " (" + price + " €)";
    }

    // Mehrere Parameter
    double calculateDiscount(double discountPercent, boolean applyMin) {
        double discount = price * discountPercent / 100;
        if (applyMin && discount < 1.0) {
            discount = 1.0;
        }
        return discount;
    }
}
```

### `this`-Referenz

`this` verweist auf das aktuelle Objekt und wird gebraucht, wenn Parameternames mit Feldnamen kollidieren:

```java
public class Product {
    String name;
    double price;

    void setName(String name) {
        this.name = name;  // this.name = Instanzfeld, name = Parameter
    }

    // this für Method Chaining
    Product withName(String name) {
        this.name = name;
        return this;  // gibt das aktuelle Objekt zurück
    }

    Product withPrice(double price) {
        this.price = price;
        return this;
    }
}

// Method Chaining
Product p = new Product()
    .withName("Kaffee")
    .withPrice(4.99);
```

---

## 3. Java Classes and Objects: Part 3 — Konstruktoren

### Default-Konstruktor

Wenn kein Konstruktor definiert wird, erzeugt Java automatisch einen parameterlosen Konstruktor:

```java
public class Product {
    String name;
    double price;
    // Java fügt automatisch hinzu: public Product() {}
}

Product p = new Product(); // OK: Default-Konstruktor
```

### Eigene Konstruktoren

```java
public class Product {
    String name;
    double price;
    int rating;

    // Parametrisierter Konstruktor
    Product(String name, double price, int rating) {
        this.name   = name;
        this.price  = price;
        this.rating = rating;
    }
}

// Verwendung
Product apple = new Product("Apple", 0.89, 5);
```

> Sobald ein eigener Konstruktor definiert ist, existiert der Default-Konstruktor NICHT mehr automatisch!

### Konstruktoren überladen (Overloading)

```java
public class Product {
    String name;
    double price;
    int rating;

    // Konstruktor 1: nur Name
    Product(String name) {
        this.name   = name;
        this.price  = 0.0;
        this.rating = 1;
    }

    // Konstruktor 2: Name und Preis
    Product(String name, double price) {
        this.name   = name;
        this.price  = price;
        this.rating = 1;
    }

    // Konstruktor 3: alles
    Product(String name, double price, int rating) {
        this.name   = name;
        this.price  = price;
        this.rating = rating;
    }
}
```

### Initialisierungsreihenfolge

```java
public class Product {
    // 1. Statische Felder werden zuerst initialisiert
    static int count = 0;

    // 2. Instanzfelder (mit Defaultwerten oder Initializer)
    String name = "Unbekannt";
    double price;

    // 3. Konstruktor wird ausgeführt
    Product(String name, double price) {
        this.name  = name;
        this.price = price;
        count++;
    }
}
```

---

## 4. Java Classes and Objects: Part 4 — Kapselung und Zugriffskontrolle

### Access Modifier (Zugriffsmodifikatoren)

| Modifier | Gleiche Klasse | Gleiches Package | Subklasse | Überall |
|---|:---:|:---:|:---:|:---:|
| `private` | ✓ | — | — | — |
| (package) | ✓ | ✓ | — | — |
| `protected` | ✓ | ✓ | ✓ | — |
| `public` | ✓ | ✓ | ✓ | ✓ |

### Kapselung: private Felder mit Getter/Setter

Das Prinzip der Kapselung besagt: **Felder sollten privat sein** und nur über kontrollierte Methoden zugänglich:

```java
public class Product {
    private String name;
    private double price;
    private int rating;

    // Konstruktor
    public Product(String name, double price, int rating) {
        this.name   = name;
        setPrice(price);    // Validierung im Setter nutzen
        setRating(rating);
    }

    // Getter
    public String getName()  { return name; }
    public double getPrice() { return price; }
    public int getRating()   { return rating; }

    // Setter mit Validierung
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Preis darf nicht negativ sein: " + price);
        }
        this.price = price;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating muss zwischen 1 und 5 liegen: " + rating);
        }
        this.rating = rating;
    }
}
```

### toString() überschreiben

`toString()` wird automatisch aufgerufen, wenn ein Objekt als String benötigt wird (z.B. in `println`):

```java
public class Product {
    private String name;
    private double price;

    @Override
    public String toString() {
        return String.format("Product[name='%s', price=%.2f]", name, price);
    }
}

Product p = new Product("Apple", 0.89, 5);
System.out.println(p);  // "Product[name='Apple', price=0.89]"
```

### equals() und hashCode()

```java
public class Product {
    private String name;
    private double price;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        // Pattern Matching [Java 16]: instanceof + Cast in einem Schritt
        if (!(obj instanceof Product other)) return false;
        return Double.compare(price, other.price) == 0
            && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price);
    }
}
```

> `equals()` und `hashCode()` sollten immer zusammen überschrieben werden!

### Der `instanceof`-Operator mit Pattern Matching `[Java 16]`

> Finalisiert in Java 16, seit **Java 17 LTS** `[Java 17]` offiziell produktionsreif.

```java
Object obj = new Product("Apple", 0.89, 5);

// Klassisch (vor Java 16): Typ prüfen + manuell casten
if (obj instanceof Product) {
    Product p = (Product) obj;
    System.out.println(p.getName());
}

// Pattern Matching [Java 16]: Prüfen und benennen in einem Schritt
if (obj instanceof Product p) {
    System.out.println(p.getName()); // p ist direkt ohne Cast verfügbar
}
```

### Unnamed Pattern Variable `[Java 22]`

Wenn nur der Typ geprüft werden soll, ohne die Variable zu benennen, kann `_` verwendet werden (JEP 456):

```java
Object obj = new Product("Apple", 0.89, 5);

// _ statt Variablenname: Typ prüfen, Wert nicht weiterverwenden
if (obj instanceof Product _) {
    System.out.println("Ist ein Produkt");
}
```

---

## 5. Javadoc — Klassen dokumentieren

Javadoc-Kommentare ermöglichen es, HTML-Dokumentation automatisch aus dem Quellcode zu generieren.

### Syntax

```java
/**
 * Repräsentiert ein Produkt im Shop-System.
 *
 * <p>Ein Produkt hat einen Namen, einen Preis und eine Bewertung.
 * Bewertungen liegen zwischen 1 (schlecht) und 5 (sehr gut).</p>
 *
 * @author Max Mustermann
 * @version 1.0
 * @since Java SE 25
 */
public class Product {

    /**
     * Der Name des Produkts.
     */
    private String name;

    /**
     * Der Preis des Produkts in Euro. Muss größer oder gleich 0 sein.
     */
    private double price;

    /**
     * Erstellt ein neues Produkt mit dem angegebenen Namen und Preis.
     *
     * @param name  der Name des Produkts (darf nicht null sein)
     * @param price der Preis in Euro (muss >= 0 sein)
     * @throws IllegalArgumentException wenn der Preis negativ ist
     */
    public Product(String name, double price) {
        // ...
    }

    /**
     * Gibt den Preis mit Mehrwertsteuer zurück.
     *
     * @param taxRate der Steuersatz als Dezimalwert (z.B. 0.19 für 19%)
     * @return der Bruttopreis
     */
    public double getPriceWithTax(double taxRate) {
        return price * (1 + taxRate);
    }
}
```

### Wichtige Javadoc-Tags

| Tag | Bedeutung |
|---|---|
| `@param name beschreibung` | Parameter einer Methode |
| `@return beschreibung` | Rückgabewert |
| `@throws ExceptionType beschreibung` | Geworfene Ausnahme |
| `@author name` | Autor |
| `@version version` | Version |
| `@since version` | Ab welcher Version verfügbar |
| `@see Klassenname` | Verweis auf andere Klasse/Methode |
| `{@code code}` | Code inline im Kommentar |
| `{@link Klasse#methode}` | Klickbarer Link im generierten HTML |

### Markdown-Dokumentationskommentare `[Java 23]`

Ab Java 23 können Javadoc-Kommentare auch in **Markdown** geschrieben werden (JEP 467). Statt `/**` wird `///` (dreifacher Schrägstrich) verwendet:

```java
/// Repräsentiert ein Produkt im Shop-System.
///
/// Ein Produkt hat einen Namen, einen Preis und eine Bewertung.
/// Bewertungen liegen zwischen **1** (schlecht) und **5** (sehr gut).
///
/// Beispiel:
/// ```java
/// Product p = new Product("Apple", 0.89, 5);
/// System.out.println(p.getName()); // "Apple"
/// ```
public class Product {

    /// Der Preis in Euro. Muss größer oder gleich 0 sein.
    private double price;

    /// Gibt den Preis mit Mehrwertsteuer zurück.
    ///
    /// @param taxRate der Steuersatz als Dezimalwert (z.B. `0.19` für 19%)
    /// @return der Bruttopreis
    public double getPriceWithTax(double taxRate) {
        return price * (1 + taxRate);
    }
}
```

Vorteile gegenüber klassischen `/** */`-Kommentaren:
- Kein HTML (`<p>`, `<pre>`, `<code>`) nötig
- **Fett**, *Kursiv*, Listen und Codeblöcke direkt in Markdown
- Besser lesbar direkt im Quellcode

### Javadoc generieren

```bash
javadoc -d docs/api src/main/java/shop/*.java
```

---

## 6. Lokale Typinferenz mit `var` `[Java 10]` — [Anfänger]

Seit Java 10 kann der Typ einer lokalen Variable mit `var` deklariert werden. Der Compiler leitet den Typ automatisch aus dem Initializer ab. Das erhöht die Lesbarkeit, ohne Typsicherheit zu verlieren — der Typ wird zur Compile-Zeit festgelegt, nicht zur Laufzeit.

`var` funktioniert ausschließlich für **lokale Variablen mit Initializer**. Felder, Methodenparameter und Rückgabetypen können nicht mit `var` deklariert werden.

```java
// Ohne var: Typ wird explizit geschrieben
Product product = new Product("Apple", 0.89, 5);
String name = product.getName();
double price = product.getPrice();

// Mit var: Typ wird aus dem Initializer abgeleitet
var product = new Product("Apple", 0.89, 5);  // Typ: Product
var name    = product.getName();               // Typ: String
var price   = product.getPrice();             // Typ: double
```

```java
// var in for-Schleifen
var products = new Product[3];
products[0] = new Product("Apple", 0.89, 5);

for (var p : products) {           // Typ: Product
    System.out.println(p);
}

// var bei komplexen generischen Typen — spart Tipparbeit
var productMap = new java.util.HashMap<String, Product>(); // Typ bleibt HashMap<String,Product>
productMap.put("apple", new Product("Apple", 0.89, 5));
```

> **Wichtige Regeln:** `var` braucht immer einen Initializer (`var x;` ist ein Kompilierungsfehler). `var` kann nicht `null` sein als einziger Initializer. `var` ist ein reservierter Typname, kein Keyword — Variablen können weiterhin `var` heißen (aber das vermeiden!).

---

## 7. Unbenannte Variablen mit `_` `[Java 22]` — [Fortgeschritten]

Seit Java 22 (JEP 456) kann `_` (Unterstrich) als **unbenannte Variable** verwendet werden, wenn eine Variable syntaktisch erforderlich ist, ihr Wert aber nicht gebraucht wird. Dies verbessert die Lesbarkeit und signalisiert explizit, dass ein Wert bewusst ignoriert wird.

Unbenannte Variablen unterscheiden sich von Unnamed Patterns (bereits in Abschnitt 4 gezeigt): Sie können in **lokalen Variablendeklarationen**, **for-Schleifen**, **catch-Blöcken** und **Lambda-Parametern** eingesetzt werden.

```java
// Catch-Block: Exception-Objekt wird nicht gebraucht
try {
    int value = Integer.parseInt("keine Zahl");
} catch (NumberFormatException _) {
    System.out.println("Ungültige Eingabe — Exception-Objekt nicht relevant");
}

// for-Schleife: Laufvariable wird nicht gebraucht
for (var _ : products) {
    count++;  // Nur zählen, Produktwert ignoriert
}

// Lambda: ein Parameter wird nicht verwendet
products.forEach(_ -> System.out.println("Ein Produkt verarbeitet"));

// Mehrere unbenannte Variablen im gleichen Scope sind erlaubt
// (normale Variablen dürften denselben Namen nicht zweimal haben)
try { doA(); } catch (Exception _) { }
try { doB(); } catch (Exception _) { }
```

```java
// Unnamed Pattern Variable bei instanceof (schon in Abschnitt 4)
if (obj instanceof Product _) {
    System.out.println("Ist ein Produkt — Wert nicht gebraucht");
}

// Kombination mit switch (Unnamed Pattern)
switch (obj) {
    case Integer i  -> System.out.println("Zahl: " + i);
    case String _   -> System.out.println("Text ignoriert");
    default         -> System.out.println("Anderer Typ");
}
```

> **Merkhilfe:** `_` als Name ist ein Signal an den Leser: *"Dieser Wert existiert, wird aber bewusst nicht genutzt."* Vor Java 22 war `_` seit Java 9 als Identifier verboten; ab Java 22 hat er diese spezielle Bedeutung.

---

## 8. Instanz-Initialisierungsblöcke — [Fortgeschritten]

Ein **Instanz-Initialisierungsblock** ist ein Block `{ ... }` direkt im Klassenrumpf (ohne Methodenname). Er wird **bei jeder Objekterstellung** ausgeführt — unmittelbar vor dem Konstruktor-Body, aber nach der Feldinitalisierung.

Instanz-Initialisierungsblöcke sind nützlich, wenn mehrere Konstruktoren denselben Initialisierungscode teilen sollen, ohne einen gemeinsamen Hilfskonstruktor zu verwenden.

```java
public class Product {
    private String name;
    private double price;
    private int rating;
    private String createdAt;

    // Instanz-Initialisierungsblock: wird bei JEDER Objekterstellung ausgeführt
    {
        createdAt = java.time.LocalDate.now().toString();
        System.out.println("Instanz-Block ausgeführt");
    }

    // Konstruktor 1
    public Product(String name, double price) {
        this.name  = name;
        this.price = price;
        this.rating = 1;
        System.out.println("Konstruktor 1 ausgeführt");
    }

    // Konstruktor 2
    public Product(String name, double price, int rating) {
        this.name   = name;
        this.price  = price;
        this.rating = rating;
        System.out.println("Konstruktor 2 ausgeführt");
    }
}

// Ausgabe bei: new Product("Apple", 0.89):
// "Instanz-Block ausgeführt"
// "Konstruktor 1 ausgeführt"
```

**Reihenfolge der Initialisierung bei einem neuen Objekt:**

1. Statische Felder und statische Blöcke (nur beim ersten Laden der Klasse)
2. Instanzfelder in Deklarationsreihenfolge
3. Instanz-Initialisierungsblöcke in Textreihenfolge
4. Konstruktor-Body

```java
public class Demo {
    int a = 10;                  // Schritt 2: Feld

    { a = 20; }                  // Schritt 3: Instanz-Block überschreibt

    Demo() { a = 30; }           // Schritt 4: Konstruktor überschreibt

    public static void main(String[] args) {
        System.out.println(new Demo().a); // 30
    }
}
```

> Instanz-Initialisierungsblöcke sind in modernem Java selten — oft ist ein gemeinsamer privater Hilfskonstruktor (`this(...)`) eleganter. Sie sind aber ein Prüfungsthema und kommen in anonymen Klassen vor, die keine Konstruktoren haben können.

---

## 9. Statische Initialisierungsblöcke — [Fortgeschritten]

Ein **statischer Initialisierungsblock** (`static { ... }`) wird **einmalig** ausgeführt, wenn die Klasse zum ersten Mal vom ClassLoader geladen wird — vor dem Erstellen irgendeines Objekts. Er eignet sich für komplexe Initialisierungslogik statischer Felder, die sich nicht in einem einfachen Feldausdruck ausdrücken lässt.

```java
public class ProductCatalog {
    private static final java.util.Map<String, Double> TAX_RATES;

    // Statischer Initialisierungsblock: läuft einmalig beim Laden der Klasse
    static {
        TAX_RATES = new java.util.HashMap<>();
        TAX_RATES.put("Lebensmittel",    0.07);
        TAX_RATES.put("Elektronik",      0.19);
        TAX_RATES.put("Bücher",          0.07);
        TAX_RATES.put("Luxusgüter",      0.19);
        System.out.println("Steuersätze geladen");
    }

    public static double getTaxRate(String category) {
        return TAX_RATES.getOrDefault(category, 0.19);
    }
}
```

```java
// Mehrere statische Blöcke sind erlaubt — sie laufen in Textreihenfolge
public class AppConfig {
    static final String ENV;
    static final int MAX_CONNECTIONS;

    static {
        ENV = System.getenv("APP_ENV") != null
            ? System.getenv("APP_ENV")
            : "development";
    }

    static {
        // Zweiter Block: kann auf Ergebnisse des ersten zugreifen
        MAX_CONNECTIONS = ENV.equals("production") ? 100 : 10;
        System.out.println("Config geladen für: " + ENV);
    }
}
```

**Gesamtreihenfolge der Klasseninitialisierung:**

| Schritt | Was passiert | Wann |
|---|---|---|
| 1 | Statische Felder auf Defaultwerte (`0`, `null`, `false`) | Beim Laden |
| 2 | Statische Feldinitializer + `static`-Blöcke (in Textreihenfolge) | Beim Laden |
| 3 | Instanzfelder auf Defaultwerte | Pro `new` |
| 4 | Instanzfeld-Initializer + Instanz-Blöcke (in Textreihenfolge) | Pro `new` |
| 5 | Konstruktor-Body | Pro `new` |

> **Faustregel:** Statische Blöcke für Klassen-Setup, Instanz-Blöcke für Objekt-Setup. Beide laufen vor ihrem jeweiligen Konstruktor.

---

## 10. Garbage Collection — Überblick — [Anfänger]

Java verwaltet den Speicher automatisch über den **Garbage Collector (GC)**. Objekte, auf die keine Referenz mehr zeigt, werden vom GC erkannt und ihr Speicher wird freigegeben. Entwickler müssen Speicher nie manuell freigeben (kein `free()` wie in C).

```java
void example() {
    Product a = new Product("Apple", 0.89, 5);  // Objekt auf dem Heap
    Product b = new Product("Birne", 1.20, 4);  // zweites Objekt

    a = b;  // a zeigt nun auf "Birne" — das "Apple"-Objekt ist nicht mehr erreichbar
            // → GC darf das "Apple"-Objekt aufräumen

    b = null; // b zeigt nirgendwo mehr hin
              // Das "Birne"-Objekt ist NOCH erreichbar (über keine Variable, aber GC läuft asynchron)
              // Sobald GC läuft: Speicher wird freigegeben
}
// Nach return von example(): alle lokalen Variablen weg → beide Objekte GC-reif
```

**Wann ist ein Objekt GC-berechtigt?**
- Es gibt keine erreichbare Referenz mehr auf das Objekt
- Zirkuläre Referenzen ohne äußeren Anker werden ebenfalls erkannt

```java
// Zirkuläre Referenz — kein Problem für den GC
class Node {
    Node next;
}

Node n1 = new Node();
Node n2 = new Node();
n1.next = n2;
n2.next = n1;

n1 = null;
n2 = null;
// Beide Nodes sind jetzt GC-berechtigt, obwohl sie sich gegenseitig referenzieren
```

> **Hinweis:** `System.gc()` ist ein *Hinweis* an die JVM, den GC auszuführen — keine Garantie. Die Methode `finalize()` ist seit Java 9 deprecated und wird in Java 18+ nicht mehr unterstützt. Für Ressourcen-Cleanup: `try-with-resources` und `AutoCloseable` verwenden.

---

## Übungsaufgaben

### Practice 4-1: Create the Product Management Application (ca. 27 Minuten)

**Ziel:** Eine vollständige `Product`-Klasse und eine `ProductManager`-Klasse von Grund auf erstellen.

**Aufgaben:**

1. Erstelle eine Klasse `Product` mit folgenden privaten Feldern:
   - `name` (String)
   - `price` (double)
   - `rating` (int, Werte 1–5)

2. Füge einen Konstruktor hinzu, der alle drei Felder setzt.

3. Füge Getter-Methoden für alle Felder hinzu.

4. Füge eine Methode `getDiscount()` hinzu, die basierend auf dem Rating einen Rabatt berechnet:
   - Rating 5: 20% Rabatt
   - Rating 4: 10% Rabatt
   - Rating 1–3: kein Rabatt

5. Erstelle eine Klasse `ProductManager` mit:
   - Einer Methode `printProductInfo(Product product)`, die alle Produktdetails formatiert ausgibt
   - Einem Feld `Product[] products` für bis zu 5 Produkte
   - Einer Methode `addProduct(Product product)`, die ein Produkt hinzufügt

6. Erstelle in `main()` mindestens 3 Produkte und gib alle aus:
   ```java
   public static void main(String[] args) {
       ProductManager pm = new ProductManager();
       pm.addProduct(new Product("Apple", 0.89, 5));
       pm.addProduct(new Product("Coffee", 4.99, 4));
       pm.addProduct(new Product("Tea", 2.99, 3));
       // Alle Produkte ausgeben
   }
   ```

**Erwartete Ausgabe (Beispiel):**
```
Produkt: Apple    | Preis: 0,89 € | Rating: ***** | Rabatt: 20%
Produkt: Coffee   | Preis: 4,99 € | Rating: ****  | Rabatt: 10%
Produkt: Tea      | Preis: 2,99 € | Rating: ***   | Rabatt:  0%
```

---

### Practice 4-2: Enhance the Product Class (ca. 16 Minuten)

**Ziel:** Die `Product`-Klasse um Kapselung, Validierung und weitere Methoden erweitern.

**Aufgaben:**

1. Mache alle Felder der `Product`-Klasse `private` (falls noch nicht geschehen).

2. Füge Setter-Methoden mit Validierung hinzu:
   - `setPrice(double price)`: Preis muss >= 0 sein
   - `setRating(int rating)`: Rating muss zwischen 1 und 5 liegen
   - Wirf bei ungültigen Werten eine `IllegalArgumentException`

3. Überschreibe `toString()`, um eine lesbare Darstellung zu liefern:
   ```
   Product[name='Coffee', price=4.99, rating=4]
   ```

4. Implementiere `equals()` und `hashCode()` so, dass zwei Produkte gleich sind, wenn sie denselben Namen und Preis haben.

5. Füge eine statische Methode `Product.of(String name, double price)` hinzu, die ein Produkt mit Rating 1 zurückgibt (Factory-Methode).

6. Teste deine Änderungen:
   ```java
   Product p1 = new Product("Apple", 0.89, 5);
   Product p2 = Product.of("Apple", 0.89);
   System.out.println(p1.equals(p2)); // true (gleicher Name und Preis)
   
   try {
       p1.setRating(6);  // soll Exception werfen
   } catch (IllegalArgumentException e) {
       System.out.println("Fehler abgefangen: " + e.getMessage());
   }
   ```

---

### Practice 4-3: Document Classes (ca. 10 Minuten)

**Ziel:** Eigene Klassen mit professionellen Javadoc-Kommentaren versehen.

**Aufgaben:**

1. Füge der `Product`-Klasse einen Javadoc-Kommentar auf Klassen-Ebene hinzu:
   - Kurze Beschreibung des Zwecks der Klasse
   - `@author` mit deinem Namen
   - `@version 1.0`

2. Dokumentiere alle public Methoden mit:
   - Kurze Beschreibung
   - `@param` für jeden Parameter
   - `@return` wenn kein void
   - `@throws` wenn eine Exception geworfen werden kann

3. Dokumentiere die Felder (private Felder können kurz dokumentiert werden).

4. Generiere die Javadoc-HTML-Dokumentation:
   ```bash
   javadoc -d docs/api -sourcepath src/main/java shop/Product.java shop/ProductManager.java
   ```
   Öffne `docs/api/index.html` im Browser und prüfe die generierte Dokumentation.

5. **Bonus:** Füge in der Klassen-Beschreibung ein Verwendungsbeispiel mit dem `{@code}`-Tag ein:
   ```java
   /**
    * ...
    * <p>Beispiel:</p>
    * <pre>{@code
    * Product p = new Product("Apple", 0.89, 5);
    * System.out.println(p.getName()); // "Apple"
    * }</pre>
    */
   ```

---

## Vollständiges Klassenbeispiel (Zusammenfassung)

```java
package shop;

import java.util.Objects;

/**
 * Repräsentiert ein Produkt im Produktverwaltungssystem.
 *
 * @author Max Mustermann
 * @version 1.0
 */
public class Product {

    private String name;
    private double price;
    private int rating;

    /**
     * Erstellt ein Produkt mit Name, Preis und Bewertung.
     *
     * @param name   der Produktname
     * @param price  der Preis in Euro (>= 0)
     * @param rating die Bewertung (1–5)
     * @throws IllegalArgumentException bei ungültigen Werten
     */
    public Product(String name, double price, int rating) {
        this.name = name;
        setPrice(price);
        setRating(rating);
    }

    public String getName()  { return name; }
    public double getPrice() { return price; }
    public int getRating()   { return rating; }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Ungültiger Preis: " + price);
        this.price = price;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Ungültiges Rating: " + rating);
        this.rating = rating;
    }

    public double getDiscount() {
        return switch (rating) {
            case 5 -> price * 0.20;
            case 4 -> price * 0.10;
            default -> 0.0;
        };
    }

    public static Product of(String name, double price) {
        return new Product(name, price, 1);
    }

    @Override
    public String toString() {
        return String.format("Product[name='%s', price=%.2f, rating=%d]", name, price, rating);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Product other)) return false;
        return Double.compare(price, other.price) == 0 && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price);
    }
}
```

---

## Multiple-Choice-Fragen

**Frage 1:** Was ist der Unterschied zwischen einer Klasse und einem Objekt?

- A) Eine Klasse ist eine Instanz eines Objekts
- B) Klassen und Objekte sind dasselbe, nur unterschiedliche Begriffe
- C) **Eine Klasse ist die Blaupause; ein Objekt ist eine konkrete Instanz dieser Blaupause** ✓
- D) Objekte existieren nur im Speicher, Klassen nur im Quellcode

---

**Frage 2:** Was gibt folgender Code aus?
```java
Product a = new Product();
a.name = "Apple";
Product b = a;
b.name = "Birne";
System.out.println(a.name);
```

- A) `Apple`
- B) `null`
- C) Eine `NullPointerException`
- D) **`Birne`** ✓

> *`b = a` kopiert nicht das Objekt, sondern die Referenz. Beide Variablen zeigen auf dasselbe Objekt im Heap.*

---

**Frage 3:** Was passiert, wenn man keinen Konstruktor definiert, dann aber einen parametrisierten hinzufügt?

- A) Beide Konstruktoren stehen zur Verfügung
- B) Java fügt automatisch beide Konstruktoren hinzu
- C) **Der parameterlose Default-Konstruktor verschwindet — nur der eigene ist verfügbar** ✓
- D) Der Compiler wirft einen Fehler

---

**Frage 4:** Was ist der Access Modifier `private`?

- A) Zugriff von überall möglich
- B) Zugriff innerhalb desselben Packages
- C) Zugriff für Subklassen erlaubt
- D) **Zugriff nur innerhalb derselben Klasse** ✓

---

**Frage 5:** Wozu dient `this` in einer Setter-Methode wie `void setName(String name)`?

- A) Um die Klasse zu referenzieren (statisch)
- B) Um den Superklassen-Konstruktor aufzurufen
- C) **Um zwischen dem Instanzfeld `this.name` und dem gleichnamigen Parameter `name` zu unterscheiden** ✓
- D) Um ein neues Objekt zu erzeugen

---

**Frage 6:** Warum sollten `equals()` und `hashCode()` immer zusammen überschrieben werden?

- A) Weil `hashCode()` intern `equals()` aufruft
- B) Weil Java sonst einen Kompilierungsfehler wirft
- C) Das ist nur eine Konvention, technisch nicht notwendig
- D) **Weil `HashMap`/`HashSet` beide Methoden kombiniert nutzen — inkonsistente Implementierung führt zu falschen Ergebnissen** ✓

---

**Frage 7:** Was bewirkt `@Override` über einer Methode?

- A) Die Methode wird mehrfach ausgeführt
- B) Die Methode überschreibt sich selbst
- C) **Die Annotation prüft zur Compile-Zeit, ob wirklich eine Methode der Oberklasse überschrieben wird** ✓
- D) Die Methode wird für Subklassen gesperrt

---

**Frage 8:** Was ist Pattern Matching für `instanceof`? `[Java 16]`

- A) Eine Methode der `Object`-Klasse
- B) Ein neuer Operator, der Typ-Casts verhindert
- C) **Eine kompakte Syntax, die Typ-Prüfung und Variablen-Binding in einem Schritt kombiniert** ✓
- D) Ein Design Pattern für Fabrikmethoden

---

**Frage 9:** Was ist ein Unnamed Pattern (`_`) bei `instanceof`? `[Java 22]`

- A) Eine Variable mit dem Namen `_`
- B) Ein Wildcard für alle primitiven Typen
- C) Ein Platzhalter für den `default`-Zweig
- D) **Eine Möglichkeit, den Typ zu prüfen ohne eine benannte Variable zu binden** ✓

---

**Frage 10:** Welchen Vorteil hat Markdown-Javadoc (`///`) gegenüber klassischem `/** */`? `[Java 23]`

- A) Es erzeugt schnelleren kompilierten Code
- B) Es funktioniert nur in IntelliJ IDEA
- C) Es erlaubt verschachtelte Kommentare
- D) **Es ermöglicht direkte Markdown-Formatierung ohne HTML-Tags wie `<p>`, `<pre>` oder `<code>`** ✓

---

**Frage 11:** Was ist `var` in Java und wo darf es verwendet werden? `[Java 10]`

- A) Ein Schlüsselwort, das den Typ zur Laufzeit ermittelt (dynamisch typisiert)
- B) **Ein reservierter Typname für lokale Variablen mit Initializer; der Typ wird zur Compile-Zeit abgeleitet** ✓
- C) Ein Modifier wie `final`, der Variablen unveränderlich macht
- D) `var` kann überall verwendet werden: bei Feldern, Parametern und Rückgabetypen

---

**Frage 12:** Welcher Code kompiliert NICHT? `[Java 10]`

- A) `var name = "Kaffee";`
- B) `var price = 4.99;`
- C) `for (var p : products) { ... }`
- D) **`var x;`** ✓

> *`var` ohne Initializer ist ein Kompilierungsfehler — der Compiler kann den Typ nicht ableiten.*

---

**Frage 13:** Wozu dient `_` als unbenannte Variable in Java? `[Java 22]`

- A) Es ist ein gültiger Variablenname, den man wie jeden anderen verwenden kann
- B) Es deaktiviert die Typprüfung im betreffenden Block
- C) **Es markiert einen Wert, der syntaktisch benötigt wird, aber bewusst ignoriert werden soll** ✓
- D) Es ist eine Kurzschreibweise für `null`

---

**Frage 14:** In welchem der folgenden Kontexte kann `_` NICHT verwendet werden? `[Java 22]`

- A) `catch (Exception _) { ... }`
- B) `for (var _ : list) { ... }`
- C) `list.forEach(_ -> count++);`
- D) **Als Name eines privaten Instanzfelds in einer öffentlichen Klasse** ✓

> *`_` ist als unbenannte Variable nur in lokalen Kontexten erlaubt, nicht als Feldname.*

---

**Frage 15:** Wann wird ein Instanz-Initialisierungsblock ausgeführt?

- A) Nur beim ersten Erstellen eines Objekts der Klasse
- B) Beim Laden der Klasse, bevor irgendein Objekt existiert
- C) **Bei jeder Objekterstellung, unmittelbar vor dem Konstruktor-Body** ✓
- D) Nur wenn kein Konstruktor definiert wurde

---

**Frage 16:** In welcher Reihenfolge werden diese Teile ausgeführt, wenn `new Demo()` aufgerufen wird?

```java
class Demo {
    int x = 1;
    { x = 2; }
    Demo() { x = 3; }
}
```

- A) Konstruktor → Instanz-Block → Feld-Initializer
- B) Instanz-Block → Feld-Initializer → Konstruktor
- C) **Feld-Initializer (`x=1`) → Instanz-Block (`x=2`) → Konstruktor (`x=3`)** ✓
- D) Alle drei gleichzeitig; Reihenfolge ist nicht definiert

---

**Frage 17:** Wie oft wird ein statischer Initialisierungsblock ausgeführt?

- A) Bei jeder Objekterstellung der Klasse
- B) Nie automatisch — nur wenn explizit aufgerufen
- C) Bei jedem Programmstart mehrfach, je nach Thread-Anzahl
- D) **Genau einmal, wenn die Klasse zum ersten Mal vom ClassLoader geladen wird** ✓

---

**Frage 18:** Wann ist ein Objekt berechtigt, vom Garbage Collector aufgeräumt zu werden?

- A) Sobald die Methode, in der es erstellt wurde, zurückkehrt
- B) Sobald `System.gc()` aufgerufen wird
- C) **Sobald keine erreichbare Referenz mehr auf das Objekt zeigt** ✓
- D) Erst wenn das Programm beendet wird

---

## Skill Check: Typische Prüfungsfragen

1. Was ist der Unterschied zwischen einer Klasse und einem Objekt?
2. Was ist der Unterschied zwischen einem Instanzfeld und einem statischen Feld?
3. Wann wird der Default-Konstruktor nicht mehr automatisch generiert?
4. Was bedeutet Kapselung (Encapsulation) und warum ist sie wichtig?
5. Was ist der Unterschied zwischen `public`, `private` und `protected`?
6. Was macht die `this`-Referenz? Wann wird sie benötigt?
7. Warum sollte man `equals()` und `hashCode()` zusammen überschreiben?
8. Was ist der `@Override`-Annotation und warum sollte man sie verwenden?
9. Was ist Javadoc und wie wird es generiert?
10. Was ist der Unterschied zwischen einer Instanzmethode und einer statischen Methode?
11. Was ist `var` und welche Einschränkungen gelten für seine Verwendung? `[Java 10]`
12. In welchen Kontexten kann `_` als unbenannte Variable eingesetzt werden? `[Java 22]`
13. Was ist der Unterschied zwischen einem Instanz-Initialisierungsblock und einem statischen Initialisierungsblock?
14. In welcher Reihenfolge werden statische Blöcke, Instanz-Blöcke und Konstruktoren ausgeführt?
15. Was versteht man unter Garbage Collection und wann ist ein Objekt GC-berechtigt?
