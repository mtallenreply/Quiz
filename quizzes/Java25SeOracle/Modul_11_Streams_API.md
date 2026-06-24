# Modul 11: Streams API

## Übersicht

Die Streams API ist seit Java 8 ein zentrales Werkzeug für die deklarative, funktionale Verarbeitung von Datenmengen. Streams ermöglichen es, Operationen wie Filtern, Transformieren und Aggregieren in übersichtlichen, verkettbaren Pipelines auszudrücken. Dieses Modul behandelt alle wichtigen Aspekte der Streams API von der Erzeugung bis zur Parallelverarbeitung.

| Thema | Dauer |
|---|---|
| Stream Concepts | 18 min |
| Creating Streams | 11 min |
| Intermediate Operations | 15 min |
| Terminal Operations | 6 min |
| Collectors | 12 min |
| Optional | 12 min |
| Parallel Streams | 13 min |
| Practice 11-1 | 31 min |
| Practice 11-2 | 10 min |
| **Skill Check: Streams** | **mind. 80%** |

---

## 1. Stream-Konzepte und Grundlagen

### 1.1 Was ist ein Stream?

Ein Stream ist keine Datenstruktur, sondern eine Sequenz von Elementen, auf die eine Folge von Operationen angewendet werden kann. Wichtige Eigenschaften:

- **Kein Speicher**: Ein Stream speichert keine Daten – er verarbeitet sie
- **Funktional**: Operationen erzeugen neue Streams, die Quelle bleibt unverändert
- **Lazy Evaluation**: Zwischenoperationen werden erst bei der Terminaloperation ausgeführt
- **Einmalig nutzbar**: Ein Stream kann nur einmal konsumiert werden
- **Potentiell unbegrenzt**: Streams können unendliche Sequenzen repräsentieren

```java
import java.util.*;
import java.util.stream.*;

public class StreamKonzepte {
    public static void main(String[] args) {
        List<Integer> zahlen = Arrays.asList(5, 3, 8, 1, 9, 2, 7, 4, 6);

        // Stream-Pipeline: Quelle -> Zwischenoperationen -> Terminaloperation
        int summeGeraderZahlenÜber4 = zahlen.stream()     // Quelle
            .filter(n -> n % 2 == 0)                       // Zwischenop: Filtern
            .filter(n -> n > 4)                            // Zwischenop: Filtern
            .mapToInt(Integer::intValue)                   // Zwischenop: Transformieren
            .sum();                                        // Terminalop: Aggregieren

        System.out.println("Summe: " + summeGeraderZahlenÜber4); // 8 + 6 = 14

        // Lazy Evaluation - ohne Terminaloperation passiert NICHTS:
        Stream<Integer> stream = zahlen.stream()
            .filter(n -> {
                System.out.println("Prüfe: " + n);
                return n > 5;
            });
        // Noch keine Ausgabe! filter wurde noch nicht ausgeführt.

        System.out.println("Stream erstellt, aber noch nicht ausgeführt");
        long anzahl = stream.count(); // Jetzt wird filter für alle Elemente ausgeführt
        System.out.println("Anzahl über 5: " + anzahl);

        // Streams sind einmalig verwendbar:
        Stream<String> einmaliger = Stream.of("A", "B", "C");
        einmaliger.forEach(System.out::println);
        // einmaliger.count(); // IllegalStateException: Stream already operated upon!
    }
}
```

### 1.2 Stream vs. Collection

| Merkmal | Collection | Stream |
|---|---|---|
| Datenspeicherung | Ja (intern) | Nein (Verarbeitungspipeline) |
| Wiederverwendung | Ja | Nein (einmalig) |
| Externe Iteration | `for-each`, `iterator()` | Nicht direkt |
| Interne Iteration | - | `forEach`, `filter`, ... |
| Lazy Evaluation | Nein | Ja (Zwischenoperationen) |
| Parallelisierung | Manuell | `parallelStream()` |
| Elemente hinzufügen | Ja | Nein |
| Größe bekannt | Ja | Nicht immer (unendliche Streams) |

### 1.3 Die Stream-Pipeline

```
Quelle       ->   Zwischenoperationen       ->   Terminaloperation
(Source)          (Intermediate Operations)       (Terminal Operation)

Collection       filter()                         collect()
Array            map()                            forEach()
IO-Kanal         sorted()                         reduce()
Generator         distinct()                       count()
...              limit()                          findFirst()
                 skip()                           anyMatch()
                 flatMap()                        ...
                 peek()

Erzeugt Stream   Erzeugen neuen Stream            Erzeugt Ergebnis
(lazy)           (lazy)                           (eagerly ausgeführt)
```

---

## 2. Streams erstellen

### 2.1 Streams aus Collections und Arrays

```java
import java.util.*;
import java.util.stream.*;

public class StreamErstellen {
    public static void main(String[] args) {
        // Aus Collection
        List<String> liste = Arrays.asList("A", "B", "C");
        Stream<String> ausListe = liste.stream();
        Stream<String> parallelAusListe = liste.parallelStream();

        // Aus Set
        Set<Integer> menge = new HashSet<>(Arrays.asList(1, 2, 3));
        Stream<Integer> ausMenge = menge.stream();

        // Aus Array
        String[] array = {"X", "Y", "Z"};
        Stream<String> ausArray = Arrays.stream(array);
        Stream<String> ausArrayTeil = Arrays.stream(array, 1, 3); // Index 1 bis 2

        // Stream.of()
        Stream<String> vonOf = Stream.of("Eins", "Zwei", "Drei");

        // Primitive Streams
        IntStream intStream = IntStream.of(1, 2, 3, 4, 5);
        LongStream longStream = LongStream.of(1L, 2L, 3L);
        DoubleStream doubleStream = DoubleStream.of(1.1, 2.2, 3.3);

        // IntStream.range und rangeClosed
        IntStream range = IntStream.range(1, 6);      // 1, 2, 3, 4, 5 (exklusiv 6)
        IntStream rangeClosed = IntStream.rangeClosed(1, 5); // 1, 2, 3, 4, 5 (inklusiv 5)

        System.out.print("range: ");
        range.forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.print("rangeClosed: ");
        rangeClosed.forEach(n -> System.out.print(n + " "));
        System.out.println();

        // LongStream.range und rangeClosed (analog zu IntStream, für long-Werte)
        LongStream longRange = LongStream.range(1L, 6L);         // 1, 2, 3, 4, 5
        LongStream longRangeClosed = LongStream.rangeClosed(1L, 5L); // 1, 2, 3, 4, 5

        System.out.print("LongStream range: ");
        longRange.forEach(n -> System.out.print(n + " "));
        System.out.println();

        // LongStream summaryStatistics
        LongSummaryStatistics longStats = LongStream.rangeClosed(1L, 10L)
            .summaryStatistics();
        System.out.println("LongStream Stats - Min: " + longStats.getMin()
            + ", Max: " + longStats.getMax()
            + ", Sum: " + longStats.getSum()
            + ", Avg: " + longStats.getAverage()
            + ", Count: " + longStats.getCount());

        // DoubleStream summaryStatistics
        DoubleSummaryStatistics doubleStats = DoubleStream.of(1.5, 2.5, 3.0, 4.0, 5.0)
            .summaryStatistics();
        System.out.printf("DoubleStream Stats - Min: %.1f, Max: %.1f, Sum: %.1f, Avg: %.2f%n",
            doubleStats.getMin(), doubleStats.getMax(),
            doubleStats.getSum(), doubleStats.getAverage());

        // DoubleStream.generate und limit
        DoubleStream zufallsDoubles = DoubleStream.generate(Math::random).limit(5);
        System.out.print("5 zufällige Doubles: ");
        zufallsDoubles.forEach(d -> System.out.printf("%.3f ", d));
        System.out.println();

        // Boxen: primitiver Stream -> Object Stream
        Stream<Integer> geboxed = IntStream.range(1, 4).boxed();
        List<Integer> geboxedListe = geboxed.collect(Collectors.toList());
        System.out.println("Geboxed: " + geboxedListe);

        // Map: Stream als Schlüssel/Wert-Quellen
        Map<String, Integer> map = Map.of("A", 1, "B", 2, "C", 3);
        map.entrySet().stream()
            .forEach(e -> System.out.println(e.getKey() + "=" + e.getValue()));
    }
}
```

### 2.2 Unendliche Streams und Stream-Generatoren

```java
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class UnendlicheStreams {
    public static void main(String[] args) {
        // Stream.generate() - unendlich, kein Zustand
        Stream<Double> zufallsZahlen = Stream.generate(Math::random);
        System.out.println("5 Zufallszahlen:");
        zufallsZahlen
            .limit(5)
            .forEach(n -> System.out.printf("  %.4f%n", n));

        // Stream.iterate() - unendlich, mit Zustand (Java 8)
        Stream<Integer> potenzen = Stream.iterate(1, n -> n * 2);
        System.out.println("Potenzen von 2:");
        potenzen
            .limit(10)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Stream.iterate() mit Bedingung (Java 9+)
        Stream<Integer> bisHundert = Stream.iterate(1, n -> n <= 100, n -> n * 2);
        System.out.println("Potenzen bis 100:");
        bisHundert.forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Fibonacci mit iterate
        Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
            .limit(10)
            .map(f -> f[0])
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Stream.empty()
        Stream<String> leer = Stream.empty();
        System.out.println("Leer: " + leer.count()); // 0

        // Stream.concat()
        Stream<String> erster = Stream.of("A", "B");
        Stream<String> zweiter = Stream.of("C", "D");
        Stream<String> zusammen = Stream.concat(erster, zweiter);
        System.out.println("Zusammen: " + zusammen.collect(Collectors.toList()));

        // Aus String-Zeilen (Java 11+)
        String text = "Zeile 1\nZeile 2\nZeile 3";
        text.lines().forEach(System.out::println);
    }
}
```

---

## 3. Intermediate Operations (Zwischenoperationen)

### 3.1 filter, map, mapToXxx

```java
import java.util.*;
import java.util.stream.*;

public class FilterMapOperationen {
    record Mitarbeiter(String name, String abteilung, double gehalt) {}

    public static void main(String[] args) {
        List<Mitarbeiter> mitarbeiter = List.of(
            new Mitarbeiter("Alice", "IT", 85_000),
            new Mitarbeiter("Bob", "HR", 55_000),
            new Mitarbeiter("Charlie", "IT", 92_000),
            new Mitarbeiter("Diana", "Finanzen", 78_000),
            new Mitarbeiter("Eve", "IT", 71_000),
            new Mitarbeiter("Frank", "HR", 48_000)
        );

        // filter: Elemente behalten, die Predicate erfüllen
        System.out.println("IT-Mitarbeiter:");
        mitarbeiter.stream()
            .filter(m -> m.abteilung().equals("IT"))
            .forEach(m -> System.out.println("  " + m.name()));

        // map: Elemente transformieren (Stream<T> -> Stream<R>)
        System.out.println("Namen aller Mitarbeiter:");
        mitarbeiter.stream()
            .map(Mitarbeiter::name)
            .forEach(System.out::println);

        // map mit Transformation
        System.out.println("Gehälter mit 10% Erhöhung:");
        mitarbeiter.stream()
            .filter(m -> m.abteilung().equals("IT"))
            .map(m -> m.name() + ": " + (m.gehalt() * 1.1))
            .forEach(System.out::println);

        // mapToInt / mapToLong / mapToDouble -> primitive Streams
        IntSummaryStatistics stats = mitarbeiter.stream()
            .mapToInt(m -> (int) m.gehalt())
            .summaryStatistics();

        System.out.println("Gehaltsstatistik:");
        System.out.println("  Min:   " + stats.getMin());
        System.out.println("  Max:   " + stats.getMax());
        System.out.printf("  Avg:   %.2f%n", stats.getAverage());
        System.out.println("  Sum:   " + stats.getSum());
        System.out.println("  Count: " + stats.getCount());

        // mapToDouble für double-Werte
        OptionalDouble durchschnitt = mitarbeiter.stream()
            .mapToDouble(Mitarbeiter::gehalt)
            .average();
        durchschnitt.ifPresent(d -> System.out.printf("Durchschnitt: %.2f%n", d));
    }
}
```

### 3.2 flatMap

`flatMap` wandelt jedes Element in einen Stream um und verbindet alle diese Streams zu einem einzigen.

```java
import java.util.*;
import java.util.stream.*;

public class FlatMapDemo {
    public static void main(String[] args) {
        // map vs flatMap
        List<List<Integer>> verschachtelt = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9)
        );

        // map -> Stream<Stream<Integer>> (nicht erwünscht)
        Stream<Stream<Integer>> mitMap = verschachtelt.stream()
            .map(List::stream);

        // flatMap -> Stream<Integer> (flach)
        List<Integer> flach = verschachtelt.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        System.out.println("Flach: " + flach); // [1, 2, 3, 4, 5, 6, 7, 8, 9]

        // Praktisches Beispiel: Wörter aus Sätzen extrahieren
        List<String> saetze = Arrays.asList(
            "Java ist toll",
            "Streams sind mächtig",
            "Lambda vereinfacht Code"
        );

        List<String> worte = saetze.stream()
            .flatMap(satz -> Arrays.stream(satz.split(" ")))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Alle Wörter: " + worte);

        // flatMapToInt für primitive Streams
        List<int[]> arrays = Arrays.asList(
            new int[]{1, 2, 3},
            new int[]{4, 5},
            new int[]{6, 7, 8}
        );

        int summe = arrays.stream()
            .flatMapToInt(Arrays::stream)
            .sum();
        System.out.println("Summe: " + summe); // 36

        // Komplexeres Beispiel: Bestellungen mit Positionen
        record Position(String artikel, int menge) {}
        record Bestellung(String kunde, List<Position> positionen) {}

        List<Bestellung> bestellungen = List.of(
            new Bestellung("Müller", List.of(
                new Position("Laptop", 1),
                new Position("Maus", 2)
            )),
            new Bestellung("Schmidt", List.of(
                new Position("Tastatur", 1),
                new Position("Monitor", 1),
                new Position("Maus", 1)
            ))
        );

        // Alle Artikel (mit flatMap über Positionen)
        List<String> alleArtikel = bestellungen.stream()
            .flatMap(b -> b.positionen().stream())
            .map(Position::artikel)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Alle Artikel: " + alleArtikel);
    }
}
```

### 3.3 sorted, distinct, limit, skip, peek

```java
import java.util.*;
import java.util.stream.*;

public class WeitereZwischenoperationen {
    public static void main(String[] args) {
        List<Integer> zahlen = Arrays.asList(5, 3, 8, 1, 9, 2, 7, 4, 6, 3, 5, 1);

        // distinct: Duplikate entfernen
        System.out.println("Duplikatfrei:");
        zahlen.stream()
            .distinct()
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // sorted: natürliche Ordnung
        System.out.println("Sortiert:");
        zahlen.stream()
            .distinct()
            .sorted()
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // sorted: mit Comparator
        System.out.println("Absteigend:");
        zahlen.stream()
            .distinct()
            .sorted(Comparator.reverseOrder())
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // limit: erste N Elemente
        System.out.println("Erste 3:");
        zahlen.stream()
            .distinct()
            .sorted()
            .limit(3)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // skip: erste N überspringen
        System.out.println("Alle außer ersten 3:");
        zahlen.stream()
            .distinct()
            .sorted()
            .skip(3)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Paginierung mit skip + limit
        int seite = 1;      // Seite 2 (0-basiert)
        int seitenGroesse = 3;
        System.out.println("Seite " + (seite + 1) + " (Größe " + seitenGroesse + "):");
        zahlen.stream()
            .distinct()
            .sorted()
            .skip((long) seite * seitenGroesse)
            .limit(seitenGroesse)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // peek: Debugging ohne Terminierung (führt keine Terminaloperation aus)
        System.out.println("Mit peek-Debugging:");
        long ergebnis = zahlen.stream()
            .peek(n -> System.out.print("[nach quelle:" + n + "] "))
            .filter(n -> n > 4)
            .peek(n -> System.out.print("[nach filter:" + n + "] "))
            .distinct()
            .peek(n -> System.out.print("[nach distinct:" + n + "] "))
            .count();
        System.out.println("\nAnzahl: " + ergebnis);

        // takeWhile und dropWhile (Java 9+)
        List<Integer> sortiert = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        System.out.println("takeWhile(< 5):");
        sortiert.stream()
            .takeWhile(n -> n < 5)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.println("dropWhile(< 5):");
        sortiert.stream()
            .dropWhile(n -> n < 5)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();
    }
}
```

---

## 4. Terminal Operations (Terminaloperationen)

### 4.1 forEach, count, findFirst, findAny

```java
import java.util.*;
import java.util.stream.*;

public class TerminalOperationen {
    public static void main(String[] args) {
        List<String> namen = Arrays.asList("Alice", "Bob", "Charlie", "Diana", "Eve");

        // forEach: Seiteneffekte für jedes Element
        System.out.println("forEach:");
        namen.stream().forEach(System.out::println);

        // forEachOrdered: bei Parallel-Streams Reihenfolge erhalten
        namen.parallelStream().forEachOrdered(System.out::println);

        // count: Anzahl der Elemente
        long anzahl = namen.stream()
            .filter(n -> n.length() > 3)
            .count();
        System.out.println("Namen länger als 3: " + anzahl); // 3

        // findFirst: erstes Element (Optional)
        Optional<String> erster = namen.stream()
            .filter(n -> n.startsWith("C"))
            .findFirst();
        erster.ifPresent(n -> System.out.println("Erster mit C: " + n)); // Charlie

        // findAny: beliebiges Element (nützlicher bei Parallel-Streams)
        Optional<String> beliebiger = namen.stream()
            .filter(n -> n.length() == 3)
            .findAny();
        beliebiger.ifPresent(n -> System.out.println("Einer mit 3 Zeichen: " + n)); // Bob

        // min und max
        Optional<String> kuerzester = namen.stream()
            .min(Comparator.comparingInt(String::length));
        Optional<String> laengster = namen.stream()
            .max(Comparator.comparingInt(String::length));

        System.out.println("Kürzester: " + kuerzester.orElse("N/A")); // Bob
        System.out.println("Längster:  " + laengster.orElse("N/A"));  // Charlie

        // toArray
        String[] array = namen.stream()
            .filter(n -> n.length() > 3)
            .toArray(String[]::new);
        System.out.println("Array: " + Arrays.toString(array));
    }
}
```

### 4.2 anyMatch, allMatch, noneMatch

```java
import java.util.*;
import java.util.stream.*;

public class MatchOperationen {
    record Produkt(String name, double preis, boolean verfügbar) {}

    public static void main(String[] args) {
        List<Produkt> produkte = List.of(
            new Produkt("Laptop", 999.99, true),
            new Produkt("Maus", 29.99, false),
            new Produkt("Tastatur", 79.99, true),
            new Produkt("Monitor", 349.99, true),
            new Produkt("Headset", 149.99, false)
        );

        // anyMatch: gibt true zurück wenn mindestens ein Element passt (short-circuit)
        boolean irgendwasTeuer = produkte.stream()
            .anyMatch(p -> p.preis() > 500);
        System.out.println("Irgendetwas teuer: " + irgendwasTeuer); // true

        boolean irgendwasGünstig = produkte.stream()
            .anyMatch(p -> p.preis() < 20);
        System.out.println("Irgendetwas günstig: " + irgendwasGünstig); // false

        // allMatch: gibt true zurück wenn ALLE Elemente passen (short-circuit)
        boolean alleVerfügbar = produkte.stream()
            .allMatch(Produkt::verfügbar);
        System.out.println("Alle verfügbar: " + alleVerfügbar); // false

        boolean allePositiverPreis = produkte.stream()
            .allMatch(p -> p.preis() > 0);
        System.out.println("Alle positiver Preis: " + allePositiverPreis); // true

        // noneMatch: gibt true zurück wenn KEIN Element passt (short-circuit)
        boolean keinesGratiis = produkte.stream()
            .noneMatch(p -> p.preis() == 0);
        System.out.println("Keines gratis: " + keinesGratiis); // true

        boolean keinesMitPreisUnter10 = produkte.stream()
            .noneMatch(p -> p.preis() < 10);
        System.out.println("Keines unter 10: " + keinesMitPreisUnter10); // true

        // Leere Streams: allMatch=true, anyMatch=false, noneMatch=true
        System.out.println("Leerer Stream allMatch:  " + Stream.empty().allMatch(x -> false)); // true
        System.out.println("Leerer Stream anyMatch:  " + Stream.empty().anyMatch(x -> true));  // false
        System.out.println("Leerer Stream noneMatch: " + Stream.empty().noneMatch(x -> true)); // true
    }
}
```

### 4.3 reduce

```java
import java.util.*;
import java.util.stream.*;

public class ReduceDemo {
    public static void main(String[] args) {
        List<Integer> zahlen = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // reduce mit Identitätswert und BinaryOperator
        int summe = zahlen.stream()
            .reduce(0, (a, b) -> a + b);
        System.out.println("Summe: " + summe); // 55

        // reduce mit Methodenreferenz
        int produkt = zahlen.stream()
            .reduce(1, (a, b) -> a * b);
        System.out.println("Produkt: " + produkt); // 3628800

        // reduce ohne Identitätswert -> Optional (kann leer sein)
        Optional<Integer> max = zahlen.stream()
            .reduce((a, b) -> a > b ? a : b);
        max.ifPresent(m -> System.out.println("Max: " + m)); // 10

        // Leerer Stream ohne Identitätswert -> Empty Optional
        Optional<Integer> leeresSumme = Stream.<Integer>empty()
            .reduce((a, b) -> a + b);
        System.out.println("Leeres Reduce: " + leeresSumme.isPresent()); // false

        // String-Verkettung mit reduce
        List<String> worte = Arrays.asList("Java", "Streams", "sind", "toll");
        String satz = worte.stream()
            .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
        System.out.println("Satz: " + satz);

        // Komplexes reduce: eigene Klasse
        record Statistik(int min, int max, long summe, int anzahl) {
            static Statistik vonElement(int element) {
                return new Statistik(element, element, element, 1);
            }
            Statistik kombinieren(Statistik andere) {
                return new Statistik(
                    Math.min(this.min, andere.min),
                    Math.max(this.max, andere.max),
                    this.summe + andere.summe,
                    this.anzahl + andere.anzahl
                );
            }
            double durchschnitt() { return (double) summe / anzahl; }
        }

        Optional<Statistik> stats = zahlen.stream()
            .map(Statistik::vonElement)
            .reduce(Statistik::kombinieren);

        stats.ifPresent(s -> {
            System.out.println("Min: " + s.min());
            System.out.println("Max: " + s.max());
            System.out.printf("Avg: %.1f%n", s.durchschnitt());
        });
    }
}
```

---

## 5. Collectors

### 5.1 Grundlegende Collectors

```java
import java.util.*;
import java.util.stream.*;

public class GrundlegendeCollectors {
    public static void main(String[] args) {
        List<String> namen = Arrays.asList("Alice", "Bob", "Charlie", "Diana", "Eve", "Bob");

        // toList() - in unveränderliche Liste (Java 16+)
        List<String> unveränderlicheListe = namen.stream()
            .distinct()
            .collect(Collectors.toList());

        // toUnmodifiableList() - garantiert unveränderlich (Java 10+)
        List<String> immutableListe = namen.stream()
            .distinct()
            .collect(Collectors.toUnmodifiableList());

        // Stream.toList() - direkte Terminaloperation seit Java 16 (kürzer als collect)
        // Gibt eine unveränderliche Liste zurück, keine null-Elemente erlaubt
        List<String> direkteListe = namen.stream()
            .distinct()
            .sorted()
            .toList(); // kein Collectors.toList() nötig!
        System.out.println("Stream.toList(): " + direkteListe);

        // Unterschied: Stream.toList() vs. Collectors.toList()
        // Stream.toList()               -> unmodifiable (UnsupportedOperationException bei add/remove)
        // Collectors.toList()           -> Implementierungsabhängig (meist mutable ArrayList)
        // Collectors.toUnmodifiableList() -> garantiert unmodifiable
        try {
            direkteListe.add("Frank"); // wirft UnsupportedOperationException
        } catch (UnsupportedOperationException e) {
            System.out.println("Stream.toList() ist unveränderlich!");
        }

        // toSet() - in Set (Reihenfolge nicht garantiert)
        Set<String> menge = namen.stream()
            .collect(Collectors.toSet());
        System.out.println("Set: " + menge);

        // toCollection() - in spezifische Collection
        TreeSet<String> sortiertesMenge = namen.stream()
            .collect(Collectors.toCollection(TreeSet::new));
        System.out.println("TreeSet: " + sortiertesMenge);

        LinkedList<String> verlinkteListe = namen.stream()
            .collect(Collectors.toCollection(LinkedList::new));

        // joining - Strings verbinden
        String kommagetrennt = namen.stream()
            .distinct()
            .collect(Collectors.joining(", "));
        System.out.println("Kommagetrennt: " + kommagetrennt);

        String mitPräfixSuffix = namen.stream()
            .distinct()
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("Mit Klammern: " + mitPräfixSuffix);

        // counting
        long anzahl = namen.stream()
            .collect(Collectors.counting());
        System.out.println("Anzahl: " + anzahl);

        // summingInt / summingDouble / summingLong
        int gesamtLaenge = namen.stream()
            .collect(Collectors.summingInt(String::length));
        System.out.println("Gesamtlänge: " + gesamtLaenge);

        // averagingInt / averagingDouble
        double durchschnittLaenge = namen.stream()
            .collect(Collectors.averagingInt(String::length));
        System.out.printf("Durchschnittslänge: %.2f%n", durchschnittLaenge);

        // summarizingInt
        IntSummaryStatistics laengenStats = namen.stream()
            .collect(Collectors.summarizingInt(String::length));
        System.out.println("Stats: " + laengenStats);

        // toMap
        Map<String, Integer> namenZuLaenge = namen.stream()
            .distinct()
            .collect(Collectors.toMap(
                n -> n,           // Key-Extractor
                String::length    // Value-Extractor
            ));
        System.out.println("Map: " + namenZuLaenge);

        // toMap mit Merge-Funktion (für Duplikate)
        Map<Integer, String> laengeZuNamen = namen.stream()
            .collect(Collectors.toMap(
                String::length,
                n -> n,
                (vorhandener, neuer) -> vorhandener + ", " + neuer // Duplikate zusammenführen
            ));
        System.out.println("Länge -> Namen: " + laengeZuNamen);
    }
}
```

### 5.2 groupingBy und partitioningBy

```java
import java.util.*;
import java.util.stream.*;

public class GroupingPartitioning {
    record Mitarbeiter(String name, String abteilung, int alter, double gehalt) {}

    public static void main(String[] args) {
        List<Mitarbeiter> mitarbeiter = List.of(
            new Mitarbeiter("Alice", "IT", 30, 85_000),
            new Mitarbeiter("Bob", "HR", 45, 55_000),
            new Mitarbeiter("Charlie", "IT", 28, 92_000),
            new Mitarbeiter("Diana", "Finanzen", 38, 78_000),
            new Mitarbeiter("Eve", "IT", 35, 71_000),
            new Mitarbeiter("Frank", "HR", 52, 48_000),
            new Mitarbeiter("Grace", "Finanzen", 29, 82_000)
        );

        // groupingBy: einfach
        Map<String, List<Mitarbeiter>> nachAbteilung = mitarbeiter.stream()
            .collect(Collectors.groupingBy(Mitarbeiter::abteilung));
        nachAbteilung.forEach((abt, ma) ->
            System.out.println(abt + ": " + ma.stream().map(Mitarbeiter::name).toList())
        );

        // groupingBy mit Downstream-Collector: counting
        Map<String, Long> anzahlProAbteilung = mitarbeiter.stream()
            .collect(Collectors.groupingBy(
                Mitarbeiter::abteilung,
                Collectors.counting()
            ));
        System.out.println("Anzahl pro Abteilung: " + anzahlProAbteilung);

        // groupingBy mit averagingDouble
        Map<String, Double> durchschnittsgehalt = mitarbeiter.stream()
            .collect(Collectors.groupingBy(
                Mitarbeiter::abteilung,
                Collectors.averagingDouble(Mitarbeiter::gehalt)
            ));
        durchschnittsgehalt.forEach((abt, avg) ->
            System.out.printf("%s: %.0f EUR%n", abt, avg)
        );

        // groupingBy mit Mapping
        Map<String, List<String>> namenProAbteilung = mitarbeiter.stream()
            .collect(Collectors.groupingBy(
                Mitarbeiter::abteilung,
                Collectors.mapping(Mitarbeiter::name, Collectors.toList())
            ));
        System.out.println("Namen: " + namenProAbteilung);

        // Zweistufiges groupingBy
        Map<String, Map<Boolean, List<Mitarbeiter>>> abteilungNachAlter = mitarbeiter.stream()
            .collect(Collectors.groupingBy(
                Mitarbeiter::abteilung,
                Collectors.groupingBy(m -> m.alter() >= 35)
            ));
        System.out.println("IT >= 35: " +
            abteilungNachAlter.get("IT").get(true).stream()
                .map(Mitarbeiter::name).toList());

        // partitioningBy: teilt in 2 Gruppen (true/false)
        Map<Boolean, List<Mitarbeiter>> partitioniert = mitarbeiter.stream()
            .collect(Collectors.partitioningBy(m -> m.gehalt() > 70_000));
        System.out.println("Gut bezahlt (>70k): " +
            partitioniert.get(true).stream().map(Mitarbeiter::name).toList());
        System.out.println("Weniger (<=70k): " +
            partitioniert.get(false).stream().map(Mitarbeiter::name).toList());

        // partitioningBy mit Downstream
        Map<Boolean, Long> anzahlPartitioniert = mitarbeiter.stream()
            .collect(Collectors.partitioningBy(
                m -> m.gehalt() > 70_000,
                Collectors.counting()
            ));
        System.out.println("Anzahl partitioniert: " + anzahlPartitioniert);
    }
}
```

### 5.3 Collectors.teeing (Java 12+)

```java
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class TeeingCollector {
    record Ergebnis(long anzahl, double summe) {}

    public static void main(String[] args) {
        List<Integer> zahlen = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // teeing: zwei Collectors parallel anwenden, Ergebnisse kombinieren
        var statistiken = zahlen.stream()
            .collect(Collectors.teeing(
                Collectors.counting(),           // Collector 1: Anzahl
                Collectors.summingInt(n -> n),   // Collector 2: Summe
                (count, sum) -> new Ergebnis(count, sum) // Kombination
            ));
        System.out.printf("Anzahl: %d, Summe: %.0f%n",
            statistiken.anzahl(), statistiken.summe());

        // Praktisches Beispiel: Min und Max gleichzeitig
        record MinMax(Optional<Integer> min, Optional<Integer> max) {}
        MinMax minMax = zahlen.stream()
            .collect(Collectors.teeing(
                Collectors.minBy(Comparator.naturalOrder()),
                Collectors.maxBy(Comparator.naturalOrder()),
                MinMax::new
            ));
        System.out.println("Min: " + minMax.min() + ", Max: " + minMax.max());

        // Durchschnitt aus Summe und Anzahl
        record SummeAnzahl(long summe, long anzahl) {
            double durchschnitt() { return (double) summe / anzahl; }
        }

        var sumAnz = zahlen.stream()
            .collect(Collectors.teeing(
                Collectors.summingLong(Integer::longValue),
                Collectors.counting(),
                SummeAnzahl::new
            ));
        System.out.printf("Durchschnitt: %.1f%n", sumAnz.durchschnitt());
    }
}
```

---

## 6. Optional

### 6.1 Optional – Grundlagen

`Optional<T>` ist ein Container, der entweder einen Wert enthält oder leer ist. Es vermeidet NullPointerException und macht die Möglichkeit eines fehlenden Wertes explizit.

```java
import java.util.*;

public class OptionalGrundlagen {
    public static void main(String[] args) {
        // Optional erstellen
        Optional<String> mitWert = Optional.of("Hallo");           // Wert, nie null
        Optional<String> leer = Optional.empty();                   // kein Wert
        Optional<String> nullsicher = Optional.ofNullable(null);   // kann null sein
        Optional<String> mitText = Optional.ofNullable("Text");

        // Wert prüfen
        System.out.println("Vorhanden: " + mitWert.isPresent());    // true
        System.out.println("Leer: "      + leer.isEmpty());          // true (Java 11+)

        // Wert abrufen - verschiedene Varianten
        String wert = mitWert.get();     // Gibt Wert zurück, oder NoSuchElementException
        System.out.println("get(): " + wert);

        String oder = leer.orElse("Standard");     // Standardwert wenn leer
        System.out.println("orElse: " + oder);

        String oderGet = leer.orElseGet(() -> "Berechneter Standard"); // Lazy
        System.out.println("orElseGet: " + oderGet);

        // orElseThrow
        try {
            leer.orElseThrow(() -> new IllegalStateException("Kein Wert!"));
        } catch (IllegalStateException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // ifPresent - Seiteneffekt wenn Wert vorhanden
        mitWert.ifPresent(v -> System.out.println("Wert ist: " + v));
        leer.ifPresent(v -> System.out.println("Wird nicht gedruckt"));

        // ifPresentOrElse (Java 9+)
        mitWert.ifPresentOrElse(
            v -> System.out.println("Vorhanden: " + v),
            () -> System.out.println("Leer!")
        );
        leer.ifPresentOrElse(
            v -> System.out.println("Vorhanden: " + v),
            () -> System.out.println("War leer!")
        );
    }
}
```

### 6.2 Optional – Funktionale Operationen

```java
import java.util.*;
import java.util.stream.*;

public class OptionalFunktional {
    record Benutzer(String name, Optional<String> email) {}

    public static Optional<Benutzer> findeBenutzer(String name) {
        Map<String, Benutzer> db = Map.of(
            "Alice", new Benutzer("Alice", Optional.of("alice@example.com")),
            "Bob", new Benutzer("Bob", Optional.empty())
        );
        return Optional.ofNullable(db.get(name));
    }

    public static void main(String[] args) {
        // map: Wert transformieren (wenn vorhanden)
        Optional<String> name = Optional.of("Alice");
        Optional<Integer> laenge = name.map(String::length);
        System.out.println("Länge: " + laenge.orElse(0)); // 5

        Optional<String> leer = Optional.empty();
        Optional<Integer> leereMap = leer.map(String::length);
        System.out.println("Leer nach map: " + leereMap.isPresent()); // false

        // filter: Optional basierend auf Bedingung leeren
        Optional<String> gefiltert = name.filter(n -> n.length() > 3);
        System.out.println("Gefiltert: " + gefiltert.isPresent()); // true

        Optional<String> ausgefilterT = name.filter(n -> n.length() > 10);
        System.out.println("Ausgefiltert: " + ausgefilterT.isPresent()); // false

        // flatMap: Optional von Optional flachen
        Optional<Benutzer> alice = findeBenutzer("Alice");
        Optional<String> aliceEmail = alice.flatMap(Benutzer::email);
        System.out.println("Alice Email: " + aliceEmail.orElse("Keine Email"));

        Optional<Benutzer> bob = findeBenutzer("Bob");
        Optional<String> bobEmail = bob.flatMap(Benutzer::email);
        System.out.println("Bob Email: " + bobEmail.orElse("Keine Email"));

        Optional<Benutzer> unbekannt = findeBenutzer("Charlie");
        Optional<String> unbekanntEmail = unbekannt.flatMap(Benutzer::email);
        System.out.println("Unbekannt Email: " + unbekanntEmail.orElse("Keine Email"));

        // or (Java 9+): alternativer Optional wenn leer
        Optional<String> fallback = leer.or(() -> Optional.of("Fallback-Wert"));
        System.out.println("or: " + fallback.get());

        // stream() (Java 9+): Optional als Stream mit 0 oder 1 Element
        List<Optional<String>> optionals = Arrays.asList(
            Optional.of("A"),
            Optional.empty(),
            Optional.of("B"),
            Optional.empty(),
            Optional.of("C")
        );

        List<String> werte = optionals.stream()
            .flatMap(Optional::stream) // Java 9+
            .collect(Collectors.toList());
        System.out.println("Nur vorhandene: " + werte); // [A, B, C]

        // Optional vermeidet NullPointerException (Vergleich):
        // Ohne Optional (riskant):
        String unsicher = null;
        // int len = unsicher.length(); // NullPointerException!

        // Mit Optional (sicher):
        Optional<String> sicher = Optional.ofNullable(unsicher);
        int len = sicher.map(String::length).orElse(0);
        System.out.println("Sichere Länge: " + len); // 0
    }
}
```

---

## 7. Parallel Streams

### 7.1 Parallel Streams – Grundlagen

```java
import java.util.*;
import java.util.stream.*;

public class ParallelStreamGrundlagen {
    public static void main(String[] args) {
        List<Integer> grosseListe = new ArrayList<>();
        for (int i = 1; i <= 1_000_000; i++) {
            grosseListe.add(i);
        }

        // Sequentieller Stream
        long startSeq = System.currentTimeMillis();
        long sumSeq = grosseListe.stream()
            .mapToLong(Integer::longValue)
            .sum();
        long zeitSeq = System.currentTimeMillis() - startSeq;

        // Paralleler Stream
        long startPar = System.currentTimeMillis();
        long sumPar = grosseListe.parallelStream()
            .mapToLong(Integer::longValue)
            .sum();
        long zeitPar = System.currentTimeMillis() - startPar;

        System.out.println("Sequentiell: " + sumSeq + " in " + zeitSeq + "ms");
        System.out.println("Parallel:    " + sumPar + " in " + zeitPar + "ms");

        // Parallel mit Stream.parallel()
        long sumParallel = grosseListe.stream()
            .parallel()              // wechsle zu parallelem Modus
            .mapToLong(Integer::longValue)
            .sum();
        System.out.println("parallel(): " + sumParallel);

        // isParallel() prüfen
        Stream<Integer> stream = grosseListe.stream();
        System.out.println("Ist parallel: " + stream.isParallel()); // false
        Stream<Integer> parallelStream = stream.parallel();
        System.out.println("Ist parallel: " + parallelStream.isParallel()); // true
        Stream<Integer> wiederSequentiell = parallelStream.sequential();
        System.out.println("Ist parallel: " + wiederSequentiell.isParallel()); // false
    }
}
```

### 7.2 Parallel Streams – Wann sinnvoll?

```java
import java.util.*;
import java.util.stream.*;

public class ParallelStreamWannSinnvoll {
    // Rechenintensive Operation
    static double teureBerechnung(double x) {
        try {
            Thread.sleep(1); // simuliert Berechnung
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Math.sqrt(x * x + Math.sin(x));
    }

    public static void main(String[] args) {
        List<Double> eingaben = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            eingaben.add((double) i);
        }

        // Sequentiell
        long start = System.currentTimeMillis();
        List<Double> ergebnisSeq = eingaben.stream()
            .map(ParallelStreamWannSinnvoll::teureBerechnung)
            .collect(Collectors.toList());
        System.out.println("Sequentiell: " + (System.currentTimeMillis() - start) + "ms");

        // Parallel
        start = System.currentTimeMillis();
        List<Double> ergebnisPar = eingaben.parallelStream()
            .map(ParallelStreamWannSinnvoll::teureBerechnung)
            .collect(Collectors.toList());
        System.out.println("Parallel: " + (System.currentTimeMillis() - start) + "ms");

        // VORSICHT: Parallel Streams haben Fallstricke!

        // Problem 1: Reihenfolge bei forEachOrdered
        System.out.print("Mit forEachOrdered (geordnet): ");
        IntStream.range(1, 6).parallel().forEachOrdered(System.out::print);
        System.out.println(); // 12345 immer

        System.out.print("Mit forEach (ungeordnet): ");
        IntStream.range(1, 6).parallel().forEach(System.out::print);
        System.out.println(); // Reihenfolge variiert!

        // Problem 2: Nicht thread-sichere Operationen vermeiden!
        // FALSCH (race condition):
        List<Integer> unsicher = new ArrayList<>(); // nicht thread-sicher
        // IntStream.range(0, 100).parallel().forEach(unsicher::add); // Fehler!

        // RICHTIG:
        List<Integer> sicher = IntStream.range(0, 100)
            .parallel()
            .boxed()
            .collect(Collectors.toList()); // thread-sicher
        System.out.println("Thread-sicher: " + sicher.size()); // 100

        // Parallel Streams lohnen sich bei:
        // - Großen Datemengen (> 10.000 Elemente)
        // - Rechenintensiven Operationen (CPU-bound)
        // - Unabhängigen Operationen (kein gemeinsamer Zustand)
        // - Mehrkernigen Prozessoren

        // Parallel Streams lohnen sich NICHT bei:
        // - I/O-gebundenen Operationen
        // - Kleinen Datemengen (Overhead überwiegt)
        // - Operationen mit gemeinsamem Zustand
        // - Wenn Reihenfolge wichtig ist
    }
}
```

### 7.3 Praktisches Beispiel: Datenauswertung

```java
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class DatenauswertungBeispiel {
    record Transaktion(
        String id,
        String kundeId,
        String kategorie,
        double betrag,
        java.time.LocalDate datum
    ) {}

    public static void main(String[] args) {
        // Testdaten generieren
        List<String> kategorien = List.of("Lebensmittel", "Elektronik", "Kleidung", "Sport");
        List<String> kunden = List.of("K001", "K002", "K003", "K004", "K005");
        Random rnd = new Random(42);

        List<Transaktion> transaktionen = IntStream.range(0, 1000)
            .mapToObj(i -> new Transaktion(
                "T" + i,
                kunden.get(rnd.nextInt(kunden.size())),
                kategorien.get(rnd.nextInt(kategorien.size())),
                10 + rnd.nextDouble() * 490,
                java.time.LocalDate.now().minusDays(rnd.nextInt(365))
            ))
            .collect(Collectors.toList());

        // 1. Umsatz pro Kategorie
        System.out.println("Umsatz pro Kategorie:");
        transaktionen.stream()
            .collect(Collectors.groupingBy(
                Transaktion::kategorie,
                Collectors.summingDouble(Transaktion::betrag)
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> System.out.printf("  %-15s %.2f EUR%n", e.getKey(), e.getValue()));

        // 2. Top 3 Kunden nach Umsatz
        System.out.println("\nTop 3 Kunden:");
        transaktionen.stream()
            .collect(Collectors.groupingBy(
                Transaktion::kundeId,
                Collectors.summingDouble(Transaktion::betrag)
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(3)
            .forEach(e -> System.out.printf("  %s: %.2f EUR%n", e.getKey(), e.getValue()));

        // 3. Transaktionen über 400 EUR pro Kategorie
        System.out.println("\nHohe Transaktionen (> 400 EUR) pro Kategorie:");
        transaktionen.stream()
            .filter(t -> t.betrag() > 400)
            .collect(Collectors.groupingBy(
                Transaktion::kategorie,
                Collectors.counting()
            ))
            .forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // 4. Parallel: Gesamtstatistik
        DoubleSummaryStatistics stats = transaktionen.parallelStream()
            .mapToDouble(Transaktion::betrag)
            .summaryStatistics();
        System.out.printf("%nGesamtstatistik:%n  Anzahl: %d%n  Min: %.2f%n  Max: %.2f%n  Avg: %.2f%n  Sum: %.2f%n",
            stats.getCount(), stats.getMin(), stats.getMax(),
            stats.getAverage(), stats.getSum());
    }
}
```

---

## Zusammenfassung

### Stream-Pipeline auf einen Blick

```
Stream<T> erzeugen:
  collection.stream()           parallelStream()
  Stream.of(a, b, c)            Stream.generate(supplier)
  Stream.iterate(seed, fn)      IntStream.range(start, end)
  Arrays.stream(array)          Stream.empty()

Intermediate Operations (lazy, geben Stream zurück):
  filter(predicate)             map(function)
  flatMap(function)             mapToInt/Long/Double
  sorted()                      sorted(comparator)
  distinct()                    limit(n)
  skip(n)                       peek(consumer)
  takeWhile(predicate)          dropWhile(predicate)

Terminal Operations (eager, verbraucht Stream):
  collect(collector)            forEach(consumer)
  reduce(identity, operator)    count()
  findFirst()                   findAny()
  anyMatch(predicate)           allMatch(predicate)
  noneMatch(predicate)          min/max(comparator)
  toArray()                     toList() (Java 16+)

Collectors:
  toList()                      toSet()
  toMap(kf, vf)                 groupingBy(classifier)
  partitioningBy(predicate)     joining(delimiter)
  counting()                    summingInt/Double
  averagingInt/Double           teeing(c1, c2, merger)
```

### Optional-API

| Methode | Beschreibung |
|---|---|
| `of(value)` | Erstellt Optional, wirft NPE bei null |
| `ofNullable(value)` | Erstellt Optional, leer bei null |
| `empty()` | Erstellt leeres Optional |
| `get()` | Gibt Wert zurück oder NoSuchElementException |
| `orElse(default)` | Gibt Wert oder Standard zurück |
| `orElseGet(supplier)` | Gibt Wert oder berechneten Standard zurück |
| `orElseThrow(exSupplier)` | Gibt Wert oder wirft Exception |
| `ifPresent(consumer)` | Führt Aktion aus wenn Wert vorhanden |
| `map(function)` | Transformiert Wert wenn vorhanden |
| `flatMap(function)` | Gibt Optional zurück wenn vorhanden |
| `filter(predicate)` | Leert Optional wenn Bedingung nicht erfüllt |
| `stream()` | Optional als Stream (Java 9+) |

---

## Multiple-Choice-Fragen

**Frage 1:** Welche Methode gibt es bei `LongStream`, die auch bei `IntStream` existiert, bei `DoubleStream` aber nicht?

- A) `summaryStatistics()`
- **B) `range(long startInclusive, long endExclusive)` ✓**
- C) `average()`
- D) `sum()`

> `LongStream.range()` und `LongStream.rangeClosed()` existieren analog zu `IntStream`. `DoubleStream` hat kein `range()`, da Gleitkommazahlen keinen sinnvollen diskreten Bereich erzeugen. Alle drei primitiven Streams bieten `summaryStatistics()`, `average()` und `sum()`.

---

**Frage 2:** Was gibt `LongStream.rangeClosed(1L, 5L).summaryStatistics().getSum()` zurück?

- A) 10
- **B) 15 ✓**
- C) 5
- D) 14

> `rangeClosed(1, 5)` erzeugt die Werte 1, 2, 3, 4, 5 (inklusiv beide Grenzen). Die Summe ist 1+2+3+4+5 = 15.

---

**Frage 3:** Welche Aussage über `DoubleStream.summaryStatistics()` ist korrekt?

- A) Es gibt nur `getMin()` und `getMax()` im `DoubleSummaryStatistics`-Objekt
- B) `summaryStatistics()` ist eine Intermediate Operation
- **C) `DoubleSummaryStatistics` liefert Minimum, Maximum, Summe, Durchschnitt und Anzahl in einem Aufruf ✓**
- D) `DoubleStream` hat keine `summaryStatistics()`-Methode

> `DoubleSummaryStatistics` (wie auch `IntSummaryStatistics` und `LongSummaryStatistics`) fasst fünf statistische Kennzahlen zusammen: `getMin()`, `getMax()`, `getSum()`, `getAverage()` und `getCount()`. `summaryStatistics()` ist eine Terminaloperation.

---

**Frage 4:** Was ist der Hauptunterschied zwischen `stream.toList()` (Java 16) und `stream.collect(Collectors.toList())`?

- A) `toList()` ist langsamer als `collect(Collectors.toList())`
- **B) `toList()` gibt immer eine unveränderliche Liste zurück; `Collectors.toList()` gibt eine mutierbare Liste zurück ✓**
- C) `toList()` erlaubt `null`-Elemente; `Collectors.toList()` nicht
- D) Es gibt keinen Unterschied, es ist nur syntaktischer Zucker

> `Stream.toList()` (Java 16) gibt eine unveränderliche Liste zurück und wirft `UnsupportedOperationException` bei Änderungsversuchen. `Collectors.toList()` liefert implementierungsabhängig (typischerweise eine `ArrayList`) eine mutierbare Liste. Außerdem erlaubt `toList()` keine `null`-Elemente.

---

**Frage 5:** Welche der folgenden Code-Varianten kompiliert und erzeugt eine unveränderliche Liste in Java 25?

- A) `list.stream().filter(s -> s.length() > 3).collect(Collectors.toImmutableList())`
- B) `list.stream().filter(s -> s.length() > 3).toUnmodifiableList()`
- **C) `list.stream().filter(s -> s.length() > 3).toList()` ✓**
- D) `list.stream().filter(s -> s.length() > 3).collect(Collectors.toFixedList())`

> Seit Java 16 ist `Stream.toList()` eine direkte Terminaloperation, die eine unveränderliche Liste zurückgibt. Die anderen Optionen existieren nicht in dieser Form. `Collectors.toUnmodifiableList()` existiert, muss aber über `collect()` aufgerufen werden.

---

**Frage 6:** Was gibt folgender Code aus?
```java
List<String> result = Stream.of("A", "B", "C").toList();
result.add("D");
```

- A) `[A, B, C, D]`
- B) `[A, B, C]` (die `add`-Operation wird still ignoriert)
- **C) `UnsupportedOperationException` wird geworfen ✓**
- D) `NullPointerException` wird geworfen

> `Stream.toList()` liefert eine unveränderliche Liste. Jeder Versuch, sie zu verändern (`add`, `remove`, `set`), wirft eine `UnsupportedOperationException`.

---

## Skill Check: Streams

Mindestens **80%** der folgenden Aufgaben müssen korrekt gelöst werden.

- [ ] Erkläre den Unterschied zwischen `LongStream.range()` und `LongStream.rangeClosed()` und nenne je ein Anwendungsbeispiel
- [ ] Schreibe Code, der `LongStream.rangeClosed(1L, 100L).summaryStatistics()` verwendet und alle fünf Kennzahlen ausgibt
- [ ] Erkläre, warum `DoubleStream` keine `range()`-Methode hat, aber `summaryStatistics()` anbietet
- [ ] Demonstriere `DoubleStream.of(...).summaryStatistics()` mit `getMin()`, `getMax()`, `getSum()`, `getAverage()` und `getCount()`
- [ ] Erkläre den Unterschied zwischen `Stream.toList()`, `Collectors.toList()` und `Collectors.toUnmodifiableList()`
- [ ] Schreibe eine Stream-Pipeline, die `Stream.toList()` als Terminaloperation verwendet und zeige, dass das Ergebnis unveränderlich ist
- [ ] Nenne die Java-Version, ab der `Stream.toList()` verfügbar ist, und begründe warum es gegenüber `collect(Collectors.toList())` bevorzugt werden kann
