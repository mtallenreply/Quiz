# Modul 21: Miscellaneous – Moderne Java-Features

## Übersicht

Dieses Modul behandelt moderne Java-Features von Java 16 bis Java 25: Records, Sealed Classes, Pattern Matching, Switch Expressions, Text Blocks und die neuesten Sprachfeatures aus Java 25. Viele dieser Features wurden als Preview-Features eingefuehrt und sind in Java 25 nun final.

| Abschnitt                   | Dauer |
|-----------------------------|-------|
| Records                     | 25 m  |
| Sealed Classes              | 20 m  |
| Pattern Matching instanceof | 15 m  |
| Switch Pattern Matching     | 20 m  |
| Text Blocks                 | 12 m  |
| Java 25 Features            | 18 m  |
| Practice 21-1               | 20 m  |
| **Gesamt**                  | **130 m** |

> **Skill Check: Modern Java** – mind. 80 % erforderlich, um das Modul abzuschließen.

---

## 1. Records (JEP 395 – seit Java 16 final)

Records sind transparente, unveraenderliche Datentraeger. Der Compiler generiert Konstruktor, Getter, `equals()`, `hashCode()` und `toString()` automatisch.

### 1.1 Grundlegende Record-Deklaration

```java
// Klassische Klasse (30+ Zeilen)
public final class PersonAlt {
    private final String name;
    private final int    alter;

    public PersonAlt(String name, int alter) {
        this.name  = name;
        this.alter = alter;
    }
    public String getName()  { return name; }
    public int    getAlter() { return alter; }
    // equals, hashCode, toString ...
}

// Record (1 Zeile!)
public record Person(String name, int alter) {}

// Verwendung
Person p1 = new Person("Anna", 30);
Person p2 = new Person("Anna", 30);
System.out.println(p1.name());           // Anna  (kein get-Praefix!)
System.out.println(p1.alter());          // 30
System.out.println(p1);                  // Person[name=Anna, alter=30]
System.out.println(p1.equals(p2));       // true
System.out.println(p1.hashCode() == p2.hashCode()); // true
```

### 1.2 Compact Constructor

Der Compact Constructor erlaubt Validierung ohne den Boilerplate des normalen Konstruktors.

```java
public record Koordinate(double x, double y) {

    /** Compact Constructor – kein explizites this.x = x noetig */
    public Koordinate {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new IllegalArgumentException("Koordinate darf nicht NaN sein");
        }
        // Normalisierung (optionale Anpassung der Felder)
        x = Math.round(x * 1000.0) / 1000.0; // Auf 3 Nachkommastellen runden
        y = Math.round(y * 1000.0) / 1000.0;
    }

    /** Benutzerdefinierte Methode im Record */
    public double abstand(Koordinate andere) {
        double dx = this.x - andere.x;
        double dy = this.y - andere.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Statische Fabrikmethode */
    public static Koordinate ursprung() {
        return new Koordinate(0.0, 0.0);
    }
}

// Verwendung
Koordinate k1 = new Koordinate(3.0, 4.0);
Koordinate k2 = Koordinate.ursprung();
System.out.println(k1.abstand(k2)); // 5.0

try {
    new Koordinate(Double.NaN, 0);
} catch (IllegalArgumentException e) {
    System.out.println("Fehler: " + e.getMessage());
}
```

### 1.3 Accessor-Methoden ueberschreiben

```java
public record Passwort(String wert) {

    /** Compact Constructor mit Validierung */
    public Passwort {
        if (wert == null || wert.length() < 8) {
            throw new IllegalArgumentException("Passwort muss mind. 8 Zeichen haben");
        }
    }

    /** Accessor-Methode ueberschreiben – kein Klartext zurueckgeben */
    @Override
    public String wert() {
        return "*".repeat(wert.length());
    }

    /** toString ueberschreiben */
    @Override
    public String toString() {
        return "Passwort[laenge=" + wert.length() + "]";
    }
}

Passwort pw = new Passwort("Sicher123!");
System.out.println(pw.wert()); // ********** (maskiert)
System.out.println(pw);        // Passwort[laenge=10]
```

### 1.4 Records implementieren Interfaces

```java
interface Berechnenbar {
    double berechne();
}

public record Kreis(double radius) implements Berechnenbar {

    public Kreis {
        if (radius <= 0) throw new IllegalArgumentException("Radius muss positiv sein");
    }

    @Override
    public double berechne() {
        return Math.PI * radius * radius;
    }

    public double umfang() {
        return 2 * Math.PI * radius;
    }
}

public record Rechteck(double breite, double hoehe) implements Berechnenbar {

    @Override
    public double berechne() {
        return breite * hoehe;
    }
}

// Polymorphismus mit Records
List<Berechnenbar> formen = List.of(
    new Kreis(5.0),
    new Rechteck(3.0, 4.0),
    new Kreis(2.5)
);
formen.forEach(f -> System.out.printf("Flaeche: %.2f%n", f.berechne()));
```

### 1.5 With-Expressions (Java 25 – JEP 468 Preview)

Java 25 fuehrt `with`-Ausdruecke fuer Records ein, die eine modifizierte Kopie erstellen.

```java
// Java 25: with-Expression (Preview)
record Adresse(String strasse, String stadt, String plz) {}

Adresse original = new Adresse("Hauptstr. 1", "Berlin", "10115");

// Kopie mit geaenderter Strasse (alle anderen Felder unveraendert)
Adresse geaendert = original with { strasse = "Nebenstr. 5"; };
System.out.println(original);  // Adresse[strasse=Hauptstr. 1, stadt=Berlin, plz=10115]
System.out.println(geaendert); // Adresse[strasse=Nebenstr. 5, stadt=Berlin, plz=10115]

// Mehrere Felder aendern
Adresse umgezogen = original with {
    strasse = "Musterweg 42";
    stadt   = "Hamburg";
    plz     = "20095";
};
```

> **Hinweis:** `with`-Expressions sind in Java 25 als Preview-Feature verfuegbar (`--enable-preview`). Ohne `with` kann man einen benutzerdefinierten `withStrasse(String)`-Accessor im Record definieren.

---

## 2. Sealed Classes (JEP 409 – seit Java 17 final)

Sealed Classes beschraenken die Vererbungshierarchie: Nur explizit genannte Klassen duerfen eine versiegelte Klasse erweitern.

### 2.1 Grundlegende Syntax

```java
/** Versiegelte abstrakte Klasse – nur drei Unterklassen erlaubt */
public sealed abstract class Form
    permits Kreis, Rechteck, Dreieck {

    public abstract double flaeche();
    public abstract String beschreibung();
}

/** final: Keine weitere Unterklasse moeglich */
public final class Kreis extends Form {
    private final double radius;

    public Kreis(double radius) { this.radius = radius; }

    @Override
    public double flaeche() { return Math.PI * radius * radius; }

    @Override
    public String beschreibung() {
        return "Kreis mit Radius %.2f".formatted(radius);
    }
}

/** sealed: Kann selbst weitere erlaubte Unterklassen haben */
public sealed class Rechteck extends Form
    permits Quadrat {

    protected final double breite;
    protected final double hoehe;

    public Rechteck(double breite, double hoehe) {
        this.breite = breite;
        this.hoehe  = hoehe;
    }

    @Override
    public double flaeche() { return breite * hoehe; }

    @Override
    public String beschreibung() {
        return "Rechteck %sx%s".formatted(breite, hoehe);
    }
}

/** non-sealed: Oeffnet die Hierarchie wieder */
public non-sealed class Dreieck extends Form {
    private final double basis;
    private final double hoehe;

    public Dreieck(double basis, double hoehe) {
        this.basis = basis;
        this.hoehe = hoehe;
    }

    @Override
    public double flaeche() { return 0.5 * basis * hoehe; }

    @Override
    public String beschreibung() {
        return "Dreieck mit Basis %s und Hoehe %s".formatted(basis, hoehe);
    }
}

/** Quadrat erweitert Rechteck */
public final class Quadrat extends Rechteck {
    public Quadrat(double seite) { super(seite, seite); }

    @Override
    public String beschreibung() {
        return "Quadrat mit Seite %s".formatted(breite);
    }
}
```

### 2.2 Sealed Interfaces

```java
public sealed interface Json
    permits Json.Null, Json.Bool, Json.Zahl, Json.Text, Json.Array, Json.Objekt {

    record Null()                               implements Json {}
    record Bool(boolean wert)                   implements Json {}
    record Zahl(double wert)                    implements Json {}
    record Text(String wert)                    implements Json {}
    record Array(java.util.List<Json> elemente) implements Json {}
    record Objekt(java.util.Map<String, Json> felder) implements Json {}
}

// Verwendung
Json json = new Json.Objekt(java.util.Map.of(
    "name",  new Json.Text("Anna"),
    "alter", new Json.Zahl(30),
    "aktiv", new Json.Bool(true)
));
```

### 2.3 Warum Sealed Classes?

| Aspekt            | Ohne Sealed            | Mit Sealed                           |
|-------------------|------------------------|--------------------------------------|
| Hierarchiekontrolle | Beliebige Unterklassen | Nur erlaubte Unterklassen           |
| Pattern Matching  | Default-Arm noetig     | Erschoepfende Pruefung ohne default |
| API-Design        | Offen, unkontrolliert  | Prazise, dokumentierte Hierarchie  |
| Algebraische Typen | Nicht moeglich         | Sum Types wie in Scala/Haskell     |

---

## 3. Pattern Matching fuer instanceof (JEP 394 – seit Java 16 final)

### 3.1 Klassisch vs. Pattern Matching

```java
// KLASSISCH – verbose und fehleranfaellig
Object obj = "Hallo";
if (obj instanceof String) {
    String s = (String) obj; // Expliziter Cast noetig
    System.out.println(s.toUpperCase());
}

// PATTERN MATCHING – kompakt und sicher
if (obj instanceof String s) {
    // s ist automatisch als String verfuegbar
    System.out.println(s.toUpperCase());
}

// Mit zusaetzlicher Bedingung (guarded pattern)
if (obj instanceof String s && s.length() > 3) {
    System.out.println("Langer String: " + s);
}
```

### 3.2 Pattern Matching in Methoden

```java
public static String objektBeschreiben(Object obj) {
    if (obj instanceof Integer i) {
        return "Ganzzahl: " + i;
    } else if (obj instanceof Double d) {
        return "Dezimalzahl: " + d;
    } else if (obj instanceof String s && !s.isBlank()) {
        return "Text (" + s.length() + " Zeichen): " + s;
    } else if (obj instanceof String s) {
        return "Leerer Text";
    } else if (obj instanceof int[] arr) {
        return "int-Array mit " + arr.length + " Elementen";
    } else if (obj == null) {
        return "null";
    } else {
        return "Unbekannt: " + obj.getClass().getSimpleName();
    }
}

System.out.println(objektBeschreiben(42));         // Ganzzahl: 42
System.out.println(objektBeschreiben("Hallo"));   // Text (5 Zeichen): Hallo
System.out.println(objektBeschreiben(""));         // Leerer Text
System.out.println(objektBeschreiben(new int[]{1,2,3})); // int-Array mit 3 Elementen
```

---

## 4. Switch Pattern Matching mit when-Guards (JEP 441 – seit Java 21 final)

### 4.1 Switch als Ausdruck mit Pattern Matching

```java
public static String formBeschreiben(Object form) {
    return switch (form) {
        case Integer i when i < 0    -> "Negative Zahl: " + i;
        case Integer i               -> "Positive Zahl oder Null: " + i;
        case String s when s.isBlank()-> "Leerer String";
        case String s                -> "String: " + s.toUpperCase();
        case null                    -> "null-Wert";
        default                      -> "Unbekannt: " + form;
    };
}

System.out.println(formBeschreiben(-5));       // Negative Zahl: -5
System.out.println(formBeschreiben(42));       // Positive Zahl oder Null: 42
System.out.println(formBeschreiben("hallo")); // String: HALLO
System.out.println(formBeschreiben("   "));   // Leerer String
System.out.println(formBeschreiben(null));    // null-Wert
```

### 4.2 Switch mit Sealed Classes (erschoepfend)

```java
// Da Form sealed ist, weiss der Compiler alle Unterklassen
// -> kein default-Arm noetig!
public static double flaecheBerechnen(Form form) {
    return switch (form) {
        case Kreis    k -> Math.PI * k.radius() * k.radius();
        case Rechteck r -> r.breite() * r.hoehe();
        case Dreieck  d -> 0.5 * d.basis() * d.hoehe();
        // Kein default noetig – alle Faelle abgedeckt
    };
}
```

### 4.3 Switch mit Record-Dekonstruktion

```java
record Punkt(int x, int y) {}
record Segment(Punkt start, Punkt ende) {}

Object obj = new Segment(new Punkt(0, 0), new Punkt(3, 4));

// Record-Dekonstruktionsmuster (Java 21+)
switch (obj) {
    case Segment(Punkt(int x1, int y1), Punkt(int x2, int y2)) -> {
        double laenge = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
        System.out.printf("Segment-Laenge: %.2f%n", laenge); // 5.00
    }
    case Punkt(int x, int y) ->
        System.out.println("Punkt bei (%d, %d)".formatted(x, y));
    default ->
        System.out.println("Unbekannt");
}
```

### 4.4 Exhaustiveness – Vollstaendigkeit

```java
sealed interface Farbe permits Rot, Gruen, Blau {}
record Rot()   implements Farbe {}
record Gruen() implements Farbe {}
record Blau()  implements Farbe {}

// VOLLSTAENDIG – kein default noetig
String farbName(Farbe f) {
    return switch (f) {
        case Rot   r -> "Rot";
        case Gruen g -> "Gruen";
        case Blau  b -> "Blau";
    };
}

// NICHT VOLLSTAENDIG – Compilerfehler ohne default/case null
// String fehler(Farbe f) {
//     return switch (f) {
//         case Rot r -> "Rot";
//         // Gruen und Blau fehlen!
//     };
// }
```

---

## 5. Text Blocks (JEP 378 – seit Java 15 final)

### 5.1 Grundlegende Syntax

```java
// Ohne Text Block – schwer lesbar
String jsonAlt = "{\n" +
    "  \"name\": \"Anna\",\n" +
    "  \"alter\": 30,\n" +
    "  \"aktiv\": true\n" +
    "}";

// Mit Text Block – sauber und lesbar
String jsonNeu = """
    {
      "name": "Anna",
      "alter": 30,
      "aktiv": true
    }
    """;

System.out.println(jsonAlt.equals(jsonNeu)); // true
```

### 5.2 Einrueckung und abschliessende Anführungszeichen

```java
// Das schliessende """ bestimmt den minimalen Einzug (Incidental Whitespace)
String sql = """
    SELECT id, name, preis
    FROM produkt
    WHERE aktiv = TRUE
      AND preis < ?
    ORDER BY name
    """; // Einzug: 4 Leerzeichen werden entfernt

// Abschliessende """ auf der gleichen Zeile -> kein abschliessendes Newline
String ohneNewline = """
    Keine neue Zeile am Ende""";

System.out.println(ohneNewline.endsWith("\n")); // false
```

### 5.3 Escape-Sequenzen in Text Blocks

```java
// \n  – Zeilenumbruch (Standard)
// \t  – Tabulator
// \\  – Backslash
// \"  – Anführungszeichen (in Text Blocks oft unnoetig)
// \s  – Leerzeichen (verhindert Stripping am Zeilenende)
// \   – Zeilenkontinuation (kein Zeilenumbruch)

String html = """
    <html>
        <body>
            <p>Hallo \
    Welt</p>
            <p>Tab:\tEingerueckt</p>
        </body>
    </html>
    """;

// \s sichert Leerzeichen am Zeilenende
String zeile = """
    Pfad: /usr/local   \s
    """; // 3 Leerzeichen nach /usr/local bleiben erhalten
```

### 5.4 Text Blocks fuer verschiedene Formate

```java
// SQL
String createTable = """
    CREATE TABLE IF NOT EXISTS benutzer (
        id       SERIAL PRIMARY KEY,
        name     VARCHAR(100) NOT NULL,
        email    VARCHAR(200) UNIQUE,
        erstellt TIMESTAMP DEFAULT NOW()
    )
    """;

// HTML
String htmlTemplate = """
    <!DOCTYPE html>
    <html lang="de">
    <head><title>%s</title></head>
    <body>
        <h1>%s</h1>
        <p>%s</p>
    </body>
    </html>
    """.formatted("Titel", "Ueberschrift", "Inhalt");

// JSON
String jsonTemplate = """
    {
      "id": %d,
      "name": "%s",
      "preis": %.2f,
      "tags": %s
    }
    """.formatted(1, "Kaffee", 3.99, "[\"warm\",\"koffein\"]");

// YAML
String yahrConfig = """
    server:
      port: 8080
      host: localhost
    database:
      url: jdbc:postgresql://localhost:5432/db
      pool-size: 10
    """;
```

---

## 6. Java 25 Features

### 6.1 Scoped Values (JEP 487 – Java 25 Preview)

Scoped Values sind eine sichere, effiziente Alternative zu `ThreadLocal` – insbesondere fuer virtuelle Threads (Project Loom).

```java
import java.lang.ScopedValue;

public class ScopedValuesBeispiel {

    // Scoped Value deklarieren (aehnlich wie Konstante)
    private static final ScopedValue<String> BENUTZER_NAME =
        ScopedValue.newInstance();

    private static final ScopedValue<String> REQUEST_ID =
        ScopedValue.newInstance();

    /** Benutzer-Kontext setzen und Methode ausfuehren */
    public static void anfragenVerarbeiten(String benutzer, String reqId) {
        ScopedValue.where(BENUTZER_NAME, benutzer)
                   .where(REQUEST_ID, reqId)
                   .run(() -> {
                       logsSchreiben("Anfrage empfangen");
                       datenbankAbfragen();
                       logsSchreiben("Anfrage abgeschlossen");
                   });
    }

    /** Scoped Value ist im gesamten Call-Stack sichtbar */
    static void logsSchreiben(String nachricht) {
        System.out.printf("[%s][%s] %s%n",
            REQUEST_ID.get(),     // Automatisch verfuegbar
            BENUTZER_NAME.get(),  // Automatisch verfuegbar
            nachricht);
    }

    static void datenbankAbfragen() {
        String benutzer = BENUTZER_NAME.get();
        System.out.println("Lade Daten fuer: " + benutzer);
    }

    public static void main(String[] args) {
        anfragenVerarbeiten("anna", "REQ-001");
        anfragenVerarbeiten("bob",  "REQ-002");
        // Scoped Values sind ausserhalb des where().run() NICHT sichtbar
        // BENUTZER_NAME.get() -> NoSuchElementException
    }
}
```

### 6.2 Instance Main Methods und Unnamed Classes (JEP 495 – Java 25)

Java 25 erlaubt vereinfachte Einstiegspunkte – ohne Klasse, `static` und `String[] args`.

```java
// Unnamed Class (kein class-Schluessel noetig)
// Dateiname: Hallo.java

void main() {
    System.out.println("Hallo von einer Unnamed Class!");
    var name = "Java 25";
    System.out.println("Willkommen in " + name);
}
```

```java
// Instance Main Method – kein static noetig
public class EinfachesBeispiel {

    void main() {
        System.out.println("Instanz-Main-Methode!");
        zeigeInfo();
    }

    void zeigeInfo() {
        System.out.println("Kein static noetig fuer main()");
    }
}

// Aeltere Signatur bleibt kompatibel
public class KlassischesBeispiel {
    public static void main(String[] args) {
        System.out.println("Klassische main-Methode");
    }
}
```

### 6.3 Module Import Declarations (JEP 494 – Java 25)

Erlaubt den Import aller Pakete eines Moduls mit einer einzigen Anweisung.

```java
// VORHER: Viele einzelne Importe
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
// ... viele weitere

// NACHHER (Java 25): Ein Modul-Import
import module java.base;    // Importiert alle Pakete aus java.base
import module java.sql;     // Importiert java.sql, javax.sql usw.

// Jetzt verfuegbar ohne weitere Importe:
List<String> liste = new ArrayList<>();
Map<String, Integer> map = new HashMap<>();
// Connection conn = ...  (aus java.sql)
```

### 6.4 Primitive Typen in Pattern Matching (JEP 488 – Java 25 Preview)

```java
// Java 25: Primitive Typen in instanceof und switch
Object zahl = 42;

// instanceof mit primitivem Typ
if (zahl instanceof int i) {
    System.out.println("Direktes int: " + i);
}

// switch mit primitiven Typen
switch (zahl) {
    case int i    when i > 0 -> System.out.println("Positive Zahl: " + i);
    case int i               -> System.out.println("Nicht-positive Zahl: " + i);
    case double d            -> System.out.println("Double: " + d);
    default                  -> System.out.println("Anderer Typ");
}
```

---

## 7. Record Patterns (JEP 440 – seit Java 21 final)

### 7.1 Geschachtelte Record-Dekonstruktion

```java
record Punkt(double x, double y) {}
record Linie(Punkt start, Punkt ende) {}
record Dreieck(Punkt a, Punkt b, Punkt c) {}

Object form = new Linie(new Punkt(0, 0), new Punkt(3, 4));

// Record Pattern in instanceof
if (form instanceof Linie(Punkt(double x1, double y1),
                           Punkt(double x2, double y2))) {
    double laenge = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    System.out.printf("Linienlange: %.2f%n", laenge); // 5.00
}

// Record Pattern im switch
String beschreibung = switch (form) {
    case Linie(Punkt(var x1, var y1), Punkt(var x2, var y2)) ->
        "Linie von (%.1f,%.1f) nach (%.1f,%.1f)".formatted(x1, y1, x2, y2);
    case Dreieck(var a, var b, var c) ->
        "Dreieck mit 3 Ecken";
    case Punkt(var x, var y) ->
        "Punkt bei (%.1f, %.1f)".formatted(x, y);
    default -> "Unbekannte Form";
};
System.out.println(beschreibung);
```

---

## 8. Vollstaendiges Beispiel – Moderne Java-Features kombiniert

```java
import java.util.List;
import java.util.stream.Collectors;

/** Sealed Typhierarchie fuer Zahlungsmethoden */
sealed interface Zahlung permits Barzahlung, Kartenzahlung, Ueberweisung {}

record Barzahlung(double betrag) implements Zahlung {}
record Kartenzahlung(String kartenNummer, double betrag) implements Zahlung {}
record Ueberweisung(String iban, double betrag, String verwendungszweck) implements Zahlung {}

/** Record fuer Bestellungen */
record Bestellung(int id, String kunde, List<String> artikel, Zahlung zahlung) {

    /** Compact Constructor mit Validierung */
    public Bestellung {
        if (id <= 0)                   throw new IllegalArgumentException("ID muss positiv sein");
        if (kunde == null || kunde.isBlank()) throw new IllegalArgumentException("Kunden-Name fehlt");
        if (artikel == null || artikel.isEmpty()) throw new IllegalArgumentException("Keine Artikel");
        artikel = List.copyOf(artikel); // Unveraenderliche Kopie
    }

    /** Gesamtbetrag ermitteln per Pattern Matching */
    public double betrag() {
        return switch (zahlung) {
            case Barzahlung   b -> b.betrag();
            case Kartenzahlung k -> k.betrag();
            case Ueberweisung u -> u.betrag();
        };
    }

    /** Zahlungsbeschreibung */
    public String zahlungsArt() {
        return switch (zahlung) {
            case Barzahlung   b -> "Bar (%.2f EUR)".formatted(b.betrag());
            case Kartenzahlung k when k.kartenNummer().startsWith("4") ->
                "Visa-Karte ****" + k.kartenNummer().substring(k.kartenNummer().length() - 4);
            case Kartenzahlung k ->
                "Kreditkarte ****" + k.kartenNummer().substring(k.kartenNummer().length() - 4);
            case Ueberweisung u ->
                "Ueberweisung von %s (%s)".formatted(u.iban(), u.verwendungszweck());
        };
    }
}

public class BestellungDemo {

    public static void main(String[] args) {
        // Text Block fuer Ausgabe-Template
        String template = """
            ========================================
            Bestellung #%d
            Kunde:    %s
            Artikel:  %s
            Zahlung:  %s
            Betrag:   %.2f EUR
            ========================================
            """;

        List<Bestellung> bestellungen = List.of(
            new Bestellung(1, "Anna Muster",
                List.of("Kaffee", "Tee"),
                new Barzahlung(7.50)),

            new Bestellung(2, "Bob Beispiel",
                List.of("Laptop", "Maus"),
                new Kartenzahlung("4111111111111234", 899.00)),

            new Bestellung(3, "Carla Test",
                List.of("Buch Java 25"),
                new Ueberweisung("DE12345678901234", 49.99, "Bestellung #3"))
        );

        for (Bestellung b : bestellungen) {
            System.out.printf(template,
                b.id(), b.kunde(),
                String.join(", ", b.artikel()),
                b.zahlungsArt(),
                b.betrag());
        }

        // Pattern Matching fuer statistische Auswertung
        long barZahlungen   = bestellungen.stream()
            .filter(b -> b.zahlung() instanceof Barzahlung)
            .count();
        double kartenUmsatz = bestellungen.stream()
            .filter(b -> b.zahlung() instanceof Kartenzahlung)
            .mapToDouble(Bestellung::betrag)
            .sum();

        System.out.println("Barzahlungen:  " + barZahlungen);
        System.out.printf("Kartenumsatz:  %.2f EUR%n", kartenUmsatz);

        // Gesamtumsatz
        double gesamt = bestellungen.stream()
            .mapToDouble(Bestellung::betrag)
            .sum();
        System.out.printf("Gesamtumsatz:  %.2f EUR%n", gesamt);
    }
}
```

---

## 9. Feature-Vergleich: Java-Versionen

| Feature                            | Eingefuehrt | Status in Java 25   |
|------------------------------------|-------------|---------------------|
| Text Blocks                        | Java 13     | Final (Java 15)     |
| Records                            | Java 14     | Final (Java 16)     |
| Sealed Classes                     | Java 15     | Final (Java 17)     |
| Pattern Matching instanceof        | Java 14     | Final (Java 16)     |
| Switch Expressions                 | Java 12     | Final (Java 14)     |
| Switch Pattern Matching            | Java 17     | Final (Java 21)     |
| Record Patterns                    | Java 19     | Final (Java 21)     |
| Virtual Threads (Project Loom)     | Java 19     | Final (Java 21)     |
| Sequenced Collections              | –           | Final (Java 21)     |
| String Templates                   | Java 21     | Withdrawn           |
| Unnamed Classes / Instance Main    | Java 21     | Final (Java 25)     |
| Scoped Values                      | Java 20     | Preview (Java 25)   |
| Primitive Types in Patterns        | Java 23     | Preview (Java 25)   |
| With-Expressions fuer Records      | –           | Preview (Java 25)   |
| Module Import Declarations         | Java 23     | Final (Java 25)     |

---

## 10. Quick Reference: Moderne Syntax

```java
// 1. Record
record Punkt(int x, int y) {}

// 2. Sealed Class
sealed interface Form permits Kreis, Rechteck {}
final class Kreis    implements Form {}
final class Rechteck implements Form {}

// 3. Pattern Matching instanceof
if (obj instanceof String s && s.length() > 3) { ... }

// 4. Switch mit Pattern und when-Guard
String r = switch (obj) {
    case Integer i when i > 0 -> "positiv";
    case Integer i            -> "nicht-positiv";
    case String s             -> "text: " + s;
    default                   -> "sonstig";
};

// 5. Text Block
String sql = """
    SELECT *
    FROM tabelle
    WHERE aktiv = TRUE
    """;

// 6. Record Pattern
if (form instanceof Kreis(double r)) { ... }

// 7. Scoped Value (Java 25)
ScopedValue<String> USER = ScopedValue.newInstance();
ScopedValue.where(USER, "anna").run(() -> { System.out.println(USER.get()); });

// 8. Instance Main (Java 25)
// Klasse ohne "public static void main(String[] args)" moeglich:
void main() { System.out.println("Hallo!"); }

// 9. Module Import (Java 25)
import module java.base;

// 10. With-Expression fuer Records (Java 25 Preview)
record Adresse(String strasse, String stadt) {}
Adresse a = new Adresse("Hauptstr.", "Berlin");
Adresse b = a with { stadt = "Hamburg"; };
```

---

## Zusammenfassung

- **Records**: Unveraenderliche Datentraeger; Compact Constructor fuer Validierung; Accessor-Methoden ueberschreibbar; `with`-Expressions in Java 25 Preview.
- **Sealed Classes**: Beschraenken Vererbungshierarchie; `final` / `sealed` / `non-sealed`; ideal fuer algebraische Typen.
- **Pattern Matching instanceof**: Kombiniert Typtest und Cast; Guard mit `&&`-Bedingung.
- **Switch Pattern Matching**: Erschoepfende Pruefung; `when`-Guards; Record-Dekonstruktion; null-Behandlung.
- **Text Blocks**: Mehrzeilige Strings ohne Escape-Zeichenchaos; `formatted()`, `\s`, `\`-Kontinuation.
- **Scoped Values** (Java 25): Sicherer, unveraenderlicher Kontext im Call-Stack; Alternative zu ThreadLocal.
- **Instance Main Methods** (Java 25): `void main()` ohne `static` und `String[] args`; Unnamed Classes.
- **Module Import Declarations** (Java 25): `import module java.base` importiert alle Pakete eines Moduls.
