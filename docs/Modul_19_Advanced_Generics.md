# Modul 19: Advanced Generics – Fortgeschrittene Generics

## Übersicht

Generics machen Java-Code typsicher und wiederverwendbar. Dieses Modul vertieft das Verstaendnis von Generic-Klassen, beschraenkten Typparametern, Wildcards, dem PECS-Prinzip sowie den Grenzen der Typloeschung (Type Erasure).

| Abschnitt                      | Dauer |
|--------------------------------|-------|
| Generic Classes and Methods    | 35 m  |
| Bounded Type Parameters        | 20 m  |
| Wildcards and PECS             | 25 m  |
| Type Erasure                   | 18 m  |
| Advanced Patterns              | 12 m  |
| Practice 19-1                  | 25 m  |
| **Gesamt**                     | **135 m** |

> **Skill Check: Advanced Generics** – mind. 80 % erforderlich, um das Modul abzuschließen.

---

## 1. Generische Klassen

### 1.1 Grundlegende generische Klasse

```java
/**
 * Generischer Container fuer ein einzelnes Element.
 * T ist der Typparameter (Type Parameter).
 */
public class Behaelter<T> {

    private T inhalt;

    public Behaelter(T inhalt) {
        this.inhalt = inhalt;
    }

    public T getInhalt() {
        return inhalt;
    }

    public void setInhalt(T inhalt) {
        this.inhalt = inhalt;
    }

    public boolean istLeer() {
        return inhalt == null;
    }

    @Override
    public String toString() {
        return "Behaelter[" + inhalt + "]";
    }
}

// Verwendung
Behaelter<String>  textBox  = new Behaelter<>("Hallo");
Behaelter<Integer> zahlenBox = new Behaelter<>(42);
Behaelter<Double>  preisBox  = new Behaelter<>(9.99);

String text  = textBox.getInhalt();   // Kein Cast noetig
Integer zahl = zahlenBox.getInhalt(); // Kein Cast noetig
```

### 1.2 Mehrere Typparameter

```java
/**
 * Generisches Paar mit zwei Typparametern.
 */
public record Paar<A, B>(A erster, B zweiter) {

    /** Tauscht die Elemente. */
    public Paar<B, A> tauschen() {
        return new Paar<>(zweiter, erster);
    }

    /** Erstellt ein Paar aus zwei Werten. */
    public static <X, Y> Paar<X, Y> von(X x, Y y) {
        return new Paar<>(x, y);
    }
}

// Verwendung
Paar<String, Integer> nameAlter = Paar.von("Anna", 30);
Paar<Integer, String> getauscht = nameAlter.tauschen();
System.out.println(nameAlter); // Paar[erster=Anna, zweiter=30]
System.out.println(getauscht); // Paar[erster=30, zweiter=Anna]
```

### 1.3 Generische Klasse mit beschraenktem Typparameter

```java
/**
 * Ein generischer Stack, der nur Comparable-Werte akzeptiert
 * und den minimalen Wert ermitteln kann.
 */
public class SortierterStack<T extends Comparable<T>> {

    private final java.util.Deque<T> daten = new java.util.ArrayDeque<>();

    public void push(T element) {
        daten.push(element);
    }

    public T pop() {
        return daten.pop();
    }

    public T minimum() {
        return daten.stream()
            .min(Comparable::compareTo)
            .orElseThrow(() -> new java.util.NoSuchElementException("Stack ist leer"));
    }

    public T maximum() {
        return daten.stream()
            .max(Comparable::compareTo)
            .orElseThrow(() -> new java.util.NoSuchElementException("Stack ist leer"));
    }
}

// Verwendung
SortierterStack<Integer> stack = new SortierterStack<>();
stack.push(5);
stack.push(2);
stack.push(8);
stack.push(1);
System.out.println("Min: " + stack.minimum()); // 1
System.out.println("Max: " + stack.maximum()); // 8
```

---

## 2. Generische Methoden

### 2.1 Grundlegendes Muster

```java
public class GenerischeMethoden {

    /**
     * Tauscht zwei Elemente in einem Array.
     * Der Typparameter <T> wird vor dem Rueckgabetyp deklariert.
     */
    public static <T> void tauschen(T[] array, int i, int j) {
        T temp    = array[i];
        array[i]  = array[j];
        array[j]  = temp;
    }

    /**
     * Gibt das maximale Element zurueck (T muss Comparable sein).
     */
    public static <T extends Comparable<T>> T maximum(T a, T b, T c) {
        T max = a;
        if (b.compareTo(max) > 0) max = b;
        if (c.compareTo(max) > 0) max = c;
        return max;
    }

    /**
     * Konvertiert ein Array in eine Liste (typ-sicher).
     */
    public static <T> java.util.List<T> arrayZuListe(T[] array) {
        return java.util.Arrays.asList(array);
    }

    public static void main(String[] args) {
        Integer[] zahlen = {1, 2, 3, 4, 5};
        tauschen(zahlen, 0, 4);
        System.out.println(java.util.Arrays.toString(zahlen)); // [5, 2, 3, 4, 1]

        System.out.println(maximum(3, 7, 2));       // 7
        System.out.println(maximum("Ana", "Bea", "Alf")); // Bea

        String[] namen = {"Alice", "Bob", "Charlie"};
        java.util.List<String> liste = arrayZuListe(namen);
        System.out.println(liste); // [Alice, Bob, Charlie]
    }
}
```

### 2.2 Typ-Inferenz

```java
// Java leitet den Typparameter automatisch ab (Type Inference)
// Explizite Angabe ist selten noetig

// Implizit (empfohlen)
java.util.List<String> liste1 = java.util.Collections.emptyList();

// Explizit (selten noetig)
java.util.List<String> liste2 = java.util.Collections.<String>emptyList();

// Diamond Operator <> seit Java 7
java.util.Map<String, java.util.List<Integer>> map = new java.util.HashMap<>();
```

---

## 3. Begrenzte Typparameter (Bounded Type Parameters)

### 3.1 Upper Bound – `extends`

```java
/**
 * Berechnet die Summe einer Liste von Number-Subtypen.
 * <T extends Number> bedeutet: T muss Number oder eine Unterklasse sein.
 */
public static <T extends Number> double summe(java.util.List<T> liste) {
    double gesamt = 0.0;
    for (T element : liste) {
        gesamt += element.doubleValue(); // Number.doubleValue() ist verfuegbar
    }
    return gesamt;
}

// Korrekte Aufrufe
System.out.println(summe(java.util.List.of(1, 2, 3)));           // Integer -> 6.0
System.out.println(summe(java.util.List.of(1.5, 2.5, 3.0)));    // Double -> 7.0
System.out.println(summe(java.util.List.of(100L, 200L)));        // Long -> 300.0

// Compilerfehler:
// System.out.println(summe(java.util.List.of("a", "b"))); // String ist kein Number
```

### 3.2 Mehrfach-Bounds – Intersection Types

```java
// T muss Comparable UND Cloneable implementieren
public static <T extends Comparable<T> & Cloneable> T minimum(T a, T b) {
    return a.compareTo(b) <= 0 ? a : b;
}

// Hinweis: Bei mehreren Bounds muss eine Klasse zuerst stehen, Interfaces folgen
// <T extends KlasseA & InterfaceB & InterfaceC>
```

---

## 4. Wildcards

### 4.1 Unbegrenzte Wildcard `<?>`

Die unbegrenzte Wildcard bedeutet: "eine Liste von irgendeinem Typ".

```java
/**
 * Gibt alle Elemente einer beliebigen Liste aus.
 * List<?> akzeptiert List<String>, List<Integer>, List<Object> usw.
 */
public static void alleAusgeben(java.util.List<?> liste) {
    for (Object element : liste) {
        System.out.println(element);
    }
}

alleAusgeben(java.util.List.of("Hallo", "Welt"));  // OK
alleAusgeben(java.util.List.of(1, 2, 3));           // OK
alleAusgeben(java.util.List.of(3.14, 2.71));        // OK

// EINSCHRAENKUNG: Man kann KEIN Element hinzufuegen (ausser null)
java.util.List<?> liste = new java.util.ArrayList<String>();
// liste.add("Wert"); // Compilerfehler!
```

### 4.2 Upper-Bounded Wildcard `<? extends T>`

```java
/**
 * Upper-bounded Wildcard: ? extends Number
 * Bedeutet: eine Liste von Number oder einem Subtyp (Integer, Double, Long ...)
 * 
 * Verwendung: LESEN von Elementen (Producer)
 */
public static double summeMitWildcard(java.util.List<? extends Number> liste) {
    return liste.stream()
        .mapToDouble(Number::doubleValue)
        .sum();
}

java.util.List<Integer> integers = java.util.List.of(1, 2, 3);
java.util.List<Double>  doubles  = java.util.List.of(1.1, 2.2, 3.3);
java.util.List<Number>  numbers  = java.util.List.of(1, 2.0, 3L);

System.out.println(summeMitWildcard(integers)); // 6.0
System.out.println(summeMitWildcard(doubles));  // 6.6
System.out.println(summeMitWildcard(numbers));  // 6.0

// EINSCHRAENKUNG: Kein Hinzufuegen moeglich (Typ unbekannt)
// liste.add(5);     // Compilerfehler!
// liste.add(5.0);   // Compilerfehler!
// liste.add(null);  // Einzige Ausnahme
```

### 4.3 Lower-Bounded Wildcard `<? super T>`

```java
/**
 * Lower-bounded Wildcard: ? super Integer
 * Bedeutet: eine Liste von Integer oder einem Supertyp (Number, Object)
 * 
 * Verwendung: HINZUFUEGEN von Elementen (Consumer)
 */
public static void integersHinzufuegen(java.util.List<? super Integer> liste) {
    for (int i = 1; i <= 5; i++) {
        liste.add(i); // Integer kann immer hinzugefuegt werden
    }
}

java.util.List<Integer> intListe    = new java.util.ArrayList<>();
java.util.List<Number>  numListe    = new java.util.ArrayList<>();
java.util.List<Object>  objListe    = new java.util.ArrayList<>();

integersHinzufuegen(intListe);
integersHinzufuegen(numListe);
integersHinzufuegen(objListe);

// EINSCHRAENKUNG: Lesen gibt nur Object zurueck
java.util.List<? super Integer> konsument = numListe;
Object o = konsument.get(0); // Nur Object, nicht Number oder Integer
```

---

## 5. PECS-Prinzip

**PECS** steht fuer **P**roducer **E**xtends, **C**onsumer **S**uper.

| Rolle       | Wildcard              | Operationen             | Beispiel                          |
|-------------|-----------------------|-------------------------|-----------------------------------|
| Producer    | `? extends T`         | Nur Lesen               | Aus der Quelle lesen              |
| Consumer    | `? super T`           | Nur Schreiben           | In das Ziel schreiben             |
| Beides      | Konkrete Typen `T`    | Lesen und Schreiben     | Wenn beide Operationen noetig     |

```java
import java.util.*;

public class PecsBeispiel {

    /**
     * Kopiert alle Elemente von src in dest.
     * src ist Producer (? extends T) – wir LESEN daraus.
     * dest ist Consumer (? super T) – wir SCHREIBEN dahin.
     */
    public static <T> void kopieren(List<? extends T> src,
                                     List<? super T>  dest) {
        for (T element : src) {
            dest.add(element);
        }
    }

    /**
     * Fuegt Zahlen aus einer Number-Liste in eine Integer-Liste ein.
     * Nicht moeglich, weil Integer kein Supertyp von Number ist.
     * 
     * PECS hilft: double summe(List<? extends Number> src)
     */
    public static double summiereAlles(List<? extends Number> zahlen) {
        return zahlen.stream().mapToDouble(Number::doubleValue).sum();
    }

    public static void main(String[] args) {
        List<Integer> quelle = List.of(1, 2, 3, 4, 5);
        List<Number>  ziel   = new ArrayList<>();

        kopieren(quelle, ziel);  // Integer extends Number, Number super Integer
        System.out.println("Kopiert: " + ziel); // [1, 2, 3, 4, 5]

        List<Double>  doubles = List.of(1.5, 2.5);
        System.out.println("Summe: " + summiereAlles(quelle));  // 15.0
        System.out.println("Summe: " + summiereAlles(doubles)); // 4.0
        System.out.println("Summe: " + summiereAlles(ziel));    // 15.0
    }
}
```

### 5.1 java.util.Collections als PECS-Musterbeispiel

```java
// Collections.copy() verwendet exakt PECS:
// public static <T> void copy(List<? super T> dest, List<? extends T> src)

List<Integer> quelle = List.of(1, 2, 3);
List<Number>  ziel   = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0));
Collections.copy(ziel, quelle); // Funktioniert dank PECS
```

---

## 6. Wildcard-Vergleich

```java
public class WildcardVergleich {

    // 1. Konkret – benoetigt exakt List<Number>
    static double methodeMitKonkret(java.util.List<Number> liste) {
        return liste.stream().mapToDouble(Number::doubleValue).sum();
    }

    // 2. Upper-bounded – akzeptiert List<Number>, List<Integer>, List<Double>
    static double methodeUpperBound(java.util.List<? extends Number> liste) {
        return liste.stream().mapToDouble(Number::doubleValue).sum();
    }

    // 3. Generisch – equivalente Loesung zu 2
    static <T extends Number> double methodeGenerisch(java.util.List<T> liste) {
        return liste.stream().mapToDouble(Number::doubleValue).sum();
    }

    public static void main(String[] args) {
        var integers = java.util.List.of(1, 2, 3);
        var doubles  = java.util.List.of(1.5, 2.5, 3.0);

        // methodeMitKonkret(integers); // Compilerfehler: List<Integer> != List<Number>
        System.out.println(methodeUpperBound(integers)); // OK: 6.0
        System.out.println(methodeGenerisch(doubles));   // OK: 7.0
    }
}
```

---

## 7. Type Erasure – Typloeschung

### 7.1 Was ist Type Erasure?

Java-Generics sind ein Compile-Zeit-Feature. Zur Laufzeit existieren keine Typparameter mehr – sie werden durch `Object` (unbegrenzt) oder die Bound-Klasse (beschraenkt) ersetzt.

```java
// Quellcode
public class Behaelter<T> {
    private T wert;
    public T getWert() { return wert; }
}

// Nach Typloeschung (Bytecode-Aequivalent)
public class Behaelter {
    private Object wert;
    public Object getWert() { return wert; }
}

// Mit Bound
public class Vergleichbar<T extends Comparable<T>> {
    public int compare(T a, T b) { return a.compareTo(b); }
}

// Nach Typloeschung
public class Vergleichbar {
    public int compare(Comparable a, Comparable b) { return a.compareTo(b); }
}
```

### 7.2 Konsequenzen der Typloeschung

```java
import java.util.*;

public class TypeErasureKonsequenzen {

    public static void main(String[] args) {

        List<String>  strings  = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        // 1. Laufzeit-Typen sind identisch
        System.out.println(strings.getClass() == integers.getClass()); // true
        System.out.println(strings.getClass().getName()); // java.util.ArrayList

        // 2. instanceof mit generischen Typen nicht moeglich
        List<String> liste = new ArrayList<>();
        System.out.println(liste instanceof ArrayList<?>); // OK
        // System.out.println(liste instanceof ArrayList<String>); // Compilerfehler

        // 3. Kein new T() – Typ unbekannt zur Laufzeit
        // T obj = new T(); // Compilerfehler

        // 4. Kein new T[] – generische Arrays nicht erlaubt
        // T[] array = new T[10]; // Compilerfehler

        // 5. Kein T.class
        // Class<T> c = T.class; // Compilerfehler
    }
}
```

### 7.3 Bridge-Methoden

Der Compiler generiert Bridge-Methoden, um Polymorphismus bei Typparametern zu erhalten.

```java
// Quellcode
interface Vergleichbar<T> {
    int vergleiche(T andere);
}

class ZahlKomparator implements Vergleichbar<Integer> {
    @Override
    public int vergleiche(Integer andere) {
        return Integer.compare(42, andere);
    }
}

// Compiler generiert automatisch eine Bridge-Methode:
// public int vergleiche(Object andere) {
//     return vergleiche((Integer) andere); // Delegiert an typsichere Methode
// }
```

---

## 8. Heap Pollution und @SafeVarargs

### 8.1 Was ist Heap Pollution?

Heap Pollution entsteht, wenn eine Variable eines parametrisierten Typs auf ein Objekt zeigt, das nicht dem erwarteten Typ entspricht.

```java
import java.util.*;

public class HeapPollution {

    // Diese Methode erzeugt Heap Pollution!
    @SuppressWarnings("unchecked")
    static void unsichereMethode() {
        List<String> strings = new ArrayList<>();
        List rawList = strings;  // Raw Type – Warnung
        rawList.add(42);         // Integer in String-Liste!

        // Heap Pollution: strings ist List<String>, enthaelt aber Integer
        String s = strings.get(0); // ClassCastException zur Laufzeit!
    }

    // Varargs + Generics erzeugen Heap Pollution Warnung
    // Unsicher, wenn die Methode auf das vararg-Array schreibt
    @SafeVarargs // Unterdrueckt Warnung – nur wenn WIRKLICH sicher!
    static <T> List<T> listAus(T... elemente) {
        return new ArrayList<>(Arrays.asList(elemente));
        // Sicher: Wir schreiben NICHT ins vararg-Array
    }

    public static void main(String[] args) {
        List<String> liste = listAus("A", "B", "C"); // @SafeVarargs unterdrueckt Warnung
        System.out.println(liste);
    }
}
```

### 8.2 @SafeVarargs Regeln

| Bedingung                                           | @SafeVarargs erlaubt? |
|-----------------------------------------------------|-----------------------|
| Methode ist `final`                                 | Ja                    |
| Methode ist `static`                                | Ja                    |
| Methode ist `private` (Java 9+)                     | Ja                    |
| Konstruktor                                         | Ja                    |
| Schreibt auf das vararg-Array                       | Nein – unsicher!      |
| Gibt das vararg-Array zurueck                       | Nein – unsicher!      |

---

## 9. Reifiable vs. Non-Reifiable Types

| Kategorie       | Beispiele                                   | Beschreibung                                    |
|-----------------|---------------------------------------------|-------------------------------------------------|
| Reifiable       | `int`, `String`, `List`, `int[]`            | Vollstaendige Typinfo zur Laufzeit verfuegbar   |
| Non-Reifiable   | `List<String>`, `List<Integer>`, `T`        | Typinfo durch Erasure verloren                  |

```java
// Reifiable: instanceof und Array-Erstellung moeglich
Object obj = "Hallo";
System.out.println(obj instanceof String);    // true
System.out.println(obj instanceof List);      // false (roher Typ)

// Non-Reifiable: Nicht direkt pruefbar
List<String> strings = new ArrayList<>();
// strings instanceof List<String> -> Compilerfehler
// new List<String>[10]             -> Compilerfehler (Generic Array Creation)
```

---

## 10. Fortgeschrittene Muster

### 10.1 Generischer Repository

```java
import java.util.*;

/** Generisches Repository-Interface. */
public interface Repository<T, ID> {
    T findById(ID id);
    List<T> findAll();
    T speichern(T entity);
    void loeschen(ID id);
}

/** Generische In-Memory-Implementierung. */
public abstract class InMemoryRepository<T, ID> implements Repository<T, ID> {

    protected final Map<ID, T> store = new HashMap<>();

    @Override
    public T findById(ID id) {
        return store.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void loeschen(ID id) {
        store.remove(id);
    }
}

/** Konkrete Implementierung. */
record Produkt(Integer id, String name, double preis) {}

public class ProduktRepository extends InMemoryRepository<Produkt, Integer> {

    @Override
    public Produkt speichern(Produkt p) {
        store.put(p.id(), p);
        return p;
    }

    public List<Produkt> findeGuenstiger(double maxPreis) {
        return store.values().stream()
            .filter(p -> p.preis() <= maxPreis)
            .sorted(Comparator.comparingDouble(Produkt::preis))
            .toList();
    }
}
```

### 10.2 Fluent Builder mit Generics

```java
/** Self-referenzierender generischer Builder (Curiously Recurring Template Pattern). */
public abstract class Builder<T, B extends Builder<T, B>> {

    @SuppressWarnings("unchecked")
    protected B selbst() {
        return (B) this;
    }

    public abstract T build();
}

public class PersonBuilder extends Builder<Person, PersonBuilder> {

    private String name;
    private int    alter;
    private String email;

    public PersonBuilder name(String name) {
        this.name = name;
        return selbst();
    }

    public PersonBuilder alter(int alter) {
        this.alter = alter;
        return selbst();
    }

    public PersonBuilder email(String email) {
        this.email = email;
        return selbst();
    }

    @Override
    public Person build() {
        return new Person(name, alter, email);
    }
}

record Person(String name, int alter, String email) {}

// Verwendung
Person p = new PersonBuilder()
    .name("Anna")
    .alter(30)
    .email("anna@example.com")
    .build();
```

### 10.3 Typtoken – Class<T> uebergeben

```java
import java.util.*;

/** Typtoken-Muster: Class<T> als Parameter uebergeben. */
public class TypedCache {

    private final Map<Class<?>, Object> cache = new HashMap<>();

    public <T> void put(Class<T> typ, T wert) {
        cache.put(typ, wert);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> typ) {
        return typ.cast(cache.get(typ));
    }

    public static void main(String[] args) {
        TypedCache c = new TypedCache();
        c.put(String.class,  "Hallo");
        c.put(Integer.class, 42);
        c.put(Double.class,  3.14);

        String  s = c.get(String.class);  // Kein Cast noetig
        Integer i = c.get(Integer.class); // Kein Cast noetig
        System.out.println(s + " " + i); // Hallo 42
    }
}
```

### 10.4 Generics mit Enum

```java
public enum Richtung { NORD, SUED, OST, WEST }

public class EnumBehaelter<E extends Enum<E>> {

    private final Class<E> enumTyp;
    private final java.util.EnumMap<E, String> daten;

    public EnumBehaelter(Class<E> enumTyp) {
        this.enumTyp = enumTyp;
        this.daten   = new java.util.EnumMap<>(enumTyp);
    }

    public void setzen(E schluessel, String wert) {
        daten.put(schluessel, wert);
    }

    public String holen(E schluessel) {
        return daten.get(schluessel);
    }

    public static void main(String[] args) {
        var container = new EnumBehaelter<>(Richtung.class);
        container.setzen(Richtung.NORD, "Norden");
        container.setzen(Richtung.SUED, "Sueden");
        System.out.println(container.holen(Richtung.NORD)); // Norden
    }
}
```

---

## 11. Vergleich: Generics vs. Wildcards

| Szenario                                         | Empfehlung                        |
|--------------------------------------------------|-----------------------------------|
| Methode gibt Typparameter zurueck                | Generisch `<T>`                   |
| Typparameter tritt mehrfach auf                  | Generisch `<T>`                   |
| Nur lesen (flexible Input-Typen)                 | `? extends T`                     |
| Nur schreiben (flexible Output-Typen)            | `? super T`                       |
| Lesen und Schreiben                              | Konkreter Typ `T`                 |
| Kein Bezug zwischen Typen noetig                 | `?` (unbegrenzt)                  |

---

## 12. Haeufige Fehler mit Generics

```java
// Fehler 1: Raw Types verwenden
List liste = new ArrayList(); // Raw Type – Warnung, unsicher
List<String> typsicher = new ArrayList<>(); // Korrekt

// Fehler 2: @SuppressWarnings unnoetig breit einsetzen
@SuppressWarnings("unchecked")
List<String> falsch = (List<String>) new ArrayList<Object>(); // Heap Pollution!

// Fehler 3: Generics und Arrays mischen
// List<String>[] array = new List<String>[10]; // Compilerfehler
List<String>[] array2 = new List[10]; // Raw-Type-Array – Warnung

// Fehler 4: Statische Felder mit Typparametern
class Fehler<T> {
    // static T wert; // Compilerfehler – statisches Feld darf kein T haben
}

// Fehler 5: Checked Exception mit Generics werfen
// class Wrapper<T extends Exception> {
//     void methode() throws T {} // Problematisch durch Erasure
// }
```

---

## Zusammenfassung

- **Generic Classes**: `class Box<T>` – Typparameter in spitzen Klammern nach dem Klassennamen.
- **Generic Methods**: `<T> void methode(T param)` – Typparameter vor dem Rueckgabetyp.
- **Bounded Type Parameters**: `<T extends Number>` beschraenkt den Typ nach oben.
- **Upper-Bounded Wildcard**: `? extends T` – Lesen erlaubt, Schreiben nicht.
- **Lower-Bounded Wildcard**: `? super T` – Schreiben erlaubt, Lesen nur als Object.
- **PECS**: Producer Extends, Consumer Super – Leitprinzip fuer Wildcards.
- **Type Erasure**: Typparameter werden zur Laufzeit entfernt; reifiable vs. non-reifiable.
- **Bridge Methods**: Compiler-generierte Methoden fuer Polymorphismus nach Erasure.
- **Heap Pollution**: Entsteht durch Raw Types oder unsichere varargs-Generics.
- **@SafeVarargs**: Unterdrueckt Warnung nur wenn die Methode nicht auf das vararg-Array schreibt.
