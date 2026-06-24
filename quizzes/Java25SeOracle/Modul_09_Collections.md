# Modul 9: Collections

## Übersicht

Dieses Modul behandelt das Java Collections Framework – eine der wichtigsten Bibliotheken der Java-Standardbibliothek. Collections ermöglichen das flexible Speichern, Verwalten und Verarbeiten von Gruppen von Objekten. Sie lernen die verschiedenen Implementierungen von Listen, Mengen und Maps kennen und verstehen, wann welche Datenstruktur geeignet ist.

| Thema | Dauer |
|---|---|
| Collection Overview | 35 min |
| List Implementations | 21 min |
| Set Implementations | 15 min |
| Map Implementations | 25 min |
| Iterators and Utility Methods | 17 min |
| Practice 9-1 | 25 min |
| **Skill Check: Collections** | **mind. 80%** |

---

## 1. Collection-Hierarchie und Übersicht

### 1.1 Das Java Collections Framework

Das Java Collections Framework (JCF) wurde in Java 2 (JDK 1.2) eingeführt und bietet eine einheitliche Architektur zur Darstellung und Bearbeitung von Sammlungen von Objekten. Es besteht aus:

- **Interfaces**: Abstrakte Datentypen (Collection, List, Set, Queue, Map)
- **Implementierungen**: Konkrete Klassen (ArrayList, HashSet, HashMap, ...)
- **Algorithmen**: Statische Methoden in der Klasse `Collections`

### 1.2 Interface-Hierarchie

```
java.lang.Iterable
    └── java.util.Collection
            ├── java.util.List
            │       ├── ArrayList
            │       ├── LinkedList
            │       └── Vector (veraltet)
            ├── java.util.Set
            │       ├── HashSet
            │       ├── LinkedHashSet
            │       └── SortedSet
            │               └── TreeSet
            └── java.util.Queue
                    ├── LinkedList
                    └── PriorityQueue

java.util.Map (kein Collection-Subinterface!)
    ├── HashMap
    ├── LinkedHashMap
    └── SortedMap
            └── TreeMap
```

### 1.3 Überblick: Wann welche Collection?

| Anforderung | Empfohlene Klasse |
|---|---|
| Reihenfolge wichtig, häufiger Zugriff per Index | `ArrayList` |
| Häufiges Einfügen/Löschen am Anfang/Ende | `LinkedList` |
| Keine Duplikate, Reihenfolge unwichtig | `HashSet` |
| Keine Duplikate, Einfügereihenfolge erhalten | `LinkedHashSet` |
| Keine Duplikate, sortierte Reihenfolge | `TreeSet` |
| Schlüssel-Wert-Paare, schneller Zugriff | `HashMap` |
| Schlüssel-Wert-Paare, Einfügereihenfolge | `LinkedHashMap` |
| Schlüssel-Wert-Paare, sortierte Schlüssel | `TreeMap` |

### 1.4 Das Collection-Interface

```java
import java.util.*;

public class CollectionInterfaceDemo {
    public static void main(String[] args) {
        // Collection-Interface definiert Grundoperationen
        Collection<String> names = new ArrayList<>();

        // Hinzufügen
        names.add("Alice");
        names.add("Bob");
        names.add("Charlie");

        // Größe
        System.out.println("Anzahl: " + names.size());         // 3

        // Enthält?
        System.out.println("Enthält Bob: " + names.contains("Bob")); // true

        // Entfernen
        names.remove("Bob");
        System.out.println("Nach Entfernen: " + names.size()); // 2

        // Iteration
        for (String name : names) {
            System.out.println(name);
        }

        // Leer?
        System.out.println("Leer: " + names.isEmpty()); // false

        // Alle löschen
        names.clear();
        System.out.println("Nach clear: " + names.isEmpty()); // true
    }
}
```

### 1.5 Generics mit Collections

Collections sind seit Java 5 generisch typisiert. Der Typ-Parameter verhindert ClassCastException zur Laufzeit.

```java
import java.util.*;

public class GenericsDemo {
    public static void main(String[] args) {
        // Ohne Generics (veraltet, unsicher)
        List roheList = new ArrayList();
        roheList.add("Text");
        roheList.add(42); // kein Fehler beim Kompilieren!
        String s = (String) roheList.get(1); // ClassCastException zur Laufzeit!

        // Mit Generics (empfohlen)
        List<String> typisierteList = new ArrayList<>();
        typisierteList.add("Text");
        // typisierteList.add(42); // Compilerfehler!
        String text = typisierteList.get(0); // kein Cast notwendig

        // Wildcards
        List<Integer> zahlen = List.of(1, 2, 3, 4, 5);
        druckeCollection(zahlen);

        List<Double> kommazahlen = List.of(1.1, 2.2, 3.3);
        druckeCollection(kommazahlen);
    }

    // Wildcard: akzeptiert List<Integer>, List<Double>, List<String>, ...
    static void druckeCollection(List<?> liste) {
        for (Object element : liste) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    // Upper bounded wildcard: nur Zahlen und Subtypen
    static double summe(List<? extends Number> zahlen) {
        double sum = 0;
        for (Number n : zahlen) {
            sum += n.doubleValue();
        }
        return sum;
    }
}
```

---

## 2. List-Implementierungen

### 2.1 Das List-Interface

Das `List`-Interface erweitert `Collection` um indexbasierte Operationen. Elemente haben eine definierte Reihenfolge und Duplikate sind erlaubt.

```java
import java.util.*;

public class ListInterfaceDemo {
    public static void main(String[] args) {
        List<String> liste = new ArrayList<>();
        liste.add("Apfel");
        liste.add("Banane");
        liste.add("Kirsche");

        // Indexbasierter Zugriff (nur in List, nicht in Collection)
        System.out.println("Element 0: " + liste.get(0));      // Apfel
        System.out.println("Element 1: " + liste.get(1));      // Banane

        // Einfügen an bestimmter Position
        liste.add(1, "Avocado");
        System.out.println("Nach add(1, ...): " + liste);

        // Ersetzen
        liste.set(0, "Ananas");
        System.out.println("Nach set(0, ...): " + liste);

        // Index suchen
        System.out.println("Index von Banane: " + liste.indexOf("Banane"));

        // Teillist (View, keine Kopie!)
        List<String> teil = liste.subList(1, 3);
        System.out.println("Teilliste: " + teil);

        // Entfernen per Index
        liste.remove(0);
        System.out.println("Nach remove(0): " + liste);
    }
}
```

### 2.2 ArrayList

`ArrayList` ist die häufigst verwendete List-Implementierung. Sie basiert intern auf einem dynamisch wachsenden Array.

```java
import java.util.*;

public class ArrayListDemo {
    public static void main(String[] args) {
        // Erstellen mit initaler Kapazität (optional, für Performance)
        ArrayList<String> stadte = new ArrayList<>(20);

        // Hinzufügen
        stadte.add("Berlin");
        stadte.add("München");
        stadte.add("Hamburg");
        stadte.add("Köln");
        stadte.add("Frankfurt");

        // Iteration mit erweiterter for-Schleife
        System.out.println("Alle Städte:");
        for (String stadt : stadte) {
            System.out.println("  " + stadt);
        }

        // Sortieren
        Collections.sort(stadte);
        System.out.println("Sortiert: " + stadte);

        // Sortieren mit Comparator (Lambda)
        stadte.sort((a, b) -> a.length() - b.length());
        System.out.println("Nach Länge: " + stadte);

        // Binäre Suche (Liste muss vorher sortiert sein)
        Collections.sort(stadte);
        int idx = Collections.binarySearch(stadte, "Hamburg");
        System.out.println("Index Hamburg: " + idx);

        // addAll
        List<String> weitere = Arrays.asList("Dresden", "Leipzig", "Nürnberg");
        stadte.addAll(weitere);
        System.out.println("Nach addAll: " + stadte);

        // removeIf mit Predicate
        stadte.removeIf(s -> s.startsWith("L"));
        System.out.println("Nach removeIf: " + stadte);

        // Konvertierung zu Array
        String[] array = stadte.toArray(new String[0]);
        System.out.println("Array: " + Arrays.toString(array));
    }
}
```

### 2.3 LinkedList

`LinkedList` implementiert sowohl `List` als auch `Deque`. Sie basiert auf einer doppelt verketteten Liste.

```java
import java.util.*;

public class LinkedListDemo {
    public static void main(String[] args) {
        LinkedList<String> aufgaben = new LinkedList<>();

        // Einfügen am Anfang und Ende
        aufgaben.addFirst("Erste Aufgabe");
        aufgaben.addLast("Letzte Aufgabe");
        aufgaben.add("Mittlere Aufgabe");

        System.out.println("Liste: " + aufgaben);

        // Als Stack (LIFO)
        LinkedList<Integer> stack = new LinkedList<>();
        stack.push(1);  // = addFirst
        stack.push(2);
        stack.push(3);
        System.out.println("Stack: " + stack);
        System.out.println("Pop: " + stack.pop());  // = removeFirst: 3

        // Als Queue (FIFO)
        LinkedList<String> queue = new LinkedList<>();
        queue.offer("Erster");   // = addLast
        queue.offer("Zweiter");
        queue.offer("Dritter");
        System.out.println("Queue: " + queue);
        System.out.println("Poll: " + queue.poll()); // = removeFirst: Erster

        // peek - lesen ohne Entfernen
        System.out.println("Peek: " + queue.peek()); // Zweiter
        System.out.println("Queue noch: " + queue);  // [Zweiter, Dritter]

        // getFirst / getLast
        System.out.println("Erstes: " + queue.getFirst());
        System.out.println("Letztes: " + queue.getLast());
    }
}
```

### 2.4 ArrayList vs. LinkedList – Vergleich

| Operation | ArrayList | LinkedList |
|---|---|---|
| `get(index)` | O(1) – direkter Zugriff | O(n) – muss traversieren |
| `add(element)` am Ende | O(1) amortisiert | O(1) |
| `add(index, element)` in der Mitte | O(n) – Elemente verschieben | O(n) – finden + O(1) einfügen |
| `remove(index)` | O(n) – Elemente verschieben | O(n) – finden + O(1) entfernen |
| `remove()` am Anfang | O(n) – alle verschieben | O(1) |
| Speicherverbrauch | weniger (nur Daten) | mehr (Daten + 2 Zeiger pro Node) |
| Sequentielle Iteration | schneller (Cache-freundlich) | langsamer |

```java
import java.util.*;

public class ListPerformanceVergleich {
    public static void main(String[] args) {
        int N = 100_000;

        // ArrayList: schneller bei Random Access
        List<Integer> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            arrayList.add(i);
        }
        // Random Access
        for (int i = 0; i < N; i++) {
            arrayList.get(i);
        }
        long arrayListTime = System.nanoTime() - start;

        // LinkedList: schneller beim Einfügen am Anfang
        LinkedList<Integer> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            linkedList.addFirst(i); // O(1) statt O(n)
        }
        long linkedListTime = System.nanoTime() - start;

        System.out.println("ArrayList (add + get): " + arrayListTime / 1_000_000 + " ms");
        System.out.println("LinkedList (addFirst): " + linkedListTime / 1_000_000 + " ms");
    }
}
```

### 2.5 Unveränderliche Listen (Java 9+)

```java
import java.util.*;

public class UnveraenderlicheListen {
    public static void main(String[] args) {
        // List.of() - unveränderlich, keine null-Werte
        List<String> immutable = List.of("Eins", "Zwei", "Drei");
        System.out.println(immutable);

        // immutable.add("Vier"); // UnsupportedOperationException!

        // List.copyOf() - unveränderliche Kopie
        List<String> veraenderlich = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> kopie = List.copyOf(veraenderlich);
        veraenderlich.add("D");
        System.out.println("Original: " + veraenderlich); // [A, B, C, D]
        System.out.println("Kopie:    " + kopie);         // [A, B, C]

        // Arrays.asList() - fixe Größe, aber Elemente änderbar
        List<String> fixeGroesse = Arrays.asList("X", "Y", "Z");
        fixeGroesse.set(0, "A"); // erlaubt
        // fixeGroesse.add("W"); // UnsupportedOperationException!
    }
}
```

---

## 3. Set-Implementierungen

### 3.1 Das Set-Interface

Ein `Set` ist eine Collection ohne Duplikate. Die `equals()`- und `hashCode()`-Methoden der Elemente bestimmen die Gleichheit.

```java
import java.util.*;

public class SetInterfaceDemo {
    public static void main(String[] args) {
        Set<String> menge = new HashSet<>();

        // Duplikate werden ignoriert
        menge.add("Apfel");
        menge.add("Banane");
        menge.add("Apfel");  // Duplikat!
        menge.add("Kirsche");

        System.out.println("Größe: " + menge.size()); // 3, nicht 4!
        System.out.println("Enthält: " + menge);

        // Mengenoperationen
        Set<String> menge2 = new HashSet<>(Arrays.asList("Banane", "Pflaume", "Mango"));

        // Schnittmenge (intersection)
        Set<String> schnittmenge = new HashSet<>(menge);
        schnittmenge.retainAll(menge2);
        System.out.println("Schnittmenge: " + schnittmenge); // [Banane]

        // Vereinigung (union)
        Set<String> vereinigung = new HashSet<>(menge);
        vereinigung.addAll(menge2);
        System.out.println("Vereinigung: " + vereinigung);

        // Differenz
        Set<String> differenz = new HashSet<>(menge);
        differenz.removeAll(menge2);
        System.out.println("Differenz: " + differenz);
    }
}
```

### 3.2 HashSet

`HashSet` verwendet intern eine `HashMap`. Die Reihenfolge der Elemente ist nicht garantiert und kann sich ändern.

```java
import java.util.*;

public class HashSetDemo {
    public static void main(String[] args) {
        HashSet<String> laender = new HashSet<>();
        laender.add("Deutschland");
        laender.add("Frankreich");
        laender.add("Italien");
        laender.add("Spanien");

        // Reihenfolge ist nicht definiert!
        System.out.println("HashSet: " + laender);

        // Schneller Zugriff O(1) für contains
        System.out.println("Enthält IT: " + laender.contains("Italien"));

        // Eigene Klassen brauchen equals() und hashCode()!
        Set<Person> personen = new HashSet<>();
        personen.add(new Person("Max", 30));
        personen.add(new Person("Max", 30)); // Duplikat nur wenn equals/hashCode korrekt
        personen.add(new Person("Anna", 25));
        System.out.println("Personen: " + personen.size()); // 2 wenn korrekt implementiert
    }
}

class Person {
    String name;
    int alter;

    Person(String name, int alter) {
        this.name = name;
        this.alter = alter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person p = (Person) o;
        return alter == p.alter && Objects.equals(name, p.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, alter);
    }

    @Override
    public String toString() {
        return name + "(" + alter + ")";
    }
}
```

### 3.3 LinkedHashSet

`LinkedHashSet` erhält die Einfügereihenfolge durch eine doppelt verkettete Liste zusätzlich zur Hash-Tabelle.

```java
import java.util.*;

public class LinkedHashSetDemo {
    public static void main(String[] args) {
        // HashSet: Reihenfolge undefiniert
        Set<String> hashSet = new HashSet<>();
        hashSet.add("Drei");
        hashSet.add("Eins");
        hashSet.add("Zwei");
        System.out.println("HashSet:       " + hashSet); // ungeordnet

        // LinkedHashSet: Einfügereihenfolge erhalten
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("Drei");
        linkedHashSet.add("Eins");
        linkedHashSet.add("Zwei");
        System.out.println("LinkedHashSet: " + linkedHashSet); // [Drei, Eins, Zwei]

        // Nützlich für: Duplikat-Entfernung mit Reihenfolge
        List<String> mitDuplikaten = Arrays.asList("a", "b", "a", "c", "b", "d");
        Set<String> ohneDuplikate = new LinkedHashSet<>(mitDuplikaten);
        System.out.println("Ohne Duplikate: " + ohneDuplikate); // [a, b, c, d]
    }
}
```

### 3.4 TreeSet

`TreeSet` implementiert das `SortedSet`-Interface und hält Elemente in natürlicher Ordnung oder nach einem `Comparator`.

```java
import java.util.*;

public class TreeSetDemo {
    public static void main(String[] args) {
        // Natürliche Ordnung (Comparable)
        TreeSet<String> woerter = new TreeSet<>();
        woerter.add("Banane");
        woerter.add("Apfel");
        woerter.add("Kirsche");
        woerter.add("Erdbeere");
        System.out.println("Alphabetisch: " + woerter);

        // SortedSet-Methoden
        System.out.println("Erstes:  " + woerter.first());    // Apfel
        System.out.println("Letztes: " + woerter.last());     // Kirsche
        System.out.println("headSet(K): " + woerter.headSet("Kirsche")); // vor Kirsche
        System.out.println("tailSet(E): " + woerter.tailSet("Erdbeere")); // ab Erdbeere
        System.out.println("subSet:  " + woerter.subSet("B", "K")); // zwischen B und K

        // NavigableSet-Methoden (Java 6+)
        System.out.println("lower(E):  " + woerter.lower("Erdbeere"));   // Banane
        System.out.println("higher(E): " + woerter.higher("Erdbeere"));  // Kirsche
        System.out.println("floor(D):  " + woerter.floor("Dattel"));     // Banane
        System.out.println("ceiling(D):" + woerter.ceiling("Dattel"));   // Erdbeere

        // TreeSet mit eigenem Comparator
        TreeSet<String> nachLaenge = new TreeSet<>(
            Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())
        );
        nachLaenge.add("Banane");
        nachLaenge.add("Apfel");
        nachLaenge.add("Kiwi");
        nachLaenge.add("Fig");
        System.out.println("Nach Länge: " + nachLaenge);
    }
}
```

### 3.5 Set-Vergleich

| Eigenschaft | HashSet | LinkedHashSet | TreeSet |
|---|---|---|---|
| Reihenfolge | keine | Einfügereihenfolge | sortiert (natürlich/Comparator) |
| `add` / `remove` / `contains` | O(1) | O(1) | O(log n) |
| Null-Elemente | 1 erlaubt | 1 erlaubt | nicht erlaubt |
| Implementierungsgrundlage | HashMap | LinkedHashMap | Red-Black-Tree |
| Implementiert | Set | Set | SortedSet, NavigableSet |
| Speicherverbrauch | mittel | mehr | mehr |

---

## 4. Map-Implementierungen

### 4.1 Das Map-Interface

Eine `Map` speichert Schlüssel-Wert-Paare. Schlüssel sind eindeutig (wie Set), Werte können doppelt vorkommen.

```java
import java.util.*;

public class MapInterfaceDemo {
    public static void main(String[] args) {
        Map<String, Integer> preise = new HashMap<>();

        // Einträge hinzufügen
        preise.put("Apfel", 2);
        preise.put("Banane", 1);
        preise.put("Kirsche", 5);

        // Wert abrufen
        System.out.println("Apfelpreis: " + preise.get("Apfel")); // 2

        // Wert mit Default
        System.out.println("Dattelpreis: " + preise.getOrDefault("Dattel", 0)); // 0

        // Enthält Schlüssel?
        System.out.println("Enthält Banane: " + preise.containsKey("Banane")); // true
        System.out.println("Enthält Wert 5: " + preise.containsValue(5)); // true

        // Größe
        System.out.println("Anzahl: " + preise.size()); // 3

        // Alle Schlüssel
        System.out.println("Schlüssel: " + preise.keySet());

        // Alle Werte
        System.out.println("Werte: " + preise.values());

        // Alle Einträge
        for (Map.Entry<String, Integer> eintrag : preise.entrySet()) {
            System.out.println(eintrag.getKey() + " -> " + eintrag.getValue());
        }

        // forEach mit Lambda
        preise.forEach((k, v) -> System.out.println(k + ": " + v + " EUR"));

        // Entfernen
        preise.remove("Banane");

        // Nur wenn Schlüssel und Wert übereinstimmen
        preise.remove("Apfel", 99); // entfernt NICHT (Wert ist 2, nicht 99)
        preise.remove("Apfel", 2);  // entfernt (Wert stimmt überein)
    }
}
```

### 4.2 HashMap

`HashMap` ist die schnellste und am häufigsten verwendete Map-Implementierung. Sie bietet O(1) für `put`, `get` und `remove`.

```java
import java.util.*;

public class HashMapDemo {
    public static void main(String[] args) {
        HashMap<String, List<String>> telefonbuch = new HashMap<>();

        // computeIfAbsent - erstelle Liste wenn noch nicht vorhanden
        telefonbuch.computeIfAbsent("Müller", k -> new ArrayList<>())
                   .add("030-123456");
        telefonbuch.computeIfAbsent("Müller", k -> new ArrayList<>())
                   .add("0160-987654");
        telefonbuch.computeIfAbsent("Schmidt", k -> new ArrayList<>())
                   .add("040-555111");

        System.out.println("Telefonbuch: " + telefonbuch);

        // putIfAbsent - nur wenn Schlüssel noch nicht vorhanden
        Map<String, Integer> zähler = new HashMap<>();
        zähler.put("Apfel", 1);
        zähler.putIfAbsent("Apfel", 99);  // wird ignoriert
        zähler.putIfAbsent("Banane", 1);  // wird hinzugefügt
        System.out.println("Zähler: " + zähler); // {Apfel=1, Banane=1}

        // merge - für Häufigkeitszählung
        String[] woerter = {"Hund", "Katze", "Hund", "Vogel", "Katze", "Hund"};
        Map<String, Integer> haeufigkeit = new HashMap<>();
        for (String wort : woerter) {
            haeufigkeit.merge(wort, 1, Integer::sum);
        }
        System.out.println("Häufigkeit: " + haeufigkeit);

        // compute - Wert aktualisieren
        haeufigkeit.compute("Hund", (k, v) -> v == null ? 1 : v + 10);
        System.out.println("Nach compute: " + haeufigkeit);

        // replaceAll - alle Werte transformieren
        haeufigkeit.replaceAll((k, v) -> v * 2);
        System.out.println("Nach replaceAll (*2): " + haeufigkeit);
    }
}
```

### 4.3 LinkedHashMap

```java
import java.util.*;

public class LinkedHashMapDemo {
    public static void main(String[] args) {
        // Einfügereihenfolge
        LinkedHashMap<String, Double> portfolio = new LinkedHashMap<>();
        portfolio.put("AAPL", 145.3);
        portfolio.put("GOOGL", 2750.0);
        portfolio.put("AMZN", 3300.5);
        portfolio.put("MSFT", 310.2);

        System.out.println("Portfolio (Einfügereihenfolge):");
        portfolio.forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // LRU Cache mit accessOrder=true
        LinkedHashMap<String, String> lruCache = new LinkedHashMap<>(16, 0.75f, true) {
            private static final int MAX_SIZE = 3;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > MAX_SIZE;
            }
        };

        lruCache.put("A", "Wert A");
        lruCache.put("B", "Wert B");
        lruCache.put("C", "Wert C");
        lruCache.get("A"); // A wird "frisch" (zuletzt genutzt)
        lruCache.put("D", "Wert D"); // B wird verdrängt (ältester)
        System.out.println("LRU Cache: " + lruCache.keySet()); // [C, A, D]
    }
}
```

### 4.4 TreeMap

```java
import java.util.*;

public class TreeMapDemo {
    public static void main(String[] args) {
        // Sortierte Schlüssel
        TreeMap<String, Integer> einwohner = new TreeMap<>();
        einwohner.put("München", 1_471_508);
        einwohner.put("Berlin", 3_669_491);
        einwohner.put("Hamburg", 1_853_935);
        einwohner.put("Köln", 1_084_394);
        einwohner.put("Frankfurt", 763_380);

        System.out.println("Alphabetisch:");
        einwohner.forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // NavigableMap-Methoden
        System.out.println("Erster Schlüssel: " + einwohner.firstKey());  // Berlin
        System.out.println("Letzter Schlüssel: " + einwohner.lastKey());  // München

        System.out.println("headMap(K): " + einwohner.headMap("Hamburg")); // vor Hamburg
        System.out.println("tailMap(K): " + einwohner.tailMap("Hamburg")); // ab Hamburg

        System.out.println("lowerKey(H): " + einwohner.lowerKey("Hamburg"));   // Frankfurt
        System.out.println("higherKey(H): " + einwohner.higherKey("Hamburg")); // Köln

        // Nach Einwohnerzahl sortieren (Comparator)
        TreeMap<String, Integer> nachGroesse = new TreeMap<>(
            Comparator.comparingInt((String k) -> einwohner.get(k)).reversed()
        );
        nachGroesse.putAll(einwohner);
        System.out.println("\nNach Größe (absteigend): " + nachGroesse.keySet());
    }
}
```

### 4.5 Map-Vergleich

| Eigenschaft | HashMap | LinkedHashMap | TreeMap |
|---|---|---|---|
| Schlüsselreihenfolge | keine | Einfüge- oder Zugriffsreihenfolge | sortiert |
| `put` / `get` / `remove` | O(1) | O(1) | O(log n) |
| Null-Schlüssel | 1 erlaubt | 1 erlaubt | nicht erlaubt |
| Null-Werte | erlaubt | erlaubt | erlaubt |
| Implementiert | Map | Map | SortedMap, NavigableMap |
| Verwendung | allgemeiner Zweck | LRU-Cache, Reihenfolge | Range-Queries, Navigation |

### 4.6 Unveränderliche Maps (Java 9+)

```java
import java.util.*;

public class UnveraenderlicheMaps {
    public static void main(String[] args) {
        // Map.of() für bis zu 10 Einträge
        Map<String, Integer> kleineMap = Map.of(
            "Eins", 1,
            "Zwei", 2,
            "Drei", 3
        );
        System.out.println("Kleine Map: " + kleineMap);

        // Map.ofEntries() für beliebig viele Einträge
        Map<String, String> hauptstaedte = Map.ofEntries(
            Map.entry("Deutschland", "Berlin"),
            Map.entry("Frankreich", "Paris"),
            Map.entry("Italien", "Rom"),
            Map.entry("Spanien", "Madrid"),
            Map.entry("Polen", "Warschau")
        );
        System.out.println("Hauptstädte: " + hauptstaedte);

        // Map.copyOf()
        Map<String, Integer> veraenderlich = new HashMap<>();
        veraenderlich.put("A", 1);
        Map<String, Integer> unveraenderlicheKopie = Map.copyOf(veraenderlich);
        System.out.println("Kopie: " + unveraenderlicheKopie);
    }
}
```

---

## 5. Iteratoren und Utility-Methoden

### 5.1 Iterator-Pattern

Der `Iterator` ermöglicht das sequenzielle Durchlaufen einer Collection, ohne die interne Struktur zu kennen.

```java
import java.util.*;

public class IteratorDemo {
    public static void main(String[] args) {
        List<String> namen = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie", "Dave"));

        // Iterator verwenden
        Iterator<String> it = namen.iterator();
        while (it.hasNext()) {
            String name = it.next();
            System.out.println("Name: " + name);

            // Sicheres Entfernen während der Iteration
            if (name.startsWith("C")) {
                it.remove(); // ConcurrentModificationException vermeiden!
            }
        }
        System.out.println("Nach Iteration: " + namen);

        // FALSCH: for-each mit Entfernen -> ConcurrentModificationException
        /*
        for (String name : namen) {
            if (name.startsWith("A")) {
                namen.remove(name); // Fehler!
            }
        }
        */

        // Korrekte Alternative: removeIf (Java 8+)
        namen.removeIf(n -> n.startsWith("B"));
        System.out.println("Nach removeIf: " + namen);
    }
}
```

### 5.2 ListIterator

`ListIterator` erlaubt auch Rückwärtsiteration und Modifikation.

```java
import java.util.*;

public class ListIteratorDemo {
    public static void main(String[] args) {
        List<Integer> zahlen = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

        // Rückwärts iterieren
        ListIterator<Integer> lit = zahlen.listIterator(zahlen.size());
        System.out.print("Rückwärts: ");
        while (lit.hasPrevious()) {
            System.out.print(lit.previous() + " ");
        }
        System.out.println();

        // Elemente ersetzen während der Iteration
        ListIterator<Integer> modIt = zahlen.listIterator();
        while (modIt.hasNext()) {
            int wert = modIt.next();
            modIt.set(wert * wert); // Element ersetzen
        }
        System.out.println("Quadriert: " + zahlen); // [1, 4, 9, 16, 25]

        // Einfügen
        ListIterator<Integer> einfuegeIt = zahlen.listIterator();
        while (einfuegeIt.hasNext()) {
            int wert = einfuegeIt.next();
            if (wert > 10) {
                einfuegeIt.add(-1); // nach aktuellem Element einfügen
            }
        }
        System.out.println("Mit -1: " + zahlen);
    }
}
```

### 5.3 Die Collections-Utility-Klasse

`java.util.Collections` bietet statische Hilfsmethoden für Collections.

```java
import java.util.*;

public class CollectionsUtilityDemo {
    public static void main(String[] args) {
        List<Integer> zahlen = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5));

        // Sortieren
        Collections.sort(zahlen);
        System.out.println("Sortiert: " + zahlen);

        // Mit Comparator
        Collections.sort(zahlen, Collections.reverseOrder());
        System.out.println("Absteigend: " + zahlen);

        // Min und Max
        System.out.println("Min: " + Collections.min(zahlen));
        System.out.println("Max: " + Collections.max(zahlen));

        // Häufigkeit
        System.out.println("Häufigkeit von 5: " + Collections.frequency(zahlen, 5));

        // Mischen
        Collections.shuffle(zahlen);
        System.out.println("Gemischt: " + zahlen);

        // Mischen mit Seed (reproduzierbar)
        Collections.shuffle(zahlen, new Random(42));
        System.out.println("Gemischt (Seed 42): " + zahlen);

        // Umkehren
        Collections.reverse(zahlen);
        System.out.println("Umgekehrt: " + zahlen);

        // Drehen
        Collections.sort(zahlen);
        Collections.rotate(zahlen, 2);
        System.out.println("Rotiert um 2: " + zahlen);

        // Füllen
        List<String> gefuellt = new ArrayList<>(Collections.nCopies(5, "X"));
        System.out.println("Gefüllt: " + gefuellt); // [X, X, X, X, X]

        // Binäre Suche (muss sortiert sein!)
        Collections.sort(zahlen);
        int pos = Collections.binarySearch(zahlen, 5);
        System.out.println("Position von 5: " + pos);

        // Disjoint (keine gemeinsamen Elemente?)
        List<Integer> a = Arrays.asList(1, 2, 3);
        List<Integer> b = Arrays.asList(4, 5, 6);
        List<Integer> c = Arrays.asList(3, 4, 5);
        System.out.println("a,b disjoint: " + Collections.disjoint(a, b)); // true
        System.out.println("a,c disjoint: " + Collections.disjoint(a, c)); // false
    }
}
```

### 5.4 Unveränderliche und synchronisierte Wrapper

```java
import java.util.*;

public class WrapperDemo {
    public static void main(String[] args) {
        // Unveränderliche Wrapper (pre-Java 9, vermeiden wenn möglich)
        List<String> original = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> unveraenderlich = Collections.unmodifiableList(original);
        // unveraenderlich.add("D"); // UnsupportedOperationException

        // Achtung: Änderungen am Original sind noch sichtbar!
        original.add("D");
        System.out.println("Unveränderlich: " + unveraenderlich); // [A, B, C, D]

        // Synchronisierte Wrapper (für Thread-Safety)
        List<String> synchronisiert = Collections.synchronizedList(new ArrayList<>());
        synchronisiert.add("Thread-sicher");

        // Einzelelement-Collections
        Set<String> einElement = Collections.singleton("Einziges");
        List<Integer> eineListe = Collections.singletonList(42);
        Map<String, Integer> eineMap = Collections.singletonMap("Key", 1);

        System.out.println("Singleton: " + einElement);

        // Leere Collections
        List<Object> leereList = Collections.emptyList();
        Set<Object> leeresSet = Collections.emptySet();
        Map<Object, Object> leereMap = Collections.emptyMap();
        System.out.println("Leere Liste: " + leereList);
    }
}
```

### 5.5 Java 21: SequencedCollection Interface

Java 21 hat das `SequencedCollection`-Interface eingeführt, das einheitliche Methoden für geordnete Collections bietet.

```java
import java.util.*;

public class SequencedCollectionDemo {
    public static void main(String[] args) {
        // SequencedCollection - neu in Java 21
        // Implementiert von: ArrayList, LinkedList, LinkedHashSet, ArrayDeque, ...

        SequencedCollection<String> seq = new ArrayList<>(
            Arrays.asList("Erst", "Zweit", "Dritt", "Viert")
        );

        // Einheitliche Methoden für erstes/letztes Element
        System.out.println("Erstes: " + seq.getFirst());   // Erst
        System.out.println("Letztes: " + seq.getLast());   // Viert

        // Hinzufügen am Anfang/Ende
        seq.addFirst("Neu-Erst");
        seq.addLast("Neu-Letzt");
        System.out.println("Nach add: " + seq);

        // Entfernen vom Anfang/Ende
        seq.removeFirst();
        seq.removeLast();
        System.out.println("Nach remove: " + seq);

        // Umgekehrte View
        SequencedCollection<String> reversed = seq.reversed();
        System.out.println("Reversed: " + reversed);

        // SequencedSet
        SequencedSet<String> seqSet = new LinkedHashSet<>(
            Arrays.asList("A", "B", "C", "D")
        );
        System.out.println("Set-Erstes: " + seqSet.getFirst());
        System.out.println("Set-Reversed: " + seqSet.reversed());

        // SequencedMap
        SequencedMap<String, Integer> seqMap = new LinkedHashMap<>();
        seqMap.put("Eins", 1);
        seqMap.put("Zwei", 2);
        seqMap.put("Drei", 3);

        System.out.println("Map-Erstes Entry: " + seqMap.firstEntry());
        System.out.println("Map-Letztes Entry: " + seqMap.lastEntry());
        System.out.println("Reversed Map: " + seqMap.reversed());
    }
}
```

### 5.6 Praktisches Beispiel: Warenkorb-System

```java
import java.util.*;
import java.util.stream.*;

public class WarenkorbSystem {
    record Produkt(String name, double preis, String kategorie) {}

    public static void main(String[] args) {
        // Produkt-Katalog (unveränderlich)
        List<Produkt> katalog = List.of(
            new Produkt("Laptop", 999.99, "Elektronik"),
            new Produkt("Maus", 29.99, "Elektronik"),
            new Produkt("Tisch", 249.99, "Möbel"),
            new Produkt("Stuhl", 189.99, "Möbel"),
            new Produkt("Buch", 19.99, "Literatur"),
            new Produkt("Tastatur", 79.99, "Elektronik")
        );

        // Warenkorb mit Anzahl
        Map<Produkt, Integer> warenkorb = new LinkedHashMap<>();
        Produkt laptop = katalog.get(0);
        Produkt maus = katalog.get(1);
        Produkt buch = katalog.get(4);

        warenkorb.put(laptop, 1);
        warenkorb.put(maus, 2);
        warenkorb.put(buch, 3);

        // Gesamtpreis berechnen
        double gesamt = warenkorb.entrySet().stream()
            .mapToDouble(e -> e.getKey().preis() * e.getValue())
            .sum();
        System.out.printf("Gesamtpreis: %.2f EUR%n", gesamt);

        // Produkte nach Kategorie gruppieren
        Map<String, List<Produkt>> nachKategorie = katalog.stream()
            .collect(Collectors.groupingBy(Produkt::kategorie));
        nachKategorie.forEach((kat, prod) ->
            System.out.println(kat + ": " + prod.stream()
                .map(Produkt::name)
                .collect(Collectors.joining(", ")))
        );

        // Teuerste Produkte pro Kategorie
        Map<String, Optional<Produkt>> teuersteProKategorie = katalog.stream()
            .collect(Collectors.groupingBy(
                Produkt::kategorie,
                Collectors.maxBy(Comparator.comparingDouble(Produkt::preis))
            ));
        teuersteProKategorie.forEach((kat, p) ->
            System.out.println("Teuerstes in " + kat + ": " + p.map(Produkt::name).orElse("N/A"))
        );
    }
}
```

---

## 6. Queue- und Deque-Interface

### 6.1 Das Queue-Interface [Anfänger]

Das `Queue`-Interface modelliert eine Warteschlange nach dem FIFO-Prinzip (First In, First Out). Es ergänzt das `Collection`-Interface um spezifische Methoden, die in zwei Varianten existieren: eine wirft eine Exception bei Fehler, die andere gibt einen Sonderwert zurück.

| Operation | Exception-Variante | Sonderwert-Variante |
|---|---|---|
| Einfügen | `add(e)` | `offer(e)` |
| Entnehmen | `remove()` | `poll()` |
| Erstes lesen | `element()` | `peek()` |

`offer()`, `poll()` und `peek()` sind für den Alltagsgebrauch bevorzugt, da sie keinen Stack-Unwinding bei leerem Zustand verursachen.

```java
import java.util.*;

public class QueueInterfaceDemo {
    public static void main(String[] args) {
        // Queue als Druckwarteschlange
        Queue<String> druckWarteschlange = new LinkedList<>();

        // offer() - fügt ein, gibt false zurück wenn nicht möglich (statt Exception)
        druckWarteschlange.offer("Dokument1.pdf");
        druckWarteschlange.offer("Bericht.docx");
        druckWarteschlange.offer("Foto.png");

        System.out.println("Warteschlange: " + druckWarteschlange);

        // peek() - liest erstes Element OHNE Entfernen
        System.out.println("Nächster Druck: " + druckWarteschlange.peek()); // Dokument1.pdf
        System.out.println("Größe noch: " + druckWarteschlange.size());     // 3

        // poll() - entnimmt erstes Element (gibt null zurück wenn leer)
        String gedruckt = druckWarteschlange.poll();
        System.out.println("Gedruckt: " + gedruckt);                        // Dokument1.pdf
        System.out.println("Verbleibend: " + druckWarteschlange);

        // Leere Queue: poll/peek geben null zurück (keine Exception)
        Queue<String> leereQueue = new LinkedList<>();
        System.out.println("poll() leer: " + leereQueue.poll());   // null
        System.out.println("peek() leer: " + leereQueue.peek());   // null
        // leereQueue.remove(); // würde NoSuchElementException werfen!
    }
}
```

### 6.2 Das Deque-Interface und ArrayDeque [Fortgeschritten]

`Deque` (Double Ended Queue, ausgesprochen "deck") ist eine Erweiterung von `Queue`, die das Einfügen und Entnehmen an beiden Enden erlaubt. `ArrayDeque` ist die bevorzugte Implementierung: sie ist schneller als `LinkedList` für Stack- und Queue-Operationen, da sie auf einem Ring-Array basiert und keinen Overhead für Knotenzeiger hat.

`ArrayDeque` sollte `LinkedList` als Stack oder Queue in neuen Implementierungen immer vorziehen werden.

```java
import java.util.*;

public class ArrayDequeDemo {
    public static void main(String[] args) {
        Deque<String> deque = new ArrayDeque<>();

        // --- Einfügen an beiden Enden ---
        deque.offerFirst("Mitte");       // [Mitte]
        deque.offerFirst("Anfang");      // [Anfang, Mitte]
        deque.offerLast("Ende");         // [Anfang, Mitte, Ende]
        System.out.println("Deque: " + deque);

        // --- Lesen ohne Entfernen ---
        System.out.println("peekFirst: " + deque.peekFirst()); // Anfang
        System.out.println("peekLast:  " + deque.peekLast());  // Ende
        System.out.println("Größe noch: " + deque.size());     // 3

        // --- Entnehmen an beiden Enden ---
        String vorne = deque.pollFirst();
        String hinten = deque.pollLast();
        System.out.println("pollFirst: " + vorne);   // Anfang
        System.out.println("pollLast:  " + hinten);  // Ende
        System.out.println("Rest: " + deque);        // [Mitte]

        // --- ArrayDeque als Stack (LIFO) ---
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(10);   // = offerFirst / addFirst
        stack.push(20);
        stack.push(30);
        System.out.println("Stack: " + stack);           // [30, 20, 10]
        System.out.println("peek: " + stack.peek());     // 30
        System.out.println("pop:  " + stack.pop());      // 30, = pollFirst
        System.out.println("Stack nach pop: " + stack);  // [20, 10]

        // --- ArrayDeque als Queue (FIFO) ---
        Deque<String> queue = new ArrayDeque<>();
        queue.offer("Erster");   // = offerLast
        queue.offer("Zweiter");
        queue.offer("Dritter");
        System.out.println("Queue poll: " + queue.poll()); // Erster (FIFO)
    }
}
```

**Deque-Methoden Übersicht:**

| Methode | Beschreibung | Exception-Pendant |
|---|---|---|
| `offerFirst(e)` | Einfügen vorne | `addFirst(e)` |
| `offerLast(e)` | Einfügen hinten | `addLast(e)` |
| `pollFirst()` | Entnehmen vorne (null wenn leer) | `removeFirst()` |
| `pollLast()` | Entnehmen hinten (null wenn leer) | `removeLast()` |
| `peekFirst()` | Lesen vorne (null wenn leer) | `getFirst()` |
| `peekLast()` | Lesen hinten (null wenn leer) | `getLast()` |
| `push(e)` | Stack-Einfügen (= `addFirst`) | — |
| `pop()` | Stack-Entnehmen (= `removeFirst`) | — |

---

## 7. Comparable und Comparator

### 7.1 Das Comparable-Interface [Anfänger]

Das `Comparable<T>`-Interface ermöglicht einer Klasse, ihre eigene natürliche Ordnung (natural ordering) zu definieren. Klassen, die `Comparable` implementieren, können direkt mit `Collections.sort()`, `TreeSet`, `TreeMap` und `Arrays.sort()` verwendet werden – ohne expliziten `Comparator`.

Der `compareTo()`-Vertrag: negativ wenn `this < other`, null wenn gleich, positiv wenn `this > other`.

```java
import java.util.*;

public class ComparableDemo {

    // Klasse implementiert Comparable für natürliche Ordnung
    static class Produkt implements Comparable<Produkt> {
        String name;
        double preis;

        Produkt(String name, double preis) {
            this.name = name;
            this.preis = preis;
        }

        // Natürliche Ordnung: nach Preis aufsteigend
        @Override
        public int compareTo(Produkt andere) {
            // Double.compare() ist die sichere Methode (vermeidet Overflow-Probleme)
            return Double.compare(this.preis, andere.preis);
        }

        @Override
        public String toString() {
            return name + "(" + preis + ")";
        }
    }

    public static void main(String[] args) {
        List<Produkt> produkte = new ArrayList<>();
        produkte.add(new Produkt("Tastatur", 79.99));
        produkte.add(new Produkt("Monitor", 349.00));
        produkte.add(new Produkt("Maus", 29.99));
        produkte.add(new Produkt("Headset", 89.99));

        // Collections.sort() nutzt compareTo() automatisch
        Collections.sort(produkte);
        System.out.println("Nach Preis (natural): " + produkte);

        // TreeSet nutzt ebenfalls compareTo()
        TreeSet<Produkt> sortiert = new TreeSet<>(produkte);
        System.out.println("Günstigstes: " + sortiert.first());
        System.out.println("Teuerstes:   " + sortiert.last());

        // Strings implementieren Comparable (lexikografisch)
        List<String> woerter = new ArrayList<>(Arrays.asList("Banane", "Apfel", "Dattel", "Kirsche"));
        Collections.sort(woerter); // nutzt String.compareTo()
        System.out.println("Alphabetisch: " + woerter);
    }
}
```

### 7.2 Comparable vs. Comparator [Fortgeschritten]

`Comparable` definiert die **natürliche Ordnung** einer Klasse (einmalig, fest in der Klasse). `Comparator` definiert eine **externe, austauschbare Sortierlogik** – ideal wenn mehrere Sortierungen benötigt werden oder die Klasse nicht änderbar ist.

```java
import java.util.*;

public class ComparableVsComparatorDemo {

    record Mitarbeiter(String name, int alter, double gehalt)
            implements Comparable<Mitarbeiter> {

        // Natürliche Ordnung: alphabetisch nach Name
        @Override
        public int compareTo(Mitarbeiter andere) {
            return this.name.compareTo(andere.name);
        }
    }

    public static void main(String[] args) {
        List<Mitarbeiter> team = new ArrayList<>(List.of(
            new Mitarbeiter("Zimmermann", 42, 72000),
            new Mitarbeiter("Braun", 35, 65000),
            new Mitarbeiter("Meyer", 28, 58000),
            new Mitarbeiter("Schulz", 50, 85000)
        ));

        // Comparable: natürliche Ordnung (nach Name)
        Collections.sort(team);
        System.out.println("Natural (nach Name): " + team.stream()
            .map(Mitarbeiter::name).toList());

        // Comparator: alternative Sortierung nach Alter
        team.sort(Comparator.comparingInt(Mitarbeiter::alter));
        System.out.println("Nach Alter:   " + team.stream()
            .map(m -> m.name() + "/" + m.alter()).toList());

        // Comparator: nach Gehalt absteigend, dann Name
        team.sort(Comparator.comparingDouble(Mitarbeiter::gehalt)
                             .reversed()
                             .thenComparing(Mitarbeiter::name));
        System.out.println("Nach Gehalt↓: " + team.stream()
            .map(m -> m.name() + "/" + m.gehalt()).toList());

        // Comparator.naturalOrder() und reverseOrder()
        List<String> namen = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob"));
        namen.sort(Comparator.naturalOrder());
        System.out.println("Natural: " + namen);
        namen.sort(Comparator.reverseOrder());
        System.out.println("Reverse: " + namen);
    }
}
```

---

## 8. Erweiterte Map-Operationen

### 8.1 SequencedMap – vollständige API [Fortgeschritten]

`SequencedMap` (Java 21) erweitert `Map` um Operationen an beiden Enden der Reihenfolge. Zusätzlich zu `firstEntry()`/`lastEntry()`/`reversed()` bietet es `putFirst()`, `putLast()` sowie sequenzierte Views für Keys, Values und Entries.

```java
import java.util.*;

public class SequencedMapDemo {
    public static void main(String[] args) {
        SequencedMap<String, Integer> agenda = new LinkedHashMap<>();
        agenda.put("Mittagessen", 1200);
        agenda.put("Meeting", 1400);
        agenda.put("Review", 1600);

        // putFirst / putLast - Einträge an bestimmter Position einfügen
        agenda.putFirst("Standup", 900);    // vorne einfügen
        agenda.putLast("Retrospektive", 1730); // hinten einfügen
        System.out.println("Agenda: " + agenda);

        // firstEntry / lastEntry
        Map.Entry<String, Integer> ersterTermin = agenda.firstEntry();
        Map.Entry<String, Integer> letzterTermin = agenda.lastEntry();
        System.out.println("Erster Termin: " + ersterTermin.getKey() + " um " + ersterTermin.getValue());
        System.out.println("Letzter Termin: " + letzterTermin.getKey() + " um " + letzterTermin.getValue());

        // sequencedKeySet() - SequencedSet der Schlüssel
        SequencedSet<String> keySet = agenda.sequencedKeySet();
        System.out.println("Erster Key: " + keySet.getFirst());
        System.out.println("Keys reversed: " + keySet.reversed());

        // sequencedValues() - SequencedCollection der Werte
        SequencedCollection<Integer> values = agenda.sequencedValues();
        System.out.println("Erster Wert: " + values.getFirst());
        System.out.println("Letzter Wert: " + values.getLast());

        // sequencedEntrySet() - SequencedSet der Einträge
        SequencedSet<Map.Entry<String, Integer>> entries = agenda.sequencedEntrySet();
        System.out.println("Letzter Entry: " + entries.getLast());

        // reversed() - umgekehrte View der Map
        SequencedMap<String, Integer> reversed = agenda.reversed();
        System.out.println("Reversed erster Key: " + reversed.firstEntry().getKey());
    }
}
```

### 8.2 Map.Entry – Iteration und Mutation [Fortgeschritten]

`Map.Entry<K,V>` repräsentiert einen einzelnen Schlüssel-Wert-Eintrag. Beim Durchlaufen mit `entrySet()` kann `setValue()` den Wert direkt in der Map ändern. `Map.entry()` erzeugt unveränderliche Einträge für Factory-Methoden.

```java
import java.util.*;

public class MapEntryDemo {
    public static void main(String[] args) {
        Map<String, Integer> lagerbestand = new HashMap<>();
        lagerbestand.put("Äpfel", 50);
        lagerbestand.put("Bananen", 30);
        lagerbestand.put("Kirschen", 20);

        // entrySet()-Iteration mit getValue() und getKey()
        System.out.println("Bestand:");
        for (Map.Entry<String, Integer> eintrag : lagerbestand.entrySet()) {
            System.out.println("  " + eintrag.getKey() + ": " + eintrag.getValue() + " Stück");
        }

        // setValue() - Wert direkt über Map.Entry mutieren (ändert die Map!)
        for (Map.Entry<String, Integer> eintrag : lagerbestand.entrySet()) {
            if (eintrag.getValue() < 25) {
                // Nachbestellung: Bestand verdoppeln
                eintrag.setValue(eintrag.getValue() * 2);
            }
        }
        System.out.println("Nach Nachbestellung: " + lagerbestand);

        // Map.entry() - unveränderliches Entry (Java 9+)
        Map.Entry<String, Integer> festEintrag = Map.entry("Datteln", 100);
        System.out.println("Fester Eintrag: " + festEintrag.getKey() + "=" + festEintrag.getValue());
        // festEintrag.setValue(200); // UnsupportedOperationException!

        // Map.ofEntries() mit Map.entry() Factory
        Map<String, String> konfiguration = Map.ofEntries(
            Map.entry("host", "localhost"),
            Map.entry("port", "8080"),
            Map.entry("db", "myapp")
        );
        konfiguration.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.println(e.getKey() + " = " + e.getValue()));
    }
}
```

### 8.3 Collections-Wrapper für Set und Map [Anfänger]

`Collections` bietet unveränderliche und synchronisierte Wrapper nicht nur für `List`, sondern auch für `Set` und `Map`. Diese sind relevant wenn Legacy-Code oder bibliothekenübergreifende APIs keinen Zugriff auf `Set.of()` / `Map.of()` erwarten.

```java
import java.util.*;

public class CollectionsWrapperSetMapDemo {
    public static void main(String[] args) {
        // --- Unveränderliche Wrapper ---
        Set<String> originalSet = new HashSet<>(Arrays.asList("A", "B", "C"));
        Set<String> unveraenderlichSet = Collections.unmodifiableSet(originalSet);
        // unveraenderlichSet.add("D"); // UnsupportedOperationException!
        System.out.println("Unmodifiable Set: " + unveraenderlichSet);

        Map<String, Integer> originalMap = new HashMap<>();
        originalMap.put("x", 1);
        originalMap.put("y", 2);
        Map<String, Integer> unveraenderlichMap = Collections.unmodifiableMap(originalMap);
        // unveraenderlichMap.put("z", 3); // UnsupportedOperationException!
        System.out.println("Unmodifiable Map: " + unveraenderlichMap);

        // --- Synchronisierte Wrapper (Thread-Safety) ---
        Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());
        syncSet.add("Thread-sicher");
        System.out.println("Synchronized Set: " + syncSet);

        Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        syncMap.put("key", 42);
        System.out.println("Synchronized Map: " + syncMap);

        // Hinweis: Iteration über synchronized Collections benötigt externen Lock!
        synchronized (syncSet) {
            for (String element : syncSet) {
                System.out.println("Element: " + element);
            }
        }
    }
}
```

---

## Übungsaufgaben

### Übung 9-1: Deque als Palindrom-Prüfer [Fortgeschritten]

Implementieren Sie einen Palindrom-Prüfer mit `ArrayDeque`, der Zeichen von beiden Enden vergleicht.

```java
import java.util.*;

public class PalindromPruefer {
    public static boolean istPalindrom(String wort) {
        Deque<Character> deque = new ArrayDeque<>();
        for (char c : wort.toLowerCase().toCharArray()) {
            deque.offerLast(c);
        }
        while (deque.size() > 1) {
            if (!deque.pollFirst().equals(deque.pollLast())) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(istPalindrom("Rennfahrer")); // true
        System.out.println(istPalindrom("Java"));       // false
        System.out.println(istPalindrom("Regallager")); // true
    }
}
```

### Übung 9-2: Comparable für Rangliste [Fortgeschritten]

Implementieren Sie eine `Spieler`-Klasse mit natürlicher Ordnung (höchste Punktzahl zuerst).

```java
import java.util.*;

public class RanglisteDemo {

    static class Spieler implements Comparable<Spieler> {
        String name;
        int punkte;

        Spieler(String name, int punkte) {
            this.name = name;
            this.punkte = punkte;
        }

        @Override
        public int compareTo(Spieler anderer) {
            // Absteigende Reihenfolge: höchste Punktzahl zuerst
            return Integer.compare(anderer.punkte, this.punkte);
        }

        @Override
        public String toString() {
            return name + ": " + punkte + " Pkt";
        }
    }

    public static void main(String[] args) {
        List<Spieler> rangliste = new ArrayList<>(List.of(
            new Spieler("Alice", 1200),
            new Spieler("Bob", 950),
            new Spieler("Carol", 1500),
            new Spieler("Dave", 1100)
        ));

        Collections.sort(rangliste); // nutzt compareTo()
        System.out.println("Rangliste:");
        for (int i = 0; i < rangliste.size(); i++) {
            System.out.println((i + 1) + ". " + rangliste.get(i));
        }
        // 1. Carol: 1500 Pkt, 2. Alice: 1200 Pkt, ...
    }
}
```

---

## Zusammenfassung

### Wichtige Punkte

1. **Collection-Hierarchie**: `Collection` (Basis) -> `List`, `Set`, `Queue`; `Map` ist separat
2. **List**: Geordnet, Duplikate erlaubt; `ArrayList` für Random Access, `LinkedList` für häufige Modifikationen
3. **Set**: Keine Duplikate; `HashSet` (schnell), `LinkedHashSet` (Reihenfolge), `TreeSet` (sortiert)
4. **Map**: Schlüssel-Wert; `HashMap` (schnell), `LinkedHashMap` (Reihenfolge), `TreeMap` (sortiert)
5. **Generics**: Immer typisieren! Verhindert `ClassCastException`
6. **Iterator**: Sicheres Entfernen während Iteration; `removeIf()` als Modern-Alternative
7. **Collections-Klasse**: Sortieren, Mischen, Min/Max, Suche, Wrapper
8. **Java 21**: `SequencedCollection` für einheitliche Erst/Letzt-Operationen
9. **Queue/Deque**: `Queue` für FIFO, `Deque` für beidseitige Operationen; `ArrayDeque` bevorzugen
10. **Comparable**: Natürliche Ordnung in der Klasse selbst via `compareTo()`; `Comparator` für externe/alternative Sortierung
11. **SequencedMap**: `putFirst/putLast`, `sequencedKeySet/sequencedValues/sequencedEntrySet` für geordnete Maps
12. **Map.Entry**: `setValue()` mutiert Map direkt; `Map.entry()` erzeugt unveränderliche Einträge

### Entscheidungsbaum

```
Benötige ich Schlüssel-Wert-Paare?
  JA  -> Map: HashMap / LinkedHashMap / TreeMap
  NEIN -> Brauche ich Sortierung?
            JA -> TreeSet (Set) oder sortierte ArrayList (List)
            NEIN -> Brauche ich Einfügereihenfolge?
                      JA -> LinkedHashSet (Set) oder ArrayList (List)
                      NEIN -> HashSet (Set, keine Duplikate) oder ArrayList (List, Duplikate OK)

Brauche ich Stack (LIFO) oder Queue (FIFO)?
  Stack -> ArrayDeque (push/pop/peek)
  Queue -> ArrayDeque (offer/poll/peek) oder LinkedList
  Beides (Deque) -> ArrayDeque mit offerFirst/offerLast/pollFirst/pollLast
```

---

## Multiple-Choice-Fragen

**Frage 1:** Welche Methode des `Queue`-Interface liest das erste Element, ohne es zu entfernen, und gibt `null` zurück wenn die Queue leer ist?

- A) `element()`
- **B) `peek()`** ✓
- C) `getFirst()`
- D) `front()`

**Frage 2:** Was ist der Hauptvorteil von `ArrayDeque` gegenüber `LinkedList` als Stack oder Queue?

- A) `ArrayDeque` erlaubt `null`-Werte, `LinkedList` nicht
- B) `ArrayDeque` implementiert das `List`-Interface
- **C) `ArrayDeque` ist schneller durch bessere Cache-Lokalität und keinen Zeiger-Overhead** ✓
- D) `ArrayDeque` unterstützt mehr Methoden als `LinkedList`

**Frage 3:** Welche `Deque`-Methode fügt ein Element am Anfang ein und gibt `false` zurück (statt Exception) wenn nicht möglich?

- A) `addFirst(e)`
- B) `push(e)`
- **C) `offerFirst(e)`** ✓
- D) `prepend(e)`

**Frage 4:** Was muss eine Klasse tun, um mit `Collections.sort()` ohne expliziten `Comparator` sortierbar zu sein?

- A) Das `Comparator`-Interface implementieren
- B) Eine statische `sort()`-Methode bereitstellen
- **C) Das `Comparable`-Interface implementieren und `compareTo()` überschreiben** ✓
- D) Die Klasse als `record` deklarieren

**Frage 5:** Welchen Wert soll `compareTo(other)` zurückgeben wenn `this` kleiner als `other` ist?

- **A) Einen negativen `int`-Wert** ✓
- B) `0`
- C) Einen positiven `int`-Wert
- D) `false`

**Frage 6:** Wie unterscheidet sich `Comparable` von `Comparator`?

- A) `Comparable` ist für primitive Typen, `Comparator` für Objekte
- B) `Comparable` kann nur aufsteigend sortieren, `Comparator` auch absteigend
- **C) `Comparable` definiert die natürliche Ordnung in der Klasse selbst; `Comparator` ist eine externe, austauschbare Sortierlogik** ✓
- D) `Comparator` ist veraltet – `Comparable` ist der moderne Ersatz

**Frage 7:** Welche Methode von `SequencedMap` fügt einen Eintrag an der letzten Position ein?

- A) `append(k, v)`
- B) `addLast(k, v)`
- **C) `putLast(k, v)`** ✓
- D) `offerLast(k, v)`

**Frage 8:** Was gibt `sequencedKeySet()` einer `SequencedMap` zurück?

- A) Eine `List<K>` mit allen Schlüsseln
- B) Ein `Set<K>` ohne definierte Reihenfolge
- **C) Ein `SequencedSet<K>` mit geordneten Schlüsseln** ✓
- D) Eine `Collection<K>` ohne Index-Zugriff

**Frage 9:** Was bewirkt `eintrag.setValue(newValue)` auf einem `Map.Entry`-Objekt während einer `entrySet()`-Iteration?

- A) Es erzeugt eine ConcurrentModificationException
- B) Es ändert nur die lokale Kopie des Eintrags, nicht die Map
- **C) Es ändert den Wert direkt in der zugrunde liegenden Map** ✓
- D) Es ist verboten und wirft UnsupportedOperationException

**Frage 10:** Wie erzeugt man ein unveränderliches `Set` aus einer bestehenden `HashSet`-Instanz mit der `Collections`-Klasse?

- A) `Collections.immutableSet(original)`
- **B) `Collections.unmodifiableSet(original)`** ✓
- C) `Set.copyOf()` ist die einzige Möglichkeit
- D) `Collections.frozenSet(original)`

**Frage 11:** Welche Aussage über `Collections.synchronizedMap()` ist korrekt?

- A) Die zurückgegebene Map ist vollständig thread-sicher, auch bei Iteration ohne externen Lock
- **B) Einzelne Methodenaufrufe sind synchronisiert, aber Iterationen benötigen einen externen `synchronized`-Block** ✓
- C) Sie erzeugt eine unveränderliche Map
- D) Sie ist äquivalent zu `ConcurrentHashMap`

---

## Skill Check: Collections – Erweiterte Themen

Zur Prüfungsvorbereitung sollten Sie folgende Fähigkeiten nachweisen können:

- [ ] `Queue`-Interface-Methoden benennen und den Unterschied zwischen Exception-Variante (`add/remove/element`) und Sonderwert-Variante (`offer/poll/peek`) erklären
- [ ] `ArrayDeque` als Stack und Queue einsetzen und gegenüber `LinkedList` bevorzugen können
- [ ] `Deque`-spezifische Methoden (`offerFirst`, `offerLast`, `pollFirst`, `pollLast`, `peekFirst`, `peekLast`) korrekt anwenden
- [ ] Das `Comparable`-Interface implementieren und den `compareTo()`-Vertrag (negativ/null/positiv) erklären
- [ ] Den Unterschied zwischen natürlicher Ordnung (`Comparable`) und externer Sortierung (`Comparator`) erläutern
- [ ] `SequencedMap`-Methoden (`putFirst`, `putLast`, `sequencedKeySet`, `sequencedValues`, `sequencedEntrySet`) anwenden
- [ ] `Map.Entry.setValue()` zur Mutation von Map-Werten während der Iteration einsetzen
- [ ] `Collections.unmodifiableSet()`, `Collections.unmodifiableMap()`, `Collections.synchronizedSet()` und `Collections.synchronizedMap()` korrekt verwenden
