# Modul 14: Concurrency and Multithreading

## Übersicht

Dieses Modul behandelt die Grundlagen der nebenläufigen Programmierung in Java: Thread-Erzeugung und -Lebenszyklus, Synchronisation, Thread-sichere Datenstrukturen, den ExecutorService sowie moderne Konzepte wie `CompletableFuture` und Virtual Threads (Java 21).

| Thema                          | Dauer |
|--------------------------------|-------|
| Thread Basics                  | 14 m  |
| Thread Lifecycle               | 20 m  |
| Synchronization                | 11 m  |
| Atomic and Locks               | 17 m  |
| ExecutorService                | 20 m  |
| Callable and Future            | 16 m  |
| CompletableFuture              | 14 m  |
| Virtual Threads                |  5 m  |
| Practice 14-1                  | 23 m  |
| Practice 14-2                  | 31 m  |
| Practice 14-3                  | 33 m  |
| **Skill Check: Concurrency**   | **mind. 80 %** |

---

## 1. Thread-Grundlagen

### 1.1 Was ist ein Thread?

Ein **Thread** (Ausführungsfaden) ist die kleinste Einheit, die vom Betriebssystem-Scheduler selbständig ausgeführt werden kann. Mehrere Threads teilen sich den Heap-Speicher einer JVM-Instanz, besitzen aber je eigenen Stack.

```
JVM-Prozess
├── Heap (gemeinsam für alle Threads)
│   ├── Objekte
│   └── Klassen-Metadaten
└── Threads
    ├── Thread 1: eigener Stack, eigener PC-Register
    ├── Thread 2: eigener Stack, eigener PC-Register
    └── Thread 3: eigener Stack, eigener PC-Register
```

### 1.2 Warum Multithreading?

| Grund                     | Erklärung                                               |
|---------------------------|---------------------------------------------------------|
| Performance               | Mehrere CPU-Kerne gleichzeitig nutzen                   |
| Reaktionsfähigkeit        | UI bleibt bedienbar während Hintergrundaufgaben laufen  |
| Ressourcenauslastung      | Wartezeiten (I/O, Netzwerk) durch andere Aufgaben füllen|
| Modellierung              | Manche Probleme sind von Natur aus nebenläufig          |

### 1.3 Threads erstellen – drei Wege

```java
// Weg 1: Thread-Klasse erweitern
class MeinThread extends Thread {
    private String name;

    MeinThread(String name) { this.name = name; }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.printf("[%s] Schritt %d%n", name, i);
        }
    }
}

// Starten
MeinThread t = new MeinThread("Arbeiter-1");
t.start(); // start() erzeugt neuen Thread und ruft run() auf
// t.run(); // FALSCH: führt run() im aktuellen Thread aus!

// Weg 2: Runnable implementieren (bevorzugt – kein Erbe verschwendet)
class MeineAufgabe implements Runnable {
    @Override
    public void run() {
        System.out.println("Aufgabe läuft in: "
            + Thread.currentThread().getName());
    }
}

Thread t2 = new Thread(new MeineAufgabe());
t2.start();

// Weg 3: Lambda (häufigste moderne Variante)
Thread t3 = new Thread(() -> {
    System.out.println("Lambda-Thread: "
        + Thread.currentThread().getName());
});
t3.start();

// Noch kürzer mit Thread.ofPlatform() (Java 19+)
Thread t4 = Thread.ofPlatform()
    .name("Mein-Thread")
    .start(() -> System.out.println("Gestartet!"));
```

---

## 2. Thread-Lebenszyklus

### 2.1 Die Zustände

```
NEW ──start()──► RUNNABLE ──┬── (läuft gerade) RUNNING
                             │        │
                             │   sleep/wait/blocked
                             │        ▼
                             │    BLOCKED/WAITING/TIMED_WAITING
                             │        │
                             │   notify/interrupt/Timeout
                             │        │
                             └────────┘
                                  │
                              run() beendet
                                  ▼
                             TERMINATED
```

| Zustand            | Beschreibung                                                    |
|--------------------|-----------------------------------------------------------------|
| `NEW`              | Thread erzeugt, `start()` noch nicht aufgerufen                 |
| `RUNNABLE`         | Läuft oder wartet auf CPU-Zuteilung                             |
| `BLOCKED`          | Wartet auf einen Monitor-Lock (synchronized)                    |
| `WAITING`          | Wartet unbegrenzt (`wait()`, `join()`, `park()`)                |
| `TIMED_WAITING`    | Wartet mit Timeout (`sleep()`, `wait(n)`, `join(n)`)            |
| `TERMINATED`       | `run()` wurde beendet (normal oder durch Exception)             |

### 2.2 Wichtige Thread-Methoden

```java
Thread t = new Thread(() -> {
    try {
        System.out.println("Thread startet: " + Thread.currentThread().getName());

        // Thread pausieren (in ms)
        Thread.sleep(2000);

        System.out.println("Thread wacht auf");
    } catch (InterruptedException e) {
        // Thread wurde unterbrochen
        System.out.println("Thread wurde unterbrochen!");
        // Interrupt-Status wiederherstellen
        Thread.currentThread().interrupt();
    }
});

t.setName("Mein-Arbeiter");     // Name setzen (vor start()!)
t.setDaemon(true);              // Daemon-Thread: JVM beendet sich auch wenn dieser läuft
t.setPriority(Thread.MAX_PRIORITY); // Priorität 1-10 (5 = normal)

t.start();

// Im Haupt-Thread warten, bis t fertig ist
t.join();           // unbegrenzt warten
// t.join(5000);    // maximal 5 Sekunden warten

// Thread-Zustand abfragen
System.out.println(t.getState());    // NEW/RUNNABLE/...
System.out.println(t.isAlive());     // läuft noch?
System.out.println(t.isDaemon());    // Daemon?

// Thread sanft beenden (interrupt statt stop()!)
t.interrupt();      // setzt Interrupt-Flag

// Im Thread prüfen:
if (Thread.currentThread().isInterrupted()) {
    System.out.println("Abbruch angefordert");
    return;
}
```

### 2.3 Daemon-Threads vs. Benutzer-Threads

```java
// Benutzer-Thread (Standard): JVM wartet auf Beendigung
Thread benutzer = new Thread(() -> {
    for (int i = 0; i < 10; i++) {
        System.out.println("Benutzer-Thread: " + i);
        try { Thread.sleep(100); } catch (InterruptedException e) { break; }
    }
});

// Daemon-Thread: JVM beendet sich NICHT seinetwegen
Thread daemon = new Thread(() -> {
    while (true) {
        System.out.println("Daemon läuft...");
        try { Thread.sleep(500); } catch (InterruptedException e) { break; }
    }
});
daemon.setDaemon(true); // VOR start() setzen!

benutzer.start();
daemon.start();

// Wenn benutzer fertig ist, beendet JVM sich – daemon wird abgebrochen
```

---

## 3. Synchronisation

### 3.1 Das Race-Condition-Problem

```java
// UNSICHER – Race Condition!
class Zaehler {
    private int wert = 0;

    public void erhoehen() {
        // read-modify-write: NICHT atomar!
        wert++;  // entspricht: wert = wert + 1 (3 Operationen!)
    }

    public int getWert() { return wert; }
}

Zaehler z = new Zaehler();
// 1000 Threads erhöhen gleichzeitig
List<Thread> threads = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    threads.add(new Thread(z::erhoehen));
}
threads.forEach(Thread::start);
threads.forEach(t -> {
    try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
});

System.out.println("Erwartet: 1000, Erhalten: " + z.getWert());
// Ergebnis unvorhersehbar: z.B. 987 oder 993 (nie zuverlässig 1000)
```

### 3.2 synchronized-Methoden

```java
class SichererZaehler {
    private int wert = 0;

    // Methode sperrt auf this
    public synchronized void erhoehen() {
        wert++;
    }

    public synchronized void erniedrigen() {
        wert--;
    }

    // Auch synchronized: liest konsistent
    public synchronized int getWert() {
        return wert;
    }
}

// Statische synchronized Methode: sperrt auf Class-Objekt
class Singleton {
    private static Singleton instanz;

    public static synchronized Singleton getInstanz() {
        if (instanz == null) {
            instanz = new Singleton();
        }
        return instanz;
    }
}
```

### 3.3 synchronized-Blöcke (feingranularer)

```java
class Konto {
    private double kontostand;
    private final Object sperre = new Object(); // dediziertes Lock-Objekt

    public void einzahlen(double betrag) {
        synchronized (sperre) {  // nur diesen Bereich sperren
            if (betrag > 0) {
                kontostand += betrag;
            }
        }
        // Hier kein Lock – anderer Code kann gleichzeitig laufen
        System.out.println("Einzahlung: " + betrag);
    }

    // Explizit auf this sperren
    public void abheben(double betrag) {
        synchronized (this) {
            if (betrag > 0 && kontostand >= betrag) {
                kontostand -= betrag;
            }
        }
    }
}
```

### 3.4 wait() und notify()

```java
class Puffer {
    private String daten = null;

    public synchronized void erzeugen(String wert) throws InterruptedException {
        while (daten != null) {
            wait(); // Warten bis Puffer leer
        }
        daten = wert;
        System.out.println("Erzeugt: " + wert);
        notifyAll(); // Wartende Konsumenten wecken
    }

    public synchronized String verbrauchen() throws InterruptedException {
        while (daten == null) {
            wait(); // Warten bis Puffer gefüllt
        }
        String wert = daten;
        daten = null;
        System.out.println("Verbraucht: " + wert);
        notifyAll(); // Wartende Erzeuger wecken
        return wert;
    }
}
```

### 3.5 volatile

```java
class Stopper {
    // volatile: garantiert Sichtbarkeit über Threads hinweg
    // (kein Caching in Thread-lokalem Register)
    private volatile boolean stopp = false;

    public void stoppen() {
        stopp = true; // Änderung sofort für alle Threads sichtbar
    }

    public void laufen() {
        while (!stopp) { // liest immer aus Hauptspeicher
            // Arbeit erledigen
        }
        System.out.println("Gestoppt!");
    }
}
```

| Merkmal                    | `synchronized`          | `volatile`                          |
|----------------------------|-------------------------|-------------------------------------|
| Atomarität                 | Ja                      | Nur für einfache Lese/Schreib-Ops   |
| Sichtbarkeit               | Ja                      | Ja                                  |
| Gegenseitiger Ausschluss   | Ja                      | Nein                                |
| Performance                | Langsamer (Lock-Overhead)| Schneller                           |
| Einsatz                    | Komplexe kritische Bereiche | Einfache Flags / Status-Felder   |

---

## 4. Atomic-Klassen und ReentrantLock

### 4.1 java.util.concurrent.atomic

```java
import java.util.concurrent.atomic.*;

// AtomicInteger: Thread-sicherer int-Zähler ohne synchronized
AtomicInteger zaehler = new AtomicInteger(0);

zaehler.incrementAndGet();   // ++, gibt neuen Wert zurück
zaehler.getAndIncrement();   // gibt alten Wert, dann ++
zaehler.addAndGet(5);        // += 5, gibt neuen Wert zurück
zaehler.compareAndSet(10, 0); // wenn == 10, setze auf 0 (CAS-Operation)

System.out.println(zaehler.get()); // aktuellen Wert lesen

// Weitere Atomic-Klassen
AtomicLong     longZaehler = new AtomicLong(0L);
AtomicBoolean  flag        = new AtomicBoolean(false);
AtomicReference<String> ref = new AtomicReference<>("initial");

// LongAdder: effizienter als AtomicLong bei hoher Konkurrenz
LongAdder adder = new LongAdder();
adder.increment();
adder.add(5);
System.out.println(adder.sum());

// AtomicIntegerArray: Thread-sicheres int-Array
AtomicIntegerArray arr = new AtomicIntegerArray(10);
arr.set(0, 42);
arr.getAndIncrement(0);
```

### 4.2 ReentrantLock

```java
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

class BankKonto {
    private double kontostand;
    private final ReentrantLock lock = new ReentrantLock(true); // fair=true

    public void einzahlen(double betrag) {
        lock.lock(); // Lock anfordern (blockiert bis frei)
        try {
            kontostand += betrag;
        } finally {
            lock.unlock(); // IMMER im finally freigeben!
        }
    }

    public boolean abheben(double betrag) throws InterruptedException {
        // Mit Timeout: maximal 2 Sekunden warten
        if (lock.tryLock(2, TimeUnit.SECONDS)) {
            try {
                if (kontostand >= betrag) {
                    kontostand -= betrag;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
        System.out.println("Timeout – konnte Lock nicht erwerben");
        return false;
    }

    public double getKontostand() {
        lock.lock();
        try { return kontostand; }
        finally { lock.unlock(); }
    }
}
```

### 4.3 ReadWriteLock

```java
import java.util.concurrent.locks.*;

class ThreadSichereKarte {
    private final Map<String, String> daten = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock leseLock    = rwLock.readLock();
    private final Lock schreibLock = rwLock.writeLock();

    public String lesen(String schluessel) {
        leseLock.lock(); // Mehrere Leser gleichzeitig erlaubt
        try {
            return daten.get(schluessel);
        } finally {
            leseLock.unlock();
        }
    }

    public void schreiben(String schluessel, String wert) {
        schreibLock.lock(); // Exklusiv – kein anderer Leser/Schreiber
        try {
            daten.put(schluessel, wert);
        } finally {
            schreibLock.unlock();
        }
    }
}
```

| Klasse               | Vorteile                                  | Nachteile                         |
|----------------------|-------------------------------------------|-----------------------------------|
| `synchronized`       | Einfach, eingebaut                        | Keine Timeout-Option              |
| `ReentrantLock`      | Timeout, tryLock, fair mode               | Manuelles unlock() notwendig      |
| `AtomicInteger` usw. | Sehr schnell (CAS), kein Lock             | Nur für einzelne Variablen        |
| `ReadWriteLock`      | Viele Leser gleichzeitig                  | Komplexer                         |

---

## 5. ExecutorService

### 5.1 Warum ExecutorService?

Direkte Thread-Erzeugung ist teuer. Der `ExecutorService` verwaltet einen **Thread-Pool**: Threads werden wiederverwendet, Aufgaben in einer Warteschlange gepuffert.

```java
import java.util.concurrent.*;

// Thread-Pools erstellen
ExecutorService fixed    = Executors.newFixedThreadPool(4);      // 4 feste Threads
ExecutorService cached   = Executors.newCachedThreadPool();       // dynamisch wächst/schrumpft
ExecutorService single   = Executors.newSingleThreadExecutor();   // genau 1 Thread
ScheduledExecutorService sched = Executors.newScheduledThreadPool(2); // zeitgesteuert
```

### 5.2 Aufgaben einreichen

```java
ExecutorService pool = Executors.newFixedThreadPool(4);

// Runnable einreichen (kein Rückgabewert)
pool.execute(() -> System.out.println("Aufgabe 1"));
pool.submit(() -> System.out.println("Aufgabe 2")); // gibt Future<?> zurück

// Pool ordentlich beenden
pool.shutdown();          // keine neuen Aufgaben; vorhandene abwarten
pool.awaitTermination(30, TimeUnit.SECONDS); // maximal 30 Sek. warten

// Sofortiger Abbruch (laufende Threads werden interrupted)
// pool.shutdownNow();
```

### 5.3 ScheduledExecutorService

```java
import java.util.concurrent.*;

ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

// Einmalig verzögert ausführen
scheduler.schedule(
    () -> System.out.println("Einmalig nach 3 Sekunden"),
    3, TimeUnit.SECONDS);

// Periodisch: feste Rate (Start-zu-Start)
ScheduledFuture<?> sf1 = scheduler.scheduleAtFixedRate(
    () -> System.out.println("Alle 2 Sekunden"),
    0,   // Initialverzögerung
    2,   // Periode
    TimeUnit.SECONDS);

// Periodisch: feste Verzögerung (Ende-zu-Start)
ScheduledFuture<?> sf2 = scheduler.scheduleWithFixedDelay(
    () -> System.out.println("2 Sek. nach letztem Ende"),
    0, 2, TimeUnit.SECONDS);

// Nach 10 Sekunden stoppen
scheduler.schedule(() -> {
    sf1.cancel(false);
    sf2.cancel(false);
    scheduler.shutdown();
}, 10, TimeUnit.SECONDS);
```

---

## 6. Callable und Future

### 6.1 Callable – Aufgabe mit Rückgabewert

```java
import java.util.concurrent.*;

// Callable<V>: wie Runnable, aber mit Rückgabewert und checked Exception
Callable<Integer> aufgabe = () -> {
    Thread.sleep(1000);
    return 42; // Ergebnis
};

ExecutorService pool = Executors.newCachedThreadPool();

// submit() gibt Future<V> zurück
Future<Integer> future = pool.submit(aufgabe);

System.out.println("Aufgabe läuft...");

// get() blockiert bis Ergebnis vorliegt
Integer ergebnis = future.get();           // blockiert unbegrenzt
// Integer ergebnis = future.get(5, TimeUnit.SECONDS); // mit Timeout

System.out.println("Ergebnis: " + ergebnis);

pool.shutdown();
```

### 6.2 Future-Methoden

```java
Future<String> f = pool.submit(() -> {
    Thread.sleep(500);
    return "Fertig!";
});

f.isDone();     // true wenn abgeschlossen (normal, Exception, cancelled)
f.isCancelled(); // true wenn abgebrochen
f.cancel(true); // Abbrechen (true = interrupt wenn läuft)

try {
    String ergebnis = f.get(); // ExecutionException wenn Callable Exception warf
} catch (ExecutionException e) {
    System.err.println("Aufgabe fehlgeschlagen: " + e.getCause());
} catch (CancellationException e) {
    System.err.println("Aufgabe abgebrochen");
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### 6.3 Mehrere Callables mit invokeAll / invokeAny

```java
import java.util.concurrent.*;
import java.util.List;

ExecutorService pool = Executors.newFixedThreadPool(3);

List<Callable<Integer>> aufgaben = List.of(
    () -> { Thread.sleep(100); return 1; },
    () -> { Thread.sleep(200); return 2; },
    () -> { Thread.sleep(300); return 3; }
);

// invokeAll: Alle ausführen, auf alle warten
List<Future<Integer>> futures = pool.invokeAll(aufgaben);
for (Future<Integer> f : futures) {
    System.out.println("Ergebnis: " + f.get()); // gibt 1, 2, 3
}

// invokeAny: Erste erfolgreiche Aufgabe gewinnt
Integer ersteAntwort = pool.invokeAny(aufgaben);
System.out.println("Schnellstes Ergebnis: " + ersteAntwort); // 1

pool.shutdown();
```

---

## 7. CompletableFuture

`CompletableFuture` (Java 8+) ermöglicht **asynchrone, nicht-blockierende** Programmierung durch Verkettung von Operationen.

### 7.1 Grundlagen

```java
import java.util.concurrent.*;

// Asynchron starten (ForkJoinPool.commonPool())
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
    // Simuliert lange Berechnung
    try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    return "Hallo";
});

// Ergebnis abholen (blockiert)
String ergebnis = cf.get();

// Mit eigenem Thread-Pool
ExecutorService pool = Executors.newFixedThreadPool(4);
CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(
    () -> berechnungAufwändig(), pool);

// Runnable (kein Rückgabewert)
CompletableFuture<Void> cf3 = CompletableFuture.runAsync(
    () -> System.out.println("Fire and Forget"), pool);
```

### 7.2 Transformationen verketten

```java
CompletableFuture<String> pipeline = CompletableFuture
    .supplyAsync(() -> "  Hallo Welt  ")      // starten
    .thenApply(String::trim)                   // transformieren
    .thenApply(String::toUpperCase)            // weitere Transformation
    .thenApply(s -> s + "!")                   // anhängen
    .exceptionally(e -> "Fehler: " + e.getMessage()); // Fehlerfall

System.out.println(pipeline.get()); // HALLO WELT!

// thenAccept: Ergebnis konsumieren (kein Rückgabewert)
CompletableFuture.supplyAsync(() -> berechneDaten())
    .thenAccept(daten -> speichereDaten(daten));

// thenRun: nach Abschluss, ohne Ergebnis
CompletableFuture.supplyAsync(() -> "fertig")
    .thenRun(() -> System.out.println("Alles erledigt"));
```

### 7.3 Kombinieren mehrerer CompletableFutures

```java
CompletableFuture<String> preise  = CompletableFuture.supplyAsync(() -> holePdreise());
CompletableFuture<String> lager   = CompletableFuture.supplyAsync(() -> holeLagerbestand());
CompletableFuture<Integer> rabatt = CompletableFuture.supplyAsync(() -> berechneRabatt());

// thenCombine: zwei Futures kombinieren
CompletableFuture<String> zusammen = preise.thenCombine(
    lager,
    (p, l) -> "Preis: " + p + ", Lager: " + l);

// allOf: Warten bis ALLE fertig
CompletableFuture<Void> alle = CompletableFuture.allOf(preise, lager, rabatt);
alle.get(); // warten

// anyOf: Warten bis EINER fertig
CompletableFuture<Object> erster = CompletableFuture.anyOf(preise, lager, rabatt);
System.out.println("Erster: " + erster.get());

// thenCompose: asynchrone Kette (flatMap)
CompletableFuture<String> kette = CompletableFuture
    .supplyAsync(() -> "Benutzer-ID: 42")
    .thenCompose(id -> CompletableFuture.supplyAsync(() -> "Daten für " + id));
```

### 7.4 Fehlerbehandlung

```java
CompletableFuture<Integer> mitFehler = CompletableFuture
    .supplyAsync(() -> {
        if (Math.random() < 0.5) throw new RuntimeException("Zufälliger Fehler");
        return 42;
    })
    .exceptionally(e -> {
        System.err.println("Fehler abgefangen: " + e.getMessage());
        return -1; // Fallback-Wert
    })
    .handle((wert, fehler) -> {
        // handle: wird IMMER aufgerufen (Erfolg oder Fehler)
        if (fehler != null) return 0;
        return wert * 2;
    });

System.out.println(mitFehler.get());
```

---

## 8. Thread-sichere Datenstrukturen

### 8.1 java.util.concurrent Sammlungen

```java
import java.util.concurrent.*;
import java.util.*;

// ConcurrentHashMap: Thread-sichere HashMap
// (Segment-basiertes Locking – nur betroffener Segment gesperrt)
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("Schlüssel", 1);
map.computeIfAbsent("neu", k -> 42);
map.merge("Schlüssel", 1, Integer::sum); // Thread-sicheres += 1

// CopyOnWriteArrayList: Thread-sichere Liste
// Schreiben erstellt Kopie – gut wenn wenig Schreiben, viel Lesen
CopyOnWriteArrayList<String> liste = new CopyOnWriteArrayList<>();
liste.add("Eintrag");
// Iteration ohne ConcurrentModificationException möglich
for (String s : liste) {
    liste.add("Neu"); // OK! (keine Exception)
}

// BlockingQueue: Producer-Consumer-Muster
BlockingQueue<String> queue = new LinkedBlockingQueue<>(100);
queue.put("Element");          // blockiert wenn voll
String el = queue.take();      // blockiert wenn leer
queue.offer("Element", 1, TimeUnit.SECONDS); // mit Timeout
String el2 = queue.poll(1, TimeUnit.SECONDS); // mit Timeout

// ArrayBlockingQueue: begrenzte Größe, FIFO
BlockingQueue<Integer> abq = new ArrayBlockingQueue<>(50);

// PriorityBlockingQueue: sortierte Warteschlange
BlockingQueue<Integer> pbq = new PriorityBlockingQueue<>();
pbq.add(3); pbq.add(1); pbq.add(2);
System.out.println(pbq.take()); // 1 (kleinster zuerst)
```

### 8.2 Collections.synchronized-Wrapper

```java
import java.util.*;

// Aus normalen Collections thread-sichere machen
List<String>   syncList = Collections.synchronizedList(new ArrayList<>());
Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());

// ACHTUNG: Iteration muss explizit synchronisiert werden!
synchronized (syncList) {
    for (String s : syncList) {
        System.out.println(s);
    }
}

// ConcurrentHashMap ist besser als synchronizedMap:
// Collections.synchronizedMap: globaler Lock auf ganze Map
// ConcurrentHashMap:           feingranulares Locking
```

### 8.3 Vergleich Thread-sichere Sammlungen

| Klasse                    | Basis       | Strategie             | Gut für                           |
|---------------------------|-------------|-----------------------|-----------------------------------|
| `ConcurrentHashMap`       | HashMap     | Segment-Locks         | Häufige Lese- und Schreibzugriffe |
| `CopyOnWriteArrayList`    | ArrayList   | Kopie beim Schreiben  | Viel Lesen, wenig Schreiben       |
| `CopyOnWriteArraySet`     | HashSet     | Kopie beim Schreiben  | Wie oben                          |
| `LinkedBlockingQueue`     | LinkedList  | Lock                  | Producer-Consumer                 |
| `ConcurrentLinkedQueue`   | LinkedList  | Lock-Free (CAS)       | Viele gleichzeitige Zugriffe      |
| `synchronizedList`        | Beliebig    | Globaler Lock         | Einfache Threadsicherheit         |

---

## 9. Virtual Threads (Java 21)

### 9.1 Motivation – Plattform-Threads vs. Virtual Threads

| Merkmal                  | Plattform-Thread (OS-Thread)  | Virtual Thread (JVM-Thread)      |
|--------------------------|-------------------------------|----------------------------------|
| Mapping                  | 1:1 zu OS-Thread              | M:N (viele auf wenige OS-Threads)|
| Speicher pro Thread      | ~1 MB Stack                   | ~Kilobytes (dynamisch)           |
| Maximale Anzahl          | ~10.000                       | Millionen                        |
| Blockierendes I/O        | OS-Thread blockiert           | JVM parkt Virtual Thread         |
| Erstellung               | Teuer (~ms)                   | Günstig (~µs)                    |
| Geeignet für             | CPU-intensive Arbeit          | I/O-intensive Aufgaben           |

### 9.2 Virtual Threads erstellen

```java
// Thread.ofVirtual() (Java 21)
Thread vt = Thread.ofVirtual()
    .name("virtuell-1")
    .start(() -> System.out.println("Virtueller Thread: "
        + Thread.currentThread().isVirtual())); // true

// Mit Runnable
Runnable aufgabe = () -> {
    System.out.println("Thread: " + Thread.currentThread());
    System.out.println("Virtual? " + Thread.currentThread().isVirtual());
};

Thread.ofVirtual().start(aufgabe);

// Über ExecutorService (empfohlen für viele Aufgaben)
try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        int aufgabenNr = i;
        exec.submit(() -> {
            Thread.sleep(Duration.ofMillis(100)); // blockiert nicht OS-Thread!
            return aufgabenNr;
        });
    }
} // automatisches shutdown() + awaitTermination()
```

### 9.3 Virtual Threads und blockierende Operationen

```java
import java.time.Duration;

// Virtual Threads sind ideal für I/O-Aufgaben
try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {

    // 100.000 parallele HTTP-Anfragen (konzeptionell)
    List<Future<String>> futures = new ArrayList<>();
    for (int i = 0; i < 100_000; i++) {
        futures.add(exec.submit(() -> {
            // Simuliert Datenbank-Abfrage
            Thread.sleep(Duration.ofMillis(200));
            return "Ergebnis von Thread " + Thread.currentThread().getName();
        }));
    }

    // Ergebnisse abrufen
    int fertig = 0;
    for (Future<String> f : futures) {
        f.get(); fertig++;
    }
    System.out.println(fertig + " Aufgaben erledigt");
}
// Mit Plattform-Threads würde das System mit 100.000 Threads kollabieren!
```

---

## 10. Vollständiges Praxisbeispiel – Producer-Consumer

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ProducerConsumer {

    record Nachricht(int id, String inhalt) {}

    static class Produzent implements Runnable {
        private final BlockingQueue<Nachricht> queue;
        private final AtomicInteger zaehler;
        private final int anzahl;

        Produzent(BlockingQueue<Nachricht> queue, AtomicInteger zaehler, int anzahl) {
            this.queue = queue; this.zaehler = zaehler; this.anzahl = anzahl;
        }

        @Override
        public void run() {
            for (int i = 0; i < anzahl; i++) {
                Nachricht msg = new Nachricht(zaehler.getAndIncrement(), "Inhalt-" + i);
                try {
                    queue.put(msg);
                    System.out.println("[Produzent " + Thread.currentThread().getName()
                        + "] Gesendet: " + msg.id());
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    static class Konsument implements Runnable {
        private final BlockingQueue<Nachricht> queue;
        private volatile boolean aktiv = true;

        Konsument(BlockingQueue<Nachricht> queue) { this.queue = queue; }

        void stoppen() { aktiv = false; }

        @Override
        public void run() {
            while (aktiv || !queue.isEmpty()) {
                try {
                    Nachricht msg = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        System.out.println("  [Konsument " + Thread.currentThread().getName()
                            + "] Empfangen: " + msg.id() + " – " + msg.inhalt());
                        Thread.sleep(80);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Nachricht> queue = new LinkedBlockingQueue<>(20);
        AtomicInteger zaehler = new AtomicInteger(1);

        ExecutorService pool = Executors.newFixedThreadPool(5);

        // 2 Produzenten
        Konsument k1 = new Konsument(queue);
        Konsument k2 = new Konsument(queue);

        pool.execute(new Produzent(queue, zaehler, 10));
        pool.execute(new Produzent(queue, zaehler, 10));
        pool.execute(k1);
        pool.execute(k2);

        // Produzenten Zeit geben
        Thread.sleep(3000);

        // Konsumenten stoppen
        k1.stoppen();
        k2.stoppen();

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Fertig. Verbleibend in Queue: " + queue.size());
    }
}
```

---

## 11. Häufige Fehler und Best Practices

### 11.1 Typische Fehler

```java
// FEHLER 1: start() statt run() vergessen zu rufen
Thread t = new Thread(() -> System.out.println("Hallo"));
t.run();   // FALSCH: läuft im aktuellen Thread, kein neuer Thread!
t.start(); // RICHTIG

// FEHLER 2: Lock im finally nicht freigeben
ReentrantLock lock = new ReentrantLock();
lock.lock();
// Wenn hier Exception: Lock nie freigegeben! Deadlock!
lock.unlock();

// RICHTIG:
lock.lock();
try {
    // kritischer Bereich
} finally {
    lock.unlock();
}

// FEHLER 3: Nicht-volatile Flag
boolean stopp = false; // JVM darf diesen Wert cachen!
// Thread sieht Änderung evtl. nie

// RICHTIG:
volatile boolean stopp = false;

// FEHLER 4: Deadlock
// Thread 1 hält Lock A, wartet auf B
// Thread 2 hält Lock B, wartet auf A
// Lösung: immer gleiche Reihenfolge beim Lock-Erwerb
```

### 11.2 Best Practices

| Empfehlung                               | Begründung                                        |
|------------------------------------------|---------------------------------------------------|
| ExecutorService statt raw Threads        | Thread-Pool-Verwaltung, bessere Kontrolle          |
| Immutable Objects bevorzugen             | Kein Synchronisationsbedarf                        |
| Scope von synchronized minimieren        | Bessere Performance                                |
| `volatile` für einfache Flags            | Lightweight-Lösung ohne Lock-Overhead              |
| `AtomicXxx` für Zähler                   | CAS-basiert, schneller als synchronized            |
| Virtual Threads für I/O-Aufgaben         | Millionen paralleler Verbindungen möglich           |
| Locks immer im `finally` freigeben       | Verhindert Deadlocks bei Exceptions                |
| Immer `interrupt()` propagieren          | Unterbrechbarkeit der Thread-Kette erhalten         |
| `CompletableFuture` für Async-Pipelines  | Lesbare, verkettbare asynchrone Logik              |

---

## 12. Zusammenfassung

| Aufgabe                             | Empfohlene Lösung                              |
|-------------------------------------|------------------------------------------------|
| Einfachen Thread starten            | `Thread.ofPlatform().start(runnable)`          |
| Thread-Pool für CPU-Aufgaben        | `Executors.newFixedThreadPool(n)`              |
| Thread-Pool für I/O-Aufgaben        | `Executors.newVirtualThreadPerTaskExecutor()`  |
| Aufgabe mit Rückgabewert            | `ExecutorService.submit(Callable)`             |
| Asynchrone Pipeline                 | `CompletableFuture.supplyAsync()`              |
| Thread-sicherer Zähler              | `AtomicInteger`                                |
| Thread-sicheres Map                 | `ConcurrentHashMap`                            |
| Producer-Consumer                   | `BlockingQueue` (z.B. `LinkedBlockingQueue`)   |
| Gegeseitiger Ausschluss             | `synchronized` oder `ReentrantLock`            |
| Sichtbarkeit eines Flags            | `volatile`                                     |
