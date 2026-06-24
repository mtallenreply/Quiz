# Modul 10: Nested Classes and Lambda

## Übersicht

Dieses Modul behandelt verschachtelte Klassen und Lambda-Ausdrücke – zwei wichtige Konzepte für ausdrucksstarken, modernen Java-Code. Sie lernen die vier Arten verschachtelter Klassen kennen, verstehen funktionale Interfaces und können Lambda-Ausdrücke sowie Methoden-Referenzen in verschiedenen Kontexten einsetzen.

| Thema | Dauer |
|---|---|
| Nested Classes | 20 min |
| Anonymous Classes | 9 min |
| Functional Interfaces | 9 min |
| Lambda Expressions | 4 min |
| Lambda Syntax | 21 min |
| Method References | 41 min |
| Practice 10-1 | 19 min |
| **Skill Check: Lambda** | **mind. 80%** |

---

## 1. Verschachtelte Klassen (Nested Classes)

### 1.1 Überblick: Vier Arten verschachtelter Klassen

Java kennt vier verschiedene Arten von Klassen, die innerhalb einer anderen Klasse definiert werden können:

| Art | Schlüsselwort | Instanz der äußeren Klasse? | Kontext |
|---|---|---|---|
| Static Nested Class | `static` | Nein | Klassenebene |
| Inner Class | (kein static) | Ja | Instanzebene |
| Local Class | (in Methode) | Je nach Art | Methodenrumpf |
| Anonymous Class | `new Interface() {}` | Je nach Kontext | Ausdruck |

### 1.2 Static Nested Class

Eine statisch verschachtelte Klasse ist mit dem Schlüsselwort `static` deklariert. Sie hat keinen Zugriff auf Instanzmitglieder der äußeren Klasse, nur auf statische Mitglieder.

```java
public class Fahrzeug {
    private String marke;
    private static int gesamtAnzahl = 0;

    // Static Nested Class
    public static class Motor {
        private int leistung;
        private String typ;

        public Motor(int leistung, String typ) {
            this.leistung = leistung;
            this.typ = typ;
            // Zugriff auf statische Mitglieder der äußeren Klasse ist erlaubt
            gesamtAnzahl++; // aber kein Zugriff auf 'marke' (Instanzmitglied)
        }

        public String getBeschreibung() {
            return typ + " mit " + leistung + " PS";
        }

        // Statische Methode in nested class
        public static Motor standardMotor() {
            return new Motor(150, "Benzin");
        }
    }

    public Fahrzeug(String marke) {
        this.marke = marke;
    }

    public static void main(String[] args) {
        // Zugriff: OuterClass.NestedClass
        Fahrzeug.Motor motor = new Fahrzeug.Motor(200, "Diesel");
        System.out.println("Motor: " + motor.getBeschreibung());

        // Statische Factory-Methode
        Motor standard = Motor.standardMotor();
        System.out.println("Standard: " + standard.getBeschreibung());

        // Wenn im selben Package:
        Fahrzeug auto = new Fahrzeug("BMW");
        // Zugriff auf gesamtAnzahl über Klassenname
        System.out.println("Motoren erstellt: " + Fahrzeug.gesamtAnzahl);
    }
}
```

### 1.3 Inner Class (Nicht-statische verschachtelte Klasse)

Eine Inner Class hat Zugriff auf alle Mitglieder der äußeren Klasse, einschließlich privater. Sie benötigt eine Instanz der äußeren Klasse.

```java
public class BankKonto {
    private String kontonummer;
    private double saldo;
    private java.util.List<String> transaktionen = new java.util.ArrayList<>();

    public BankKonto(String kontonummer, double anfangssaldo) {
        this.kontonummer = kontonummer;
        this.saldo = anfangssaldo;
    }

    // Inner Class: hat Zugriff auf alle privaten Felder von BankKonto
    public class Transaktion {
        private String typ;
        private double betrag;
        private java.time.LocalDateTime zeitpunkt;

        public Transaktion(String typ, double betrag) {
            this.typ = typ;
            this.betrag = betrag;
            this.zeitpunkt = java.time.LocalDateTime.now();

            // Direkter Zugriff auf äußere Instanz
            if (typ.equals("Einzahlung")) {
                saldo += betrag; // privates Feld der äußeren Klasse!
            } else if (typ.equals("Auszahlung")) {
                if (betrag > saldo) {
                    throw new IllegalStateException("Nicht genug Guthaben!");
                }
                saldo -= betrag;
            }
            transaktionen.add(zeitpunkt + ": " + typ + " " + betrag + " EUR");
        }

        public void drucken() {
            // Zugriff auf äußere Klasse mit OuterClass.this
            System.out.println("Konto: " + BankKonto.this.kontonummer);
            System.out.println("Typ: " + typ + ", Betrag: " + betrag);
            System.out.println("Saldo nach: " + saldo);
        }
    }

    public Transaktion einzahlen(double betrag) {
        return new Transaktion("Einzahlung", betrag);
    }

    public Transaktion auszahlen(double betrag) {
        return new Transaktion("Auszahlung", betrag);
    }

    public static void main(String[] args) {
        BankKonto konto = new BankKonto("DE123456789", 1000.00);

        // Inner Class Instanz braucht äußere Instanz
        BankKonto.Transaktion t1 = konto.einzahlen(500.00);
        t1.drucken();

        BankKonto.Transaktion t2 = konto.auszahlen(200.00);
        t2.drucken();

        // Alternativ: direkter Aufruf
        konto.new Transaktion("Einzahlung", 100.00).drucken();
    }
}
```

### 1.4 Local Class (Lokale Klasse) [Fortgeschritten]

Eine lokale Klasse wird innerhalb eines Methodenrumpfes definiert. Sie kann auf effektiv finale lokale Variablen der Methode zugreifen. Lokale Klassen können Interfaces implementieren oder andere Klassen erweitern – auch abstrakte und konkrete. Wichtig für die Prüfung: In einer **statischen** Methode hat die lokale Klasse keinen Zugriff auf Instanzfelder der äußeren Klasse; in einer **Instanzmethode** dagegen schon.

```java
import java.util.*;

public class LocalClassDemo {
    private String instanzFeld = "Ich bin ein Instanzfeld";

    // --- Lokale Klasse in einer INSTANZ-Methode ---
    public List<String> filtereNamen(List<String> namen, int minLaenge) {
        // Lokale Klasse - nur innerhalb dieser Methode sichtbar
        class NameFilter {
            private final int minLen;

            // Zugriff auf effektiv finalen Parameter minLaenge
            NameFilter() {
                this.minLen = minLaenge; // effektiv final!
            }

            boolean gueltig(String name) {
                return name != null
                    && name.length() >= minLen
                    && !name.isBlank();
            }

            String formatieren(String name) {
                return name.trim().substring(0, 1).toUpperCase()
                    + name.trim().substring(1).toLowerCase();
            }

            // Lokale Klasse in Instanzmethode: darf auf Instanzfeld zugreifen
            void debug() {
                System.out.println("Kontext: " + instanzFeld); // OK!
            }
        }

        NameFilter filter = new NameFilter();
        filter.debug();
        List<String> ergebnis = new ArrayList<>();
        for (String name : namen) {
            if (filter.gueltig(name)) {
                ergebnis.add(filter.formatieren(name));
            }
        }
        return ergebnis;
    }

    // --- Lokale Klasse in einer STATISCHEN Methode ---
    public static void statischeMethode() {
        String lokaleFinal = "Nur lokal";

        // Lokale Klasse kann Interface implementieren:
        interface Meldbar {
            void melden();
        }

        class Meldung implements Meldbar {
            private final String text;
            Meldung(String text) { this.text = text; }

            @Override
            public void melden() {
                System.out.println("Meldung: " + text + " / " + lokaleFinal);
                // instanzFeld  <- COMPILERFEHLER: statische Methode, kein this
            }
        }

        // Lokale Klasse kann auch konkrete Klasse erweitern:
        class BunteMeldung extends Meldung {
            BunteMeldung(String text) { super("[BUNT] " + text); }
        }

        Meldbar m = new Meldung("Test");
        m.melden();
        new BunteMeldung("Farbe").melden();
    }

    public static void main(String[] args) {
        LocalClassDemo demo = new LocalClassDemo();
        List<String> namen = Arrays.asList("alice", "Bo", "CHARLIE", "  ", "d", "Emma");
        System.out.println(demo.filtereNamen(namen, 3)); // [Alice, Charlie, Emma]
        statischeMethode();
    }
}
```

**Merksätze für die Prüfung:**
- Lokale Klassen in **statischen** Methoden: kein Zugriff auf `this` / Instanzfelder der äußeren Klasse.
- Lokale Klassen in **Instanzmethoden**: vollständiger Zugriff auf Instanzfelder der äußeren Klasse.
- Lokale Klassen dürfen Interfaces implementieren und Klassen (auch konkrete) erweitern.
- Zugriff auf lokale Variablen nur wenn **effektiv final** (nie neu zugewiesen).

### 1.5 Vergleich: Static Nested vs. Inner Class

| Merkmal | Static Nested Class | Inner Class |
|---|---|---|
| `static` Modifier | Ja | Nein |
| Eigene Instanz benötigt? | Nein | Ja (äußere Instanz) |
| Zugriff auf äußere Instanzmitglieder | Nein | Ja (alle, auch private) |
| Zugriff auf äußere statische Mitglieder | Ja | Ja |
| Kann statische Mitglieder deklarieren? | Ja | Nein (nur Konstanten) |
| Verwendungszweck | Hilfsfklasse, Builder | Enge Kopplung, Callbacks |
| Typisches Beispiel | `Map.Entry`, Builder-Pattern | Iterator, Event-Listener |

---

## 2. Anonyme Klassen

### 2.1 Konzept und Syntax

Eine anonyme Klasse wird gleichzeitig definiert und instanziiert. Sie hat keinen Namen und kann ein Interface oder eine abstrakte Klasse implementieren/erweitern.

```java
import java.util.*;

public class AnonymousClassDemo {
    interface Begruessung {
        void begruessen(String name);
        default String formatieren(String name) {
            return "Hallo, " + name + "!";
        }
    }

    public static void main(String[] args) {
        // Anonyme Klasse, die Begruessung implementiert
        Begruessung deutsch = new Begruessung() {
            @Override
            public void begruessen(String name) {
                System.out.println("Guten Tag, " + name + "!");
            }
        };

        Begruessung englisch = new Begruessung() {
            private String prefix = "Mr./Ms."; // Zusätzliches Feld möglich

            @Override
            public void begruessen(String name) {
                System.out.println("Good day, " + prefix + " " + name + "!");
            }
        };

        deutsch.begruessen("Müller");
        englisch.begruessen("Smith");

        // Anonyme Klasse mit abstrakter Basisklasse
        abstract class Tier {
            String name;
            Tier(String name) { this.name = name; }
            abstract String lautmachen();
        }

        Tier hund = new Tier("Bello") {
            @Override
            String lautmachen() { return "Wuff!"; }
        };
        System.out.println(hund.name + " sagt: " + hund.lautmachen());

        // Klassischer Anwendungsfall: Comparator (vor Lambda)
        List<String> namen = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob"));
        Collections.sort(namen, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.compareToIgnoreCase(b);
            }
        });
        System.out.println("Sortiert: " + namen);
    }
}
```

### 2.2 Anonyme Klasse – konkrete Klasse erweitern [Fortgeschritten]

Anonyme Klassen können nicht nur Interfaces implementieren oder abstrakte Klassen erweitern, sondern auch **konkrete (nicht-abstrakte) Klassen** erweitern. Dabei können beliebige Methoden überschrieben werden. Der Konstruktor der konkreten Superklasse wird mit den entsprechenden Argumenten aufgerufen.

```java
public class AnonymousExtendsConcreteDemo {

    // Konkrete (nicht-abstrakte) Klasse
    static class Rechteck {
        protected int breite;
        protected int hoehe;

        Rechteck(int breite, int hoehe) {
            this.breite = breite;
            this.hoehe = hoehe;
        }

        int flaeche() {
            return breite * hoehe;
        }

        String beschreibung() {
            return "Rechteck " + breite + "x" + hoehe;
        }
    }

    public static void main(String[] args) {
        // Anonyme Klasse erweitert KONKRETE Klasse Rechteck
        Rechteck quadrat = new Rechteck(5, 5) {
            // Methode überschreiben
            @Override
            String beschreibung() {
                return "Quadrat " + breite + "x" + hoehe + " (Fläche: " + flaeche() + ")";
            }

            // Neue Methode in anonymer Klasse (nur via Object-Ref nicht aufrufbar)
            int umfang() {
                return 4 * breite;
            }
        };

        System.out.println(quadrat.beschreibung()); // Quadrat 5x5 (Fläche: 25)
        System.out.println("Fläche: " + quadrat.flaeche()); // 25

        // Weiteres Beispiel: java.util.TimerTask ist eine konkrete Klasse
        java.util.TimerTask einmaligeAufgabe = new java.util.TimerTask() {
            @Override
            public void run() {
                System.out.println("TimerTask ausgeführt (anonyme Subklasse von TimerTask)");
            }
        };
        einmaligeAufgabe.run(); // direkt aufrufen für Demo
    }
}
```

### 2.3 Anonyme Klassen vs. Lambda – `this` im Vergleich [Fortgeschritten]

Ein wesentlicher Unterschied zwischen anonymer Klasse und Lambda ist die Bedeutung von `this`. In einer anonymen Klasse verweist `this` auf die anonyme Instanz selbst. In einem Lambda verweist `this` auf die umschließende Instanz der äußeren Klasse. Dieser Unterschied ist prüfungsrelevant.

```java
public class ThisVergleich {
    private String name = "AeussereKlasse";

    @FunctionalInterface
    interface Identifizierbar {
        String wer();
    }

    public void demonstrieren() {
        // Anonyme Klasse: this = die anonyme Instanz
        Identifizierbar anon = new Identifizierbar() {
            private String name = "AnonymeKlasse"; // eigenes Feld

            @Override
            public String wer() {
                // this bezieht sich auf die anonyme Instanz
                return "Anon.this.name = " + this.name
                    + " | Äußeres: " + ThisVergleich.this.name;
            }
        };

        // Lambda: this = die äußere Instanz (ThisVergleich)
        Identifizierbar lambda = () -> {
            // Kein eigenes 'this' für das Lambda selbst
            return "Lambda: this.name = " + this.name; // this = ThisVergleich-Instanz
        };

        System.out.println(anon.wer());
        // → Anon.this.name = AnonymeKlasse | Äußeres: AeussereKlasse

        System.out.println(lambda.wer());
        // → Lambda: this.name = AeussereKlasse
    }

    public static void main(String[] args) {
        new ThisVergleich().demonstrieren();
    }
}
```

**Merksätze für die Prüfung:**
- `this` in **anonymer Klasse** = die anonyme Instanz selbst. Zugriff auf äußere Instanz: `AeussereKlasse.this`.
- `this` in **Lambda** = immer die umschließende Instanz der äußeren Klasse (kein eigenes `this`).
- Anonyme Klassen können **konkrete** Klassen erweitern – der Superklassen-Konstruktor wird aufgerufen.
- Anonyme Klassen können eigene Felder und Methoden deklarieren, diese sind aber nur über eine Typcast-Variable zugänglich.

### 2.4 Anonyme Klassen vs. Lambda – Gegenüberstellung

```java
import java.util.*;

public class AnonVsLambda {
    @FunctionalInterface
    interface Berechnung {
        int berechne(int a, int b);
    }

    public static void main(String[] args) {
        // Anonyme Klasse (alt)
        Berechnung additionAnon = new Berechnung() {
            @Override
            public int berechne(int a, int b) {
                return a + b;
            }
        };

        // Lambda (modern, bevorzugt für funktionale Interfaces)
        Berechnung additionLambda = (a, b) -> a + b;

        System.out.println("Anon: " + additionAnon.berechne(3, 4));    // 7
        System.out.println("Lambda: " + additionLambda.berechne(3, 4)); // 7

        // Wann anonyme Klassen noch sinnvoll sind:
        // 1. Wenn das Interface mehr als eine Methode hat
        // 2. Wenn Zustand (Felder) benötigt wird
        // 3. Wenn die eigene Identität (this) der anonymen Instanz benötigt wird
        // 4. Wenn eine konkrete Klasse erweitert werden soll
    }
}
```

---

## 3. Funktionale Interfaces

### 3.1 Definition und @FunctionalInterface

Ein funktionales Interface hat genau eine abstrakte Methode. Es kann beliebig viele Default- und statische Methoden haben.

```java
import java.util.function.*;

// Eigenes funktionales Interface
@FunctionalInterface
public interface Konverter<F, T> {
    T konvertiere(F von);

    // Default-Methoden sind erlaubt
    default <V> Konverter<F, V> andThen(Konverter<T, V> nach) {
        return (F f) -> nach.konvertiere(this.konvertiere(f));
    }

    // Statische Methoden sind erlaubt
    static <T> Konverter<T, T> identitaet() {
        return t -> t;
    }

    // @FunctionalInterface verhindert versehentliche zweite abstrakte Methode:
    // void zweiteMethode(); // Compilerfehler!
}

class KonverterDemo {
    public static void main(String[] args) {
        // Lambda als Implementierung
        Konverter<String, Integer> stringZuInt = Integer::parseInt;
        System.out.println("42 als Int: " + stringZuInt.konvertiere("42")); // 42

        Konverter<Integer, String> intZuString = n -> "Zahl: " + n;

        // Verkettung mit andThen
        Konverter<String, String> kombiniert = stringZuInt.andThen(intZuString);
        System.out.println(kombiniert.konvertiere("123")); // Zahl: 123

        // Identität
        Konverter<String, String> id = Konverter.identitaet();
        System.out.println(id.konvertiere("unverändert")); // unverändert
    }
}
```

### 3.2 java.util.function – Standard-Interfaces

```java
import java.util.function.*;
import java.util.*;

public class StandardFunctionalInterfaces {
    public static void main(String[] args) {
        // --- Predicate<T>: T -> boolean ---
        Predicate<String> istLang = s -> s.length() > 5;
        Predicate<String> beginntMitA = s -> s.startsWith("A");

        System.out.println("'Hallo' ist lang: " + istLang.test("Hallo"));          // false
        System.out.println("'Alexander' lang: " + istLang.test("Alexander"));       // true

        // Predicate-Kombinationen
        Predicate<String> langUndMitA = istLang.and(beginntMitA);
        Predicate<String> langOderMitA = istLang.or(beginntMitA);
        Predicate<String> nichtLang = istLang.negate();

        System.out.println("and: " + langUndMitA.test("Alexander")); // true (lang UND mit A)
        System.out.println("or:  " + langOderMitA.test("Anna"));     // true (mit A)
        System.out.println("not: " + nichtLang.test("Hi"));          // true

        // --- Function<T, R>: T -> R ---
        Function<String, Integer> laenge = String::length;
        Function<Integer, String> verdoppelt = n -> n + n + " (verdoppelt)";

        // compose: g(f(x)) -- zuerst compose-Argument, dann this
        Function<String, String> zuerst = laenge.andThen(verdoppelt);
        System.out.println(zuerst.apply("Hallo")); // 10 (verdoppelt)

        // compose: erst vorherge Funktion, dann aktuelle
        Function<Integer, Integer> mal2 = x -> x * 2;
        Function<Integer, Integer> plus3 = x -> x + 3;

        Function<Integer, Integer> mal2DannPlus3 = mal2.andThen(plus3);
        Function<Integer, Integer> plus3DannMal2 = mal2.compose(plus3);

        System.out.println("5 * 2 + 3 = " + mal2DannPlus3.apply(5)); // 13
        System.out.println("(5 + 3) * 2 = " + plus3DannMal2.apply(5)); // 16

        // Function.identity()
        Function<String, String> id = Function.identity();
        System.out.println(id.apply("unverändert")); // unverändert

        // --- Consumer<T>: T -> void ---
        Consumer<String> drucken = System.out::println;
        Consumer<String> mitPrefix = s -> System.out.println(">> " + s);

        drucken.accept("Direkt ausgeben");
        // andThen: beide Consumer nacheinander
        Consumer<String> beides = drucken.andThen(mitPrefix);
        beides.accept("Zweifach");

        // --- Supplier<T>: () -> T ---
        Supplier<List<String>> listeErstellen = ArrayList::new;
        Supplier<Double> zufallsZahl = Math::random;

        List<String> neueListe = listeErstellen.get();
        neueListe.add("Element");
        System.out.println("Liste: " + neueListe);
        System.out.println("Zufall: " + zufallsZahl.get());

        // --- BiFunction<T, U, R>: (T, U) -> R ---
        BiFunction<String, Integer, String> wiederholen = (s, n) -> s.repeat(n);
        System.out.println(wiederholen.apply("Ha", 3)); // HaHaHa

        // --- Weitere Varianten ---
        BiPredicate<String, Integer> laengeGleich = (s, n) -> s.length() == n;
        BiConsumer<String, String> paare = (a, b) -> System.out.println(a + " -> " + b);
        UnaryOperator<String> grossbuchstaben = String::toUpperCase;
        BinaryOperator<Integer> addieren = (a, b) -> a + b;

        System.out.println(laengeGleich.test("Hallo", 5)); // true
        paare.accept("Schlüssel", "Wert");
        System.out.println(grossbuchstaben.apply("hello")); // HELLO
        System.out.println(addieren.apply(3, 4)); // 7
    }
}
```

### 3.3 Primitive Spezialformen

```java
import java.util.function.*;

public class PrimitiveFunctionalInterfaces {
    public static void main(String[] args) {
        // Primitive Versionen vermeiden Boxing/Unboxing

        // IntPredicate, LongPredicate, DoublePredicate
        IntPredicate geradeZahl = n -> n % 2 == 0;
        System.out.println("4 gerade: " + geradeZahl.test(4)); // true

        // IntFunction<R>, LongFunction<R>, DoubleFunction<R>
        IntFunction<String> intZuString = n -> "Zahl: " + n;
        System.out.println(intZuString.apply(42)); // Zahl: 42

        // ToIntFunction<T>, ToLongFunction<T>, ToDoubleFunction<T>
        ToIntFunction<String> stringLaenge = String::length;
        System.out.println("Länge: " + stringLaenge.applyAsInt("Hallo")); // 5

        // IntSupplier, LongSupplier, DoubleSupplier
        IntSupplier konstante = () -> 42;
        System.out.println("Konstante: " + konstante.getAsInt()); // 42

        // IntConsumer, LongConsumer, DoubleConsumer
        IntConsumer druckeInt = n -> System.out.println("Int: " + n);
        druckeInt.accept(100);

        // IntUnaryOperator, IntBinaryOperator
        IntUnaryOperator quadrat = n -> n * n;
        IntBinaryOperator max = Math::max;
        System.out.println("9²: " + quadrat.applyAsInt(9));          // 81
        System.out.println("max(7,3): " + max.applyAsInt(7, 3));      // 7
    }
}
```

---

## 4. Lambda-Ausdrücke

### 4.1 Lambda-Syntax

Ein Lambda-Ausdruck ist ein kompakter Ausdruck für eine anonyme Funktion. Er implementiert genau ein funktionales Interface.

```
(Parameter) -> { Rumpf }
```

```java
import java.util.function.*;
import java.util.*;

public class LambdaSyntaxDemo {
    public static void main(String[] args) {
        // Vollständige Syntax
        Comparator<String> vollstaendig = (String a, String b) -> {
            return a.compareTo(b);
        };

        // Typinferenz (Typen weglassen)
        Comparator<String> ohneTypen = (a, b) -> {
            return a.compareTo(b);
        };

        // Ausdruck-Lambda (kein return, kein Block nötig)
        Comparator<String> ausdruck = (a, b) -> a.compareTo(b);

        // Kein Parameter
        Runnable ohneParameter = () -> System.out.println("Läuft!");

        // Ein Parameter (Klammern optional)
        Consumer<String> einParameter = s -> System.out.println(s);
        // oder:
        Consumer<String> mitKlammern = (s) -> System.out.println(s);

        // Mehrere Anweisungen (Block-Lambda)
        Consumer<String> mehrereAnweisungen = s -> {
            String gross = s.toUpperCase();
            System.out.println(gross);
            System.out.println("Länge: " + s.length());
        };

        // Alle aufrufen
        ohneParameter.run();
        einParameter.accept("Hallo");
        mehrereAnweisungen.accept("Welt");

        // Lambda als Argument
        List<String> namen = Arrays.asList("Charlie", "Alice", "Bob");
        namen.sort((a, b) -> a.compareTo(b));
        namen.forEach(name -> System.out.println(name));
    }
}
```

### 4.2 Variable Capture in Lambdas [Fortgeschritten]

Lambdas können auf lokale Variablen des umgebenden Kontexts zugreifen, aber nur wenn diese **effektiv final** sind – d.h. nach der Initialisierung nie neu zugewiesen werden. Jede Neuzuweisung **nach** der Deklaration, auch nach dem Lambda, macht die Variable nicht mehr effektiv final. Das ist besonders bei Schleifenvariablen und Exception-Szenarien prüfungsrelevant.

```java
import java.util.function.*;
import java.util.*;

public class VariableCapture {
    private String instanzFeld = "Instanzfeld";
    private static String statischesFeld = "Statisches Feld";

    public void demonstrieren() {
        String lokaleVariable = "Lokale Variable"; // muss effektiv final sein
        int zahl = 42; // effektiv final

        // Zugriff auf effektiv finale lokale Variable
        Runnable r1 = () -> System.out.println(lokaleVariable);

        // Zugriff auf Instanzfelder (immer erlaubt)
        Runnable r2 = () -> System.out.println(instanzFeld);

        // Zugriff auf statische Felder (immer erlaubt)
        Runnable r3 = () -> System.out.println(statischesFeld);

        // COMPILERFEHLER: lokaleVariable = "Geändert"; → nicht mehr effektiv final!

        // Effektiv final: nie explizit als final deklariert, aber auch nie geändert
        int[] zaehler = {0}; // Trick für mutable state (Workaround, vermeiden!)
        Runnable r4 = () -> zaehler[0]++; // Array-Referenz ist final, Inhalt nicht

        r1.run();
        r2.run();
        r3.run();
        r4.run();
        System.out.println("Zähler: " + zaehler[0]); // 1

        // this in Lambda = äußere Instanz (kein separates this wie in anon. Klassen)
        Runnable r5 = () -> System.out.println(this.instanzFeld);
        r5.run();
    }

    // --- Prüfungsszenarien: effektiv final ---
    public static void examScenarios() {
        // Szenario 1: Schleifenvariable
        // COMPILERFEHLER: for-Schleifenzähler ist NICHT effektiv final
        // for (int i = 0; i < 3; i++) {
        //     Runnable r = () -> System.out.println(i); // FEHLER: i wird hochgezählt
        // }

        // Korrekt: neue Variable im Schleifenrumpf erzeugen (effektiv final)
        for (int i = 0; i < 3; i++) {
            final int snapshot = i; // effektiv final (nur einmal zugewiesen)
            Runnable r = () -> System.out.println(snapshot);
            r.run(); // 0, 1, 2
        }

        // Szenario 2: Zuweisung nach Lambda-Definition ist ebenfalls ein Fehler
        // int wert = 10;
        // Runnable r = () -> System.out.println(wert); // würde Fehler geben...
        // wert = 20; // ...weil diese Zeile wert nicht mehr effektiv final macht

        // Szenario 3: Bedingte Zuweisung – auch Compilerfehler
        // int x;
        // if (Math.random() > 0.5) x = 1; else x = 2;
        // Runnable r = () -> System.out.println(x); // FEHLER: x könnte mehrfach zugewiesen sein

        // Szenario 4: Effektiv final in try-catch
        String nachricht;
        try {
            nachricht = "Erfolg";
        } catch (Exception e) {
            nachricht = "Fehler"; // ZWEI mögliche Zuweisungen → nicht effektiv final!
            // Runnable r = () -> System.out.println(nachricht); // COMPILERFEHLER hier
            return;
        }
        // Hier: nur ein Pfad → nachricht ist effektiv final
        Runnable r = () -> System.out.println(nachricht); // OK
        r.run();

        // Szenario 5: for-each – Schleifenvariable IST effektiv final pro Iteration
        List<String> namen = List.of("Alice", "Bob");
        for (String name : namen) {
            // name wird pro Iteration einmal zugewiesen, nie geändert → effektiv final
            Runnable ausgabe = () -> System.out.println(name);
            ausgabe.run();
        }
    }

    public static void main(String[] args) {
        new VariableCapture().demonstrieren();
        examScenarios();
    }
}
```

**Merksätze für die Prüfung:**
- **Effektiv final** = nach Initialisierung nie neu zugewiesen. `final` als Schlüsselwort ist optional.
- Eine Neuzuweisung **irgendwo** im selben Scope (auch nach dem Lambda) macht die Variable ungültig.
- `for (int i = ...)` – `i` ist **nicht** effektiv final (wird jede Iteration inkrementiert).
- `for (String s : collection)` – `s` **ist** effektiv final pro Iteration.
- Instanz- und Klassenfelder unterliegen dieser Einschränkung **nicht** – sie sind immer zugänglich.

---

## 5. Lambda-Syntax – Erweitert

### 5.1 Predicate-Kombinationen

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class PredicateKombinationen {
    record Person(String name, int alter, String stadt) {}

    public static void main(String[] args) {
        List<Person> personen = List.of(
            new Person("Alice", 30, "Berlin"),
            new Person("Bob", 17, "München"),
            new Person("Charlie", 25, "Berlin"),
            new Person("Diana", 15, "Hamburg"),
            new Person("Eve", 35, "München")
        );

        // Einzelne Predicates
        Predicate<Person> volljährig = p -> p.alter() >= 18;
        Predicate<Person> ausBerlin = p -> p.stadt().equals("Berlin");
        Predicate<Person> jungerErwachsener = p -> p.alter() >= 18 && p.alter() <= 25;

        // Kombinationen
        Predicate<Person> volljährigAusBerlin = volljährig.and(ausBerlin);
        Predicate<Person> berlinOderMünchen = ausBerlin.or(p -> p.stadt().equals("München"));
        Predicate<Person> nichtVolljährig = volljährig.negate();

        System.out.println("Volljährig aus Berlin:");
        personen.stream()
            .filter(volljährigAusBerlin)
            .forEach(p -> System.out.println("  " + p.name()));

        System.out.println("Nicht volljährig:");
        personen.stream()
            .filter(nichtVolljährig)
            .forEach(p -> System.out.println("  " + p.name() + " (" + p.alter() + ")"));

        System.out.println("Berlin oder München:");
        personen.stream()
            .filter(berlinOderMünchen)
            .forEach(p -> System.out.println("  " + p.name() + " aus " + p.stadt()));

        // Predicate.not() (Java 11+) - negate als statische Methode
        Predicate<String> leer = String::isBlank;
        List<String> bereinigt = List.of("Alice", "", "  ", "Bob", "")
            .stream()
            .filter(Predicate.not(String::isBlank))
            .collect(Collectors.toList());
        System.out.println("Nicht leer: " + bereinigt);
    }
}
```

### 5.2 Function-Verkettung

```java
import java.util.function.*;

public class FunctionVerkettung {
    public static void main(String[] args) {
        // Einzelne Funktionen
        Function<String, String> trimmen = String::trim;
        Function<String, String> kleinbuchstaben = String::toLowerCase;
        Function<String, Integer> laenge = String::length;
        Function<Integer, String> bewerten = n ->
            n > 10 ? "lang" : n > 5 ? "mittel" : "kurz";

        // andThen: von links nach rechts
        Function<String, String> normalisieren = trimmen.andThen(kleinbuchstaben);
        System.out.println(normalisieren.apply("  HALLO WELT  ")); // hallo welt

        // Kette von Transformationen
        Function<String, String> analyse = trimmen
            .andThen(kleinbuchstaben)
            .andThen(s -> s.replaceAll("\\s+", "_"))
            .andThen(s -> "[" + s + "]");

        System.out.println(analyse.apply("  Hallo Welt  ")); // [hallo_welt]

        // compose: von rechts nach links (andThen ist meist lesbarer)
        Function<String, Integer> laengeNachTrim = laenge.compose(trimmen);
        System.out.println("Länge nach trim: " + laengeNachTrim.apply("  Hi  ")); // 2

        // Kombination mit anderen funktionalen Interfaces
        Function<String, String> pipeline = trimmen
            .andThen(kleinbuchstaben);

        String[] texte = {"  JAVA  ", " Python ", " KOTLIN "};
        for (String text : texte) {
            System.out.println(pipeline.apply(text));
        }
    }
}
```

### 5.3 Lambdas als first-class citizens

```java
import java.util.*;
import java.util.function.*;

public class FirstClassFunctions {
    // Lambda als Rückgabewert
    public static Predicate<Integer> grösserAls(int schwellwert) {
        return n -> n > schwellwert;
    }

    // Lambda als Parameter (Higher-Order Function)
    public static <T> List<T> filtern(List<T> liste, Predicate<T> bedingung) {
        List<T> ergebnis = new ArrayList<>();
        for (T element : liste) {
            if (bedingung.test(element)) {
                ergebnis.add(element);
            }
        }
        return ergebnis;
    }

    // Lambda in Map speichern
    public static Map<String, Function<Integer, Integer>> rechenOperationen() {
        Map<String, Function<Integer, Integer>> ops = new HashMap<>();
        ops.put("verdoppeln", n -> n * 2);
        ops.put("quadrieren", n -> n * n);
        ops.put("negieren", n -> -n);
        ops.put("inkrementieren", n -> n + 1);
        return ops;
    }

    public static void main(String[] args) {
        // Lambda als Rückgabewert
        Predicate<Integer> grösserAls10 = grösserAls(10);
        Predicate<Integer> grösserAls100 = grösserAls(100);

        System.out.println("15 > 10: " + grösserAls10.test(15));  // true
        System.out.println("15 > 100: " + grösserAls100.test(15)); // false

        // Lambda als Parameter
        List<Integer> zahlen = Arrays.asList(1, 5, 10, 15, 20, 25);
        System.out.println("Über 10: " + filtern(zahlen, grösserAls10));

        // Lambdas in Map
        Map<String, Function<Integer, Integer>> ops = rechenOperationen();
        int wert = 5;
        ops.forEach((name, funktion) ->
            System.out.println(name + "(" + wert + ") = " + funktion.apply(wert))
        );
    }
}
```

---

## 6. Methoden-Referenzen (Method References)

### 6.1 Die vier Arten von Methoden-Referenzen

Eine Methoden-Referenz ist eine Kurzform eines Lambda-Ausdrucks, der nur eine vorhandene Methode aufruft.

```
Lambda:              (params) -> ClassName.method(params)
Methoden-Referenz:   ClassName::method
```

| Art | Syntax | Äquivalentes Lambda |
|---|---|---|
| Statische Methode | `ClassName::staticMethod` | `(args) -> ClassName.staticMethod(args)` |
| Instanzmethode (bestimmtes Objekt) | `object::instanceMethod` | `(args) -> object.instanceMethod(args)` |
| Instanzmethode (beliebiges Objekt) | `ClassName::instanceMethod` | `(obj, args) -> obj.instanceMethod(args)` |
| Konstruktor | `ClassName::new` | `(args) -> new ClassName(args)` |

### 6.2 Statische Methoden-Referenz

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class StatischeMethodenReferenz {
    public static boolean istGeradeZahl(int n) {
        return n % 2 == 0;
    }

    public static String formatiereDoppelt(String s) {
        return s.toUpperCase() + " - " + s.toLowerCase();
    }

    public static int vergleiche(String a, String b) {
        return a.compareToIgnoreCase(b);
    }

    public static void main(String[] args) {
        List<Integer> zahlen = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        // Lambda: n -> StatischeMethodenReferenz.istGeradeZahl(n)
        // Methoden-Referenz:
        List<Integer> gerade = zahlen.stream()
            .filter(StatischeMethodenReferenz::istGeradeZahl)
            .collect(Collectors.toList());
        System.out.println("Gerade: " + gerade);

        // Verwendung mit vorhandenen Java-Methoden
        List<String> texte = Arrays.asList("hello", "world", "java");

        // String::toUpperCase (Instanzmethode als statische Ref für beliebiges Objekt)
        List<String> gross = texte.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        System.out.println("Groß: " + gross);

        // Integer::parseInt
        List<String> zahlenTexte = Arrays.asList("1", "2", "3", "4");
        List<Integer> parsed = zahlenTexte.stream()
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        System.out.println("Parsed: " + parsed);

        // Eigene statische Methoden
        texte.stream()
            .map(StatischeMethodenReferenz::formatiereDoppelt)
            .forEach(System.out::println);

        // Sortierung mit statischer Referenz
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "alice", "Bob"));
        names.sort(StatischeMethodenReferenz::vergleiche);
        System.out.println("Sortiert (ignore case): " + names);
    }
}
```

### 6.3 Instanzmethoden-Referenz (bestimmtes Objekt)

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class InstanzMethodenReferenzBestimmt {
    public static void main(String[] args) {
        // Referenz auf Methode eines bestimmten Objekts
        String prefix = "-> ";
        // Lambda: s -> prefix.concat(s)    (prefix ist effektiv final)
        // Methoden-Referenz: prefix::concat
        Function<String, String> mitPrefix = prefix::concat;

        System.out.println(mitPrefix.apply("Hallo")); // -> Hallo
        System.out.println(mitPrefix.apply("Welt"));  // -> Welt

        // PrintStream::println (System.out ist ein bestimmtes Objekt)
        Consumer<String> drucken = System.out::println;
        drucken.accept("Ausgabe über Methoden-Referenz");

        // StringBuilder akkumulieren
        StringBuilder sb = new StringBuilder();
        Consumer<String> anhängen = sb::append;
        List<String> worte = Arrays.asList("Java", " ", "ist", " ", "toll");
        worte.forEach(anhängen);
        System.out.println("Ergebnis: " + sb); // Java ist toll

        // Comparator aus einem bestimmten String
        String vergleichsBasis = "Hamburg";
        Comparator<String> nachHamburg = vergleichsBasis::compareTo;
        List<String> staedte = new ArrayList<>(
            Arrays.asList("Berlin", "München", "Frankfurt", "Hamburg", "Köln")
        );
        staedte.sort(nachHamburg);
        System.out.println("Relativ zu Hamburg: " + staedte);

        // Praktisches Beispiel: Validator-Kette
        String muster = "\\d{5}";
        Predicate<String> istPLZ = s -> s.matches(muster);
        // Äquivalent mit Methoden-Referenz auf Pattern:
        java.util.regex.Pattern plzMuster = java.util.regex.Pattern.compile("\\d{5}");
        Predicate<String> istPLZRef = plzMuster.asMatchPredicate();

        List<String> eingaben = Arrays.asList("10115", "ABCDE", "12345", "1234X");
        System.out.println("Gültige PLZ: " + eingaben.stream()
            .filter(istPLZRef)
            .collect(Collectors.toList()));
    }
}
```

### 6.4 Instanzmethoden-Referenz (beliebiges Objekt)

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class InstanzMethodenReferenzBeliebigesObjekt {
    record Produkt(String name, double preis) {
        // Instanzmethode
        boolean istTeuer() {
            return preis > 100;
        }

        String formatiert() {
            return String.format("%-15s %.2f EUR", name, preis);
        }
    }

    public static void main(String[] args) {
        List<String> names = Arrays.asList("Charlie", "alice", "BOB", "Diana");

        // String::toLowerCase - für beliebiges String-Objekt
        // Lambda: s -> s.toLowerCase()
        List<String> klein = names.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        System.out.println("Klein: " + klein);

        // String::length
        List<Integer> laengen = names.stream()
            .map(String::length)
            .collect(Collectors.toList());
        System.out.println("Längen: " + laengen);

        // String::isEmpty
        List<String> mitLeer = Arrays.asList("A", "", "B", "", "C");
        long anzahlLeer = mitLeer.stream()
            .filter(String::isEmpty)
            .count();
        System.out.println("Leere Strings: " + anzahlLeer);

        // Eigene Klasse: Produkt::istTeuer
        List<Produkt> produkte = List.of(
            new Produkt("Apfel", 0.99),
            new Produkt("Laptop", 999.99),
            new Produkt("Buch", 29.99),
            new Produkt("Handy", 699.99)
        );

        // Lambda: p -> p.istTeuer()
        System.out.println("Teure Produkte:");
        produkte.stream()
            .filter(Produkt::istTeuer)
            .map(Produkt::formatiert)
            .forEach(System.out::println);

        // Comparator mit Instanzmethode
        produkte.stream()
            .sorted(Comparator.comparing(Produkt::preis))
            .forEach(p -> System.out.println(p.formatiert()));
    }
}
```

### 6.5 Konstruktor-Referenz

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class KonstruktorReferenz {
    record Person(String name, int alter) {}

    static class Verbindung {
        private final String host;
        private final int port;

        Verbindung(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }
    }

    public static void main(String[] args) {
        // Supplier<T> = () -> new T()
        Supplier<ArrayList<String>> listeErstellen = ArrayList::new;
        ArrayList<String> neueListe = listeErstellen.get();
        System.out.println("Neue Liste: " + neueListe.getClass().getSimpleName());

        // Function<String, Person> = name -> new Person(name, 0)
        // Hier: eigene Factory wäre sinnvoller, aber Konstruktor-Ref mit einem Param:
        // (Für Person mit 2 Params brauchen wir BiFunction)

        // BiFunction<String, Integer, Person>
        BiFunction<String, Integer, Person> personErstellen = Person::new;
        Person p = personErstellen.apply("Alice", 30);
        System.out.println("Person: " + p);

        // Aus Strings eine Liste von Personen erstellen
        List<String> namen = Arrays.asList("Alice", "Bob", "Charlie");
        // Benötigt einparametrischen Konstruktor:
        // Wenn Person(String) existieren würde:
        // List<Person> personen = namen.stream()
        //     .map(Person::new)
        //     .collect(Collectors.toList());

        // Array-Konstruktor-Referenz
        // IntFunction<T[]> = n -> new T[n]
        IntFunction<String[]> arrayErstellen = String[]::new;
        String[] array = arrayErstellen.apply(5);
        System.out.println("Array-Größe: " + array.length);

        // Stream.toArray mit Konstruktor-Referenz
        List<String> quellliste = Arrays.asList("A", "B", "C");
        String[] ergebnisArray = quellliste.stream().toArray(String[]::new);
        System.out.println("Array: " + Arrays.toString(ergebnisArray));

        // BiFunction für Verbindung
        BiFunction<String, Integer, Verbindung> verbindung = Verbindung::new;
        List<String> hosts = Arrays.asList("localhost", "server1", "server2");
        List<Verbindung> verbindungen = hosts.stream()
            .map(host -> verbindung.apply(host, 8080))
            .collect(Collectors.toList());
        verbindungen.forEach(System.out::println);
    }
}
```

### 6.6 Methoden-Referenzen – Zusammenfassung

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class MethodenReferenzZusammenfassung {
    static void statischeMethode(String s) {
        System.out.println("Statisch: " + s);
    }

    void instanzMethode(String s) {
        System.out.println("Instanz: " + s);
    }

    public static void main(String[] args) {
        MethodenReferenzZusammenfassung obj = new MethodenReferenzZusammenfassung();
        List<String> liste = Arrays.asList("A", "B", "C");

        // 1. Statisch
        liste.forEach(MethodenReferenzZusammenfassung::statischeMethode);

        // 2. Bestimmtes Objekt
        liste.forEach(obj::instanzMethode);

        // 3. Beliebiges Objekt (Instanzmethode des ersten Parameters)
        liste.stream()
            .map(String::toUpperCase) // s -> s.toUpperCase()
            .forEach(System.out::println);

        // 4. Konstruktor
        Supplier<ArrayList<String>> listFactory = ArrayList::new;

        // Vergleich Lambda vs. Methoden-Referenz:
        System.out.println("\n--- Lambda vs. Methoden-Referenz ---");

        // Lambda:           s -> System.out.println(s)
        // Methoden-Referenz: System.out::println
        Consumer<String> lambda = s -> System.out.println(s);
        Consumer<String> ref = System.out::println;

        lambda.accept("Lambda");
        ref.accept("Methoden-Referenz");

        // Wann Methoden-Referenz verwenden:
        // - Wenn Lambda nur eine Methode aufruft
        // - Wenn der Code dadurch lesbarer wird
        // - Immer wenn möglich (Code wird kürzer)

        // Wann Lambda verwenden:
        // - Wenn Logik mehr als nur einen Aufruf enthält
        // - Wenn Parameter transformiert werden
        // - Wenn mehrere Ausdrücke kombiniert werden
    }
}
```

---

## Zusammenfassung

### Arten verschachtelter Klassen

| Art | Zugriff auf äußere Instanz | Instanziierung | Typischer Einsatz |
|---|---|---|---|
| Static Nested | Nein (nur static) | `Outer.Inner obj = new Outer.Inner()` | Builder, Hilfsklassen |
| Inner Class | Ja (alle Member) | `outer.new Inner()` | Event-Handler, Iterator |
| Local Class | Effektiv finale Variablen | Nur in der Methode | Einmalige Hilfsklassen |
| Anonymous Class | Effektiv finale Variablen | `new Interface() {}` | Kurzlebige Implementierungen |

### Funktionale Interfaces (java.util.function)

| Interface | Signatur | Methode | Kombination |
|---|---|---|---|
| `Predicate<T>` | `T -> boolean` | `test()` | `and()`, `or()`, `negate()` |
| `Function<T,R>` | `T -> R` | `apply()` | `andThen()`, `compose()` |
| `Consumer<T>` | `T -> void` | `accept()` | `andThen()` |
| `Supplier<T>` | `() -> T` | `get()` | - |
| `BiFunction<T,U,R>` | `(T,U) -> R` | `apply()` | `andThen()` |
| `UnaryOperator<T>` | `T -> T` | `apply()` | erbt von Function |
| `BinaryOperator<T>` | `(T,T) -> T` | `apply()` | erbt von BiFunction |

### Methoden-Referenz-Typen

```
Statische Methode:         ClassName::staticMethod
Instanz (best. Objekt):    object::instanceMethod
Instanz (bel. Objekt):     ClassName::instanceMethod
Konstruktor:               ClassName::new
```

---

## Übungsaufgaben

### Aufgabe 1: Lokale Klassen – Zugriff [Fortgeschritten]

Welche der folgenden lokalen Klassen kompiliert fehlerfrei?

```java
public class Outer {
    private int instanzWert = 10;

    public static void statisch() {
        int x = 5;
        class A {
            void methode() {
                System.out.println(x); // (a)
                // System.out.println(instanzWert); // (b)
            }
        }
    }

    public void instanz() {
        int y = 7;
        class B {
            void methode() {
                System.out.println(y);          // (c)
                System.out.println(instanzWert); // (d)
            }
        }
    }
}
```

**Antwort:** (a), (c) und (d) kompilieren. (b) ist ein Compilerfehler, weil `instanzWert` ein Instanzfeld ist und `statisch()` eine statische Methode – kein `this` verfügbar.

---

### Aufgabe 2: Effectively Final – was kompiliert? [Fortgeschritten]

Markieren Sie jede Variable als effektiv final (EF) oder nicht (NEF):

```java
public static void test() {
    int a = 1;                        // (1)
    int b = 2; b = 3;                 // (2)
    final int c = 4;                  // (3)
    int d;                            // (4)
    if (true) d = 5; else d = 6;      // (4) Zuweisung
    String e = "x";                   // (5)

    Runnable r1 = () -> System.out.println(a); // OK?
    Runnable r2 = () -> System.out.println(b); // OK?
    Runnable r3 = () -> System.out.println(c); // OK?
    Runnable r4 = () -> System.out.println(d); // OK?
    Runnable r5 = () -> System.out.println(e); // OK?
}
```

**Antwort:** (1) EF → r1 OK. (2) NEF → r2 FEHLER. (3) EF (explizit final) → r3 OK. (4) NEF (zwei mögliche Zuweisungen) → r4 FEHLER. (5) EF → r5 OK.

---

## Multiple-Choice-Fragen

### Lokale Klassen

**Frage 1:** Eine lokale Klasse wird innerhalb einer **statischen** Methode definiert. Welche Aussage ist korrekt?

- A) Sie kann auf Instanzfelder der äußeren Klasse zugreifen, wenn sie `OuterClass.this` verwendet.
- B) Sie kann keine Interfaces implementieren.
- **C) Sie hat keinen Zugriff auf Instanzfelder der äußeren Klasse, aber auf effektiv finale lokale Variablen.** ✓
- D) Sie kann statische Methoden definieren.

**Frage 2:** Welche Aussage über lokale Klassen ist FALSCH?

- A) Sie können andere Klassen (auch konkrete) erweitern.
- B) Sie können Interfaces implementieren.
- **C) Sie sind außerhalb ihrer Methode per vollständigem Klassennamen erreichbar.** ✓
- D) Sie können auf effektiv finale Parameter der umgebenden Methode zugreifen.

**Frage 3:** Eine lokale Klasse in einer Instanzmethode möchte auf das Feld `name` der äußeren Klasse zugreifen. Wie referenziert sie es korrekt?

- A) `super.name`
- B) `outer.name`
- **C) Direkt als `name` oder `OuterClass.this.name`** ✓
- D) Nur über einen Konstruktor-Parameter

---

### Effectively Final in Lambdas

**Frage 4:** Welcher Code kompiliert fehlerfrei?

```java
// Option A
int x = 5;
x = 6;
Runnable r = () -> System.out.println(x);

// Option B
int y = 5;
Runnable r = () -> System.out.println(y);

// Option C
for (int i = 0; i < 3; i++) {
    Runnable r = () -> System.out.println(i);
}

// Option D
int z = 5;
Runnable r = () -> System.out.println(z);
z = 6;
```

- A) Option A
- **B) Option B** ✓
- C) Option C
- D) Option D

**Frage 5:** Was gibt folgender Code aus?

```java
List<Runnable> tasks = new ArrayList<>();
for (int i = 0; i < 3; i++) {
    final int snap = i;
    tasks.add(() -> System.out.println(snap));
}
tasks.forEach(Runnable::run);
```

- A) Compilerfehler, `snap` ist nicht effektiv final.
- B) 0 0 0
- **C) 0 1 2** ✓
- D) 2 2 2

**Frage 6:** Welche Variable ist effektiv final?

```java
String a = "x";                   // (1)
String b = "x"; b = "y";          // (2)
String c;
c = "z";                          // (3) – nur einmal zugewiesen
```

- A) Nur (1)
- B) Nur (2)
- **C) (1) und (3)** ✓
- D) Alle drei

---

### Anonyme Klassen

**Frage 7:** Eine anonyme Klasse `new Basis() { ... }` wobei `Basis` eine **konkrete** (nicht-abstrakte) Klasse ist. Welche Aussage ist korrekt?

- A) Das ist ein Compilerfehler – anonyme Klassen können nur Interfaces implementieren.
- B) Das ist ein Compilerfehler – anonyme Klassen können nur abstrakte Klassen erweitern.
- **C) Es ist erlaubt; der Konstruktor von `Basis` wird aufgerufen.** ✓
- D) Es ist erlaubt, aber keine Methoden von `Basis` können überschrieben werden.

**Frage 8:** Was gibt folgender Code aus?

```java
public class Outer {
    String name = "Outer";

    void run() {
        Runnable lambda = () -> System.out.println(this.name);

        Runnable anon = new Runnable() {
            String name = "Anon";
            public void run() {
                System.out.println(this.name);
            }
        };

        lambda.run();
        anon.run();
    }

    public static void main(String[] args) { new Outer().run(); }
}
```

- A) Outer / Outer
- B) Anon / Anon
- **C) Outer / Anon** ✓
- D) Outer / null

**Frage 9:** Welche Eigenschaft unterscheidet eine anonyme Klasse von einem Lambda-Ausdruck?

- A) Lambda-Ausdrücke können keine lokalen Variablen erfassen.
- B) Anonyme Klassen können nur Interfaces implementieren.
- **C) `this` verweist in einer anonymen Klasse auf die anonyme Instanz, im Lambda auf die äußere Instanz.** ✓
- D) Lambda-Ausdrücke können immer anonyme Klassen vollständig ersetzen.

---

## Skill Check: Nested Classes und Lambda (Erweiterung)

Zusätzlich zum Basis-Skill-Check sollten Sie nach diesem Modul folgende Fähigkeiten demonstrieren können:

- [ ] Lokale Klasse in einer statischen Methode definieren, die ein Interface implementiert, ohne Zugriff auf Instanzfelder zu benötigen
- [ ] Erklären, warum `for (int i = 0; ...)` keine effektiv finale Schleifenvariable erzeugt, und das Problem mit einem `final`-Snapshot lösen
- [ ] Den Unterschied zwischen `this` in einer anonymen Klasse und `this` in einem Lambda an einem kompilierbaren Beispiel erklären
- [ ] Eine anonyme Klasse schreiben, die eine **konkrete** (nicht-abstrakte) Klasse erweitert und eine Methode überschreibt
- [ ] Einen `try-catch`-Block analysieren und beurteilen, ob eine darin initialisierte Variable effektiv final ist
