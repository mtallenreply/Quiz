# Modul 3: Text, Date, Time, and Numeric Objects

## Übersicht

Dieses Modul behandelt die wichtigsten Java-Klassen für Text, Zahlen, Datum und Zeit. Im Mittelpunkt stehen `String`, `StringBuilder`, `BigDecimal`, die Date-Time-API (`java.time`) sowie Lokalisierung und Formatierung.

| Thema | Dauer |
|---|---|
| String Initialization and Operations | 15m |
| String Indexing, Mutable Text Objects and Blocks | 22m |
| Describe Primitive Wrapper, BigDecimal Classes and Method Chaining | 14m |
| Handle Date and Time Values | 13m |
| Represent Languages and Countries, Format and Parse Numeric, Date Time Values | 13m |
| Describe Localization and Formatting Classes | 12m |
| Practice 3-1: Explore String and StringBuilder Objects | 33m |
| Practice 3-2: Use BigDecimal Class and Format Numeric Values | 12m |
| Practice 3-3: Use and Format Date and Time Values | 17m |
| Practice 3-4: Apply Localization and Format Messages | 13m |
| Skill Check: Text, Date, Time, and Numeric Objects (mind. 80%) | — |

---

## 1. String: Initialisierung und Operationen

### String in Java

`String` ist eine **Klasse** in Java (kein primitiver Typ), aber durch besondere JVM-Unterstützung wie ein primitiver Typ nutzbar. Strings sind **immutable** (unveränderlich) — jede Modifikation erzeugt ein neues String-Objekt.

### String-Literal vs. new String()

```java
// String-Literal → wird im String Pool gespeichert
String s1 = "Hallo";
String s2 = "Hallo";
System.out.println(s1 == s2);       // true  (selbe Referenz im Pool)
System.out.println(s1.equals(s2));  // true  (gleicher Inhalt)

// new String → immer neues Objekt auf dem Heap
String s3 = new String("Hallo");
System.out.println(s1 == s3);       // false (verschiedene Objekte)
System.out.println(s1.equals(s3));  // true  (gleicher Inhalt)
```

> **Wichtig:** Strings immer mit `.equals()` vergleichen, NICHT mit `==`!

### Häufige String-Methoden

```java
String text = "  Hello, Java World!  ";

// Länge und Prüfungen
text.length();              // 22
text.isEmpty();             // false
text.isBlank();             // false (nur Leerzeichen wäre true) [Java 11]

// Suchen
text.contains("Java");      // true
text.startsWith("  Hello"); // true
text.endsWith("!  ");       // true
text.indexOf("Java");       // 9 (erste Position)
text.lastIndexOf("o");      // 19

// Extrahieren
text.substring(8);          // "Java World!  "
text.substring(8, 12);      // "Java"
text.charAt(8);             // 'J'

// Transformieren
text.trim();                // "Hello, Java World!" (Leerzeichen vorne/hinten entfernen)
text.strip();               // "Hello, Java World!" (Unicode-bewusste Version von trim) [Java 11]
text.toUpperCase();         // "  HELLO, JAVA WORLD!  "
text.toLowerCase();         // "  hello, java world!  "
text.replace("Java", "SE"); // "  Hello, SE World!  "
text.replaceAll("\\s+", "_"); // Regex: alle Leerzeichen durch _

// Aufteilen und Verbinden
String csv = "Apfel,Birne,Kirsche";
String[] fruits = csv.split(",");  // ["Apfel", "Birne", "Kirsche"]
String joined = String.join(" - ", fruits); // "Apfel - Birne - Kirsche"

// Konvertierungen
String.valueOf(42);         // "42"
String.valueOf(3.14);       // "3.14"
Integer.parseInt("100");    // 100 (String → int)
Double.parseDouble("3.14"); // 3.14 (String → double)
```

### String-Verkettung (Concatenation)

```java
String firstName = "Max";
String lastName = "Mustermann";

// Mit +  (für einzelne Verkettungen OK)
String full = firstName + " " + lastName;

// Mit String.format()
String formatted = String.format("Name: %s %s, Alter: %d", firstName, lastName, 30);

// Mit formatted() [Java 15] — kürzer als String.format()
String msg = "Preis: %.2f EUR".formatted(19.99);

// Mit + in Schleifen VERMEIDEN → StringBuilder verwenden (s. unten)
```

---

## 2. String-Indexierung, veränderliche Texte und Text Blocks

### String-Indizierung

```java
String s = "Java";
//          0123
s.charAt(0);   // 'J'
s.charAt(3);   // 'a'
// s.charAt(4) → StringIndexOutOfBoundsException!

// Letztes Zeichen:
s.charAt(s.length() - 1);  // 'a'
```

### StringBuilder (veränderlich, nicht threadsicher)

`StringBuilder` ist die veränderliche Alternative zu `String`. Effizient für viele String-Operationen in einer Schleife.

```java
StringBuilder sb = new StringBuilder();
sb.append("Hello");
sb.append(", ");
sb.append("World");
sb.append("!");
System.out.println(sb.toString());  // "Hello, World!"

// Weitere Methoden
sb.insert(5, " Java");           // "Hello Java, World!"
sb.delete(5, 10);                // "Hello, World!"
sb.reverse();                    // "!dlroW ,olleH"
sb.replace(0, 6, "Goodbye");     // "Goodbye,olleH"
sb.length();                     // Aktuelle Länge
sb.capacity();                   // Interne Puffergröße (Standard 16)

// Method Chaining (Methodenketten)
String result = new StringBuilder()
    .append("Java")
    .append(" ")
    .append("25")
    .reverse()
    .toString();  // "52 avaJ"
```

### StringBuffer (threadsicher, langsamer)

`StringBuffer` ist wie `StringBuilder`, aber synchronisiert. Verwenden wenn mehrere Threads auf denselben Buffer zugreifen.

```java
StringBuffer buffer = new StringBuffer("Hallo");
// Gleiche API wie StringBuilder
```

### Text Blocks `[Java 15]`

> Ab **Java 17 LTS** `[Java 17]` offiziell für Produktionscode empfohlen.

Text Blocks ermöglichen mehrzeilige Strings ohne Escape-Sequenzen:

```java
// Klassisch (unübersichtlich)
String json = "{\n" +
              "  \"name\": \"Java\",\n" +
              "  \"version\": 25\n" +
              "}";

// Text Block
String json = """
        {
          "name": "Java",
          "version": 25
        }
        """;
System.out.println(json);
```

Regeln für Text Blocks:
- Öffnendes `"""` muss am Ende einer Zeile stehen (kein Inhalt dahinter)
- Einrückung wird relativ zum `"""` am Ende berechnet
- Trailing newline am Ende des Blocks

---

## 3. Wrapper-Klassen, BigDecimal und Method Chaining

### Primitive Wrapper-Klassen

Jeder primitive Typ hat eine entsprechende Klasse:

| Primitiv | Wrapper |
|---|---|
| `int` | `Integer` |
| `long` | `Long` |
| `double` | `Double` |
| `float` | `Float` |
| `char` | `Character` |
| `boolean` | `Boolean` |
| `byte` | `Byte` |
| `short` | `Short` |

### Autoboxing und Unboxing

```java
// Autoboxing: primitiv → Wrapper (automatisch)
Integer i = 42;            // int wird zu Integer
List<Integer> list = new ArrayList<>();
list.add(10);              // 10 (int) wird automatisch zu Integer(10)

// Unboxing: Wrapper → primitiv (automatisch)
int wert = i;             // Integer wird zu int
int summe = list.get(0) + 5; // Integer wird zu int addiert

// Vorsicht: NullPointerException beim Unboxing
Integer nullWert = null;
int x = nullWert;  // NullPointerException!
```

### Nützliche Methoden der Wrapper-Klassen

```java
// Integer
Integer.MAX_VALUE;          // 2147483647
Integer.MIN_VALUE;          // -2147483648
Integer.parseInt("42");     // String → int
Integer.toBinaryString(10); // "1010"
Integer.toHexString(255);   // "ff"
Integer.compare(5, 10);     // negative Zahl (5 < 10)

// Double
Double.parseDouble("3.14");
Double.isNaN(0.0 / 0.0);   // true
Double.isInfinite(1.0 / 0.0); // true
```

### BigDecimal — Präzise Dezimalzahlen

`double` und `float` sind ungeeignet für Geldbeträge, weil sie Binärbrüche verwenden:

```java
double d = 0.1 + 0.2;
System.out.println(d);  // 0.30000000000000004 ← Rundungsfehler!

// BigDecimal für exakte Berechnung
import java.math.BigDecimal;
import java.math.RoundingMode;

BigDecimal a = new BigDecimal("0.1");  // String-Konstruktor verwenden!
BigDecimal b = new BigDecimal("0.2");
BigDecimal sum = a.add(b);
System.out.println(sum);  // 0.3  ← exakt!

// Operationen
BigDecimal preis = new BigDecimal("19.99");
BigDecimal mwst  = new BigDecimal("0.19");
BigDecimal brutto = preis.multiply(mwst.add(BigDecimal.ONE));
BigDecimal gerundet = brutto.setScale(2, RoundingMode.HALF_UP);

// Vergleich
a.compareTo(b);            // -1, 0 oder 1
a.equals(new BigDecimal("0.10")); // false! (Skala unterschiedlich)
a.compareTo(new BigDecimal("0.10")) == 0; // true
```

> **Regel:** Niemals `new BigDecimal(0.1)` — immer String-Konstruktor oder `BigDecimal.valueOf(0.1)` verwenden!

---

## 4. Datum und Zeit verwalten (java.time API)

Java 8 führte das neue `java.time`-Paket ein (basierend auf Joda-Time). Es ersetzt die veralteten Klassen `Date` und `Calendar`.

### Kernklassen

| Klasse | Beschreibung |
|---|---|
| `LocalDate` | Datum ohne Zeit (z.B. 2025-06-01) |
| `LocalTime` | Zeit ohne Datum (z.B. 14:30:00) |
| `LocalDateTime` | Datum + Zeit ohne Zeitzone |
| `ZonedDateTime` | Datum + Zeit + Zeitzone |
| `Instant` | Zeitstempel (Sekunden seit 1970-01-01 UTC) |
| `Duration` | Zeitdauer (in Stunden, Minuten, Sekunden) |
| `Period` | Zeitspanne (in Jahren, Monaten, Tagen) |

### Beispiele

```java
import java.time.*;
import java.time.format.DateTimeFormatter;

// Aktuelles Datum/Zeit
LocalDate heute = LocalDate.now();
LocalTime jetzt = LocalTime.now();
LocalDateTime jetztGenau = LocalDateTime.now();
ZonedDateTime mitZone = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));

// Datum erstellen
LocalDate geburtstag = LocalDate.of(1990, 5, 15);
LocalDate geburtstag2 = LocalDate.of(1990, Month.MAY, 15);

// Datum manipulieren (Immutable: gibt neues Objekt zurück)
LocalDate morgen = heute.plusDays(1);
LocalDate naechstenMonat = heute.plusMonths(1);
LocalDate letzteWoche = heute.minusWeeks(1);

// Informationen abrufen
heute.getDayOfWeek();    // MONDAY, TUESDAY, ...
heute.getDayOfMonth();   // 1-31
heute.getMonth();        // JANUARY, FEBRUARY, ...
heute.getYear();         // 2025
heute.isLeapYear();      // true/false
heute.isBefore(morgen);  // true

// Zeitspannen
Period alter = Period.between(geburtstag, heute);
System.out.println("Alter: " + alter.getYears() + " Jahre");

Duration dauer = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
System.out.println("Arbeitsdauer: " + dauer.toHours() + " Stunden");
```

### **[Fortgeschritten]** `Instant` — maschinenlesbarer Zeitstempel

```java
// Instant: Sekunden seit 1970-01-01T00:00:00Z (Unix Epoch), immer UTC
Instant jetzt = Instant.now();
Instant epoch = Instant.EPOCH;                    // 1970-01-01T00:00:00Z
Instant spaeter = jetzt.plusSeconds(3600);        // + 1 Stunde

// Umwandlung Instant ↔ ZonedDateTime
ZoneId berlin = ZoneId.of("Europe/Berlin");
ZonedDateTime zdt = jetzt.atZone(berlin);
Instant zurück = zdt.toInstant();

// Zeitdifferenz messen (z.B. Performance)
Instant start = Instant.now();
// ... Code ...
Instant ende = Instant.now();
Duration dauer = Duration.between(start, ende);
System.out.println("Elapsed: " + dauer.toMillis() + " ms");
```

### **[Professionell]** Zeitzonen und Daylight Saving Time (DST)

Sommerzeit (DST) bedeutet: Uhren werden im Sommer **um 1 Stunde vorgestellt**. `ZonedDateTime` berücksichtigt das automatisch.

```java
ZoneId berlin = ZoneId.of("Europe/Berlin");

// Nacht der Zeitumstellung (Winterzeit → Sommerzeit): 26. März 2023, 2:00 → 3:00
// Die Stunde von 2:00 bis 3:00 existiert nicht!
ZonedDateTime vorher = ZonedDateTime.of(2023, 3, 26, 1, 30, 0, 0, berlin);
// Offset: +01:00 (Winterzeit / CET)
System.out.println(vorher.getOffset());  // +01:00

ZonedDateTime nachher = vorher.plusHours(1);
// Jetzt 3:30 Uhr (Sommerzeit!) — Java überspringt die nicht-existierende Stunde
System.out.println(nachher);             // 2023-03-26T03:30+02:00[Europe/Berlin]
System.out.println(nachher.getOffset()); // +02:00 (Sommerzeit / CEST)

// Rückkehr zur Normalzeit (Sommerzeit → Winterzeit): 29. Oktober 2023, 3:00 → 2:00
// Die Stunde von 2:00 bis 3:00 existiert zweimal!
ZonedDateTime ambig = ZonedDateTime.of(2023, 10, 29, 2, 30, 0, 0, berlin);
// Java wählt die erste Variante (Sommerzeit)
System.out.println(ambig.getOffset());  // +02:00
// Zur zweiten Variante (Winterzeit) wechseln:
ZonedDateTime winter = ambig.withLaterOffsetAtOverlap();
System.out.println(winter.getOffset()); // +01:00

// Zeitzone ermitteln und konvertieren
ZonedDateTime nyTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
ZonedDateTime berlinTime = nyTime.withZoneSameInstant(berlin);
// Selber Instant, andere Darstellung
System.out.println(nyTime.toInstant().equals(berlinTime.toInstant())); // true

// Alle verfügbaren Zeitzonen
Set<String> zonen = ZoneId.getAvailableZoneIds();  // 600+ Einträge
```

**Wichtige Regeln:**
| Situation | Java-Verhalten |
|---|---|
| Zeitumstellung vorwärts (Lücke) | übersprungene Zeit → nächste gültige Zeit |
| Zeitumstellung rückwärts (Überlappung) | `withEarlierOffsetAtOverlap()` / `withLaterOffsetAtOverlap()` |
| `LocalDateTime` + ZoneId | Mehrdeutigkeit möglich → ZonedDateTime |
| `Instant` | immer UTC, keine DST-Probleme |

### Formatierung und Parsing

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

// Datum → String
LocalDate date = LocalDate.of(2025, 6, 1);
String text = date.format(formatter);  // "01.06.2025"

// String → Datum
LocalDate parsed = LocalDate.parse("01.06.2025", formatter);

// Vordefinierte Formatter
DateTimeFormatter.ISO_LOCAL_DATE;        // 2025-06-01
DateTimeFormatter.ISO_LOCAL_DATE_TIME;   // 2025-06-01T14:30:00
```

---

## 5. Lokalisierung und Formatierung

### Locale

`Locale` repräsentiert eine Sprache und optionales Land:

```java
import java.util.Locale;

Locale deutsch = Locale.GERMAN;           // Sprache: Deutsch
Locale germany = Locale.GERMANY;          // Sprache: Deutsch, Land: Deutschland
Locale us = Locale.US;                    // Sprache: Englisch, Land: USA
Locale custom = new Locale("de", "AT");   // Deutsch, Österreich

// Systemlocale
Locale.getDefault();
```

### Zahlen formatieren (NumberFormat)

```java
import java.text.NumberFormat;
import java.util.Locale;

double preis = 12345.678;

// Zahl formatieren
NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
System.out.println(nf.format(preis));       // "12.345,678"

// Währung formatieren
NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
System.out.println(currency.format(preis)); // "12.345,68 €"

// Prozent formatieren
NumberFormat pct = NumberFormat.getPercentInstance(Locale.US);
System.out.println(pct.format(0.1234));    // "12%"

// String → Zahl (Parsing)
Number parsed = nf.parse("12.345,678");    // Vorsicht: wirft ParseException
```

### Datum/Zeit formatieren mit Locale

```java
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

LocalDate date = LocalDate.of(2025, 6, 1);

DateTimeFormatter deFormatter = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.FULL)
    .withLocale(Locale.GERMANY);
System.out.println(date.format(deFormatter)); // "Sonntag, 1. Juni 2025"

DateTimeFormatter usFormatter = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.FULL)
    .withLocale(Locale.US);
System.out.println(date.format(usFormatter)); // "Sunday, June 1, 2025"
```

### MessageFormat — Lokalisierte Nachrichten

```java
import java.text.MessageFormat;

String template = "Hallo {0}! Du hast {1} Nachrichten.";
String msg = MessageFormat.format(template, "Anna", 5);
System.out.println(msg);  // "Hallo Anna! Du hast 5 Nachrichten."

// Mit ResourceBundle für echte Lokalisierung
// messages_de.properties: greeting=Hallo {0}!
// messages_en.properties: greeting=Hello {0}!
ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMANY);
String greeting = bundle.getString("greeting");
System.out.println(MessageFormat.format(greeting, "Anna"));
```

---

## Übungsaufgaben

### Practice 3-1: Explore String and StringBuilder Objects (ca. 33 Minuten)

**Ziel:** String-Methoden und StringBuilder in der Praxis anwenden.

**Aufgaben:**

1. Deklariere folgende Strings und führe die angegebenen Operationen durch:
   ```java
   String s1 = "Java SE 25 Programming";
   ```
   - Gib die Länge aus
   - Konvertiere in Großbuchstaben und Kleinbuchstaben
   - Extrahiere "SE 25" mit `substring()`
   - Ersetze "25" durch "17"
   - Prüfe ob `s1` mit "Java" beginnt
   - Finde die Position von "Programming"

2. Vergleiche folgende zwei Strings und erkläre das Ergebnis:
   ```java
   String a = "Hallo";
   String b = "Hallo";
   String c = new String("Hallo");
   System.out.println(a == b);      // Was kommt raus und warum?
   System.out.println(a == c);      // Was kommt raus und warum?
   System.out.println(a.equals(c)); // Was kommt raus und warum?
   ```

3. Erstelle einen `StringBuilder` und baue folgenden String zusammen:
   `"Mein Name ist [Vorname] [Nachname] und ich bin [Alter] Jahre alt."`
   Nutze `append()`, `insert()` und `replace()`.

4. Vergleiche die Performance: Erstelle in einer Schleife (10.000 Iterationen) einen langen String einmal mit `+` und einmal mit `StringBuilder`. Messe die Zeit mit `System.currentTimeMillis()`.

5. Erstelle einen Text Block, der JSON-Inhalt enthält:
   ```json
   {
     "produkt": "Kaffee",
     "preis": 4.99,
     "verfuegbar": true
   }
   ```

---

### Practice 3-2: Use BigDecimal Class and Format Numeric Values (ca. 12 Minuten)

**Ziel:** Präzise Dezimalberechnungen mit BigDecimal; Zahlen lokalisiert formatieren.

**Aufgaben:**

1. Demonstriere den Rundungsfehler von `double`:
   ```java
   System.out.println(0.1 + 0.2);           // Was kommt raus?
   System.out.println(0.1 + 0.2 == 0.3);    // Was kommt raus?
   ```

2. Berechne dasselbe mit `BigDecimal` korrekt:
   ```java
   BigDecimal a = new BigDecimal("0.1");
   BigDecimal b = new BigDecimal("0.2");
   // Addiere, subtrahiere, multipliziere und dividiere
   // Runde auf 2 Nachkommastellen mit HALF_UP
   ```

3. Berechne einen Einkaufspreis mit Mehrwertsteuer:
   ```java
   BigDecimal nettoPreis = new BigDecimal("99.99");
   BigDecimal mwstSatz = new BigDecimal("0.19");
   // Berechne den Bruttopreis, gerundet auf 2 Stellen
   ```

4. Formatiere die Zahl `1234567.89` als:
   - Deutsche Zahl: `1.234.567,89`
   - US-Zahl: `1,234,567.89`
   - Deutsche Währung: `1.234.567,89 €`
   - Prozentwert aus `0.1234`: `12%` (DE) und `12%` (US)

---

### Practice 3-3: Use and Format Date and Time Values (ca. 17 Minuten)

**Ziel:** java.time API anwenden; Datum/Zeit formatieren und parsen.

**Aufgaben:**

1. Erstelle das aktuelle Datum und die aktuelle Uhrzeit und gib beides aus.

2. Berechne:
   - Das Datum in 100 Tagen
   - Das Datum vor 3 Monaten
   - Den Wochentag deines Geburtstags in diesem Jahr
   - Wie viele Tage noch bis zum 31.12. dieses Jahres

3. Berechne das Alter einer Person (geboren am 15.03.1990) in Jahren, Monaten und Tagen.

4. Formatiere das aktuelle Datum in folgende Formate:
   - `"dd.MM.yyyy"` → z.B. `01.06.2025`
   - `"EEEE, d. MMMM yyyy"` (Deutsch) → z.B. `Sonntag, 1. Juni 2025`
   - `"MM/dd/yyyy"` (US) → z.B. `06/01/2025`

5. Parse folgende Strings zu `LocalDate`-Objekten:
   ```java
   "2025-12-31"       // ISO Format
   "31.12.2025"       // Deutsches Format
   "December 31, 2025" // Englisches Format
   ```

---

### Practice 3-4: Apply Localization and Format Messages (ca. 13 Minuten)

**Ziel:** Anwendungen für verschiedene Sprachen und Länder konfigurieren.

**Aufgaben:**

1. Gib die aktuelle System-Locale aus und ändere sie temporär auf `Locale.US`.

2. Erstelle und vergleiche Formatierungen für denselben Betrag (`2999.99`) in folgenden Locales:
   - `Locale.GERMANY`
   - `Locale.US`
   - `Locale.JAPAN`
   - `Locale.UK`

3. Erstelle ein einfaches `ResourceBundle`-System:
   - Erstelle `messages_de.properties` mit: `welcome=Willkommen, {0}!`
   - Erstelle `messages_en.properties` mit: `welcome=Welcome, {0}!`
   - Lade je nach gewählter Locale das richtige Bundle und formatiere die Nachricht

4. Formatiere folgendes Datum auf Deutsch und auf Englisch im `FormatStyle.LONG`-Stil:
   ```java
   LocalDate.of(2025, 6, 1)
   // Deutsch:  "1. Juni 2025"
   // Englisch: "June 1, 2025"
   ```

---

## Multiple-Choice-Fragen

**Frage 1:** Was gibt folgender Code aus?
```java
String a = "Hallo";
String b = new String("Hallo");
System.out.println(a == b);
System.out.println(a.equals(b));
```

- A) `true` / `true`
- B) `true` / `false`
- C) **`false` / `true`** ✓
- D) `false` / `false`

> *`==` vergleicht Referenzen — `a` liegt im String Pool, `b` ist ein neues Heap-Objekt. `.equals()` vergleicht den Inhalt.*

---

**Frage 2:** Wann sollte `StringBuilder` statt String-Verkettung mit `+` verwendet werden?

- A) Immer, auch für einzelne Verkettungen
- B) Nie, `+` ist immer schneller
- C) Nur bei Strings mit mehr als 1000 Zeichen
- D) **Bei vielen Verkettungen (z.B. in Schleifen), da `+` jedes Mal ein neues String-Objekt erzeugt** ✓

---

**Frage 3:** Welches Ergebnis hat `0.1 + 0.2 == 0.3` in Java?

- A) `true`
- B) **`false`** ✓
- C) `0.30000000000000004`
- D) Eine `ArithmeticException`

> *`double` verwendet Binärdarstellung, die 0.1 nicht exakt darstellen kann. Für präzise Berechnungen: `BigDecimal`.*

---

**Frage 4:** Wie erstellt man korrekt einen `BigDecimal` mit dem Wert 0.1?

- A) `new BigDecimal(0.1)`
- B) `BigDecimal.create(0.1)`
- C) **`new BigDecimal("0.1")`** ✓
- D) `BigDecimal.of("0.1")`

> *`new BigDecimal(0.1)` übernimmt den Binär-Rundungsfehler von `double`. Der String-Konstruktor liefert den exakten Wert.*

---

**Frage 5:** Was ist der Unterschied zwischen `String.strip()` und `String.trim()`? `[Java 11]`

- A) Kein Unterschied
- B) `trim()` ist Unicode-bewusst, `strip()` entfernt nur ASCII
- C) **`strip()` ist Unicode-bewusst und entfernt alle Unicode-Leerzeichen; `trim()` entfernt nur Zeichen ≤ U+0020** ✓
- D) `strip()` entfernt alle Leerzeichen im gesamten String

---

**Frage 6:** Was ist ein Text Block? `[Java 15]`

- A) Ein unveränderlicher `StringBuilder`
- B) Ein Kommentarblock in Java
- C) Ein String mit mehr als 100 Zeichen
- D) **Ein mehrzeiliges String-Literal mit `"""`, das Escape-Sequenzen weitgehend überflüssig macht** ✓

---

**Frage 7:** Welche Klasse repräsentiert ein Datum **mit** Uhrzeit, aber **ohne** Zeitzone?

- A) `LocalDate`
- B) `ZonedDateTime`
- C) **`LocalDateTime`** ✓
- D) `Instant`

---

**Frage 8:** Was ist der Unterschied zwischen `Period` und `Duration`?

- A) Es gibt keinen Unterschied
- B) `Period` misst in Millisekunden, `Duration` in Tagen
- C) **`Period` misst in Jahren/Monaten/Tagen; `Duration` in Stunden/Minuten/Sekunden** ✓
- D) `Duration` ist für Datum, `Period` für Uhrzeit

---

**Frage 9:** Was macht `NumberFormat.getCurrencyInstance(Locale.GERMANY).format(1234.5)`?

- A) `"1234.50 EUR"`
- B) `"1.234,50"`
- C) **`"1.234,50 €"` (oder ähnlich je nach JVM-Version)** ✓
- D) Eine `ParseException`

---

**Frage 10:** Was ist der Zweck eines `ResourceBundle`?

- A) Externe Bibliotheken einbinden
- B) Dateien lesen und schreiben
- C) **Texte und Meldungen für verschiedene Sprachen/Locales zentral verwalten** ✓
- D) HTTP-Anfragen senden

---

**Frage 11:** Was passiert bei einer Zeitumstellung (Winterzeit → Sommerzeit), wenn man `ZonedDateTime.plusHours(1)` auf 1:30 Uhr anwendet?

- A) Eine `DateTimeException` wird geworfen
- B) Das Ergebnis ist 2:30 Uhr (existiert nicht — Lücke)
- C) **Java überspringt die Lücke automatisch und gibt 3:30 Uhr (Sommerzeit) zurück** ✓
- D) Das Ergebnis bleibt 1:30 Uhr

---

**Frage 12:** Was ist der Unterschied zwischen `ZonedDateTime` und `Instant`?

- A) `Instant` kennt Zeitzonen, `ZonedDateTime` nicht
- B) Beide sind identisch
- C) **`Instant` ist ein UTC-Zeitstempel ohne Zeitzone; `ZonedDateTime` enthält Datum, Zeit und Zeitzone** ✓
- D) `ZonedDateTime` ist für die Systemzeit, `Instant` für externe Quellen

---

**Frage 13:** Mit welcher Methode wechselt man bei einer Zeitüberlappung (Sommerzeit → Winterzeit) zur späteren Variante?

- A) `adjustToWinterTime()`
- B) `withZoneSameInstant()`
- C) `normalizedOffset()`
- D) **`withLaterOffsetAtOverlap()`** ✓

---

**Frage 14:** Was gibt `ZonedDateTime.getOffset()` zurück?

- A) Den Namen der Zeitzone (`"Europe/Berlin"`)
- B) Die DST-Differenz in Minuten
- C) **Den aktuellen UTC-Offset zum Zeitpunkt dieses ZonedDateTime (z.B. `+02:00`)** ✓
- D) Immer `+00:00`

---

## Skill Check: Typische Prüfungsfragen

1. Warum sind Strings in Java unveränderlich (immutable)? Was sind die Vorteile?
2. Was ist der Unterschied zwischen `==` und `.equals()` bei Strings?
3. Wann sollte man `StringBuilder` statt `String`-Verkettung verwenden?
4. Was ist der Unterschied zwischen `StringBuffer` und `StringBuilder`?
5. Warum eignet sich `double` nicht für Geldbeträge?
6. Was ist der richtige Weg, einen `BigDecimal` mit dem Wert `0.1` zu erstellen?
7. Welche Klasse repräsentiert ein Datum ohne Uhrzeit in der modernen Java-API?
8. Was ist der Unterschied zwischen `Period` und `Duration`?
9. Was ist eine `Locale` und wozu dient sie?
10. Was ist `Instant` und wann verwendet man es statt `ZonedDateTime`?
11. Was passiert bei einer Lücke (Winterzeit → Sommerzeit) wenn man `ZonedDateTime.plusHours(1)` aufruft?
12. Was ist der Unterschied zwischen `withEarlierOffsetAtOverlap()` und `withLaterOffsetAtOverlap()`?
10. Was macht `MessageFormat.format()`?
