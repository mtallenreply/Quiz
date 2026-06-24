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
| Threads auf gemeinsamen Punkt sync. | `CountDownLatch` / `CyclicBarrier`             |
| Zugriffe auf Ressource begrenzen    | `Semaphore`                                    |
| Thread-lokale unveränderliche Daten | `ScopedValue`                                  |
| Parallele Stream-Verarbeitung       | `parallelStream()` / `Stream.parallel()`       |

---

## 13. Synchronisations-Hilfsmittel (java.util.concurrent)

### 13.1 CountDownLatch [Fortgeschritten]

Ein `CountDownLatch` ist ein einmaliger Synchronisations-Zähler: ein oder mehrere Threads warten mit `await()`, bis der Zähler durch `countDown()`-Aufrufe auf null fällt. Das Latch lässt sich nicht zurücksetzen – für wiederholbare Szenarien eignet sich `CyclicBarrier`. Typische Anwendungsfälle sind das Warten auf den Abschluss mehrerer paralleler Initialisierungsschritte oder das koordinierte Starten aller Threads gleichzeitig.

```java
import java.util.concurrent.*;

// Szenario: Haupt-Thread wartet, bis 3 Dienste hochgefahren sind
CountDownLatch bereit = new CountDownLatch(3);

ExecutorService pool = Executors.newFixedThreadPool(3);

for (int i = 1; i <= 3; i++) {
    final int dienstNr = i;
    pool.submit(() -> {
        try {
            Thread.sleep(dienstNr * 300L); // Simuliert Startzeit
            System.out.println("Dienst " + dienstNr + " ist bereit");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bereit.countDown(); // Zähler -1
        }
    });
}

System.out.println("Warte auf alle Dienste...");
bereit.await();             // blockiert bis Zähler == 0
// bereit.await(10, TimeUnit.SECONDS); // mit Timeout
System.out.println("Alle Dienste gestartet – Anwendung läuft");

pool.shutdown();
```

```java
// Muster: Startschuss – alle Threads beginnen gleichzeitig
CountDownLatch startschuss = new CountDownLatch(1);
CountDownLatch fertig = new CountDownLatch(5);

for (int i = 0; i < 5; i++) {
    new Thread(() -> {
        try {
            startschuss.await(); // alle warten auf den Startschuss
            // Aufgabe ausführen
            System.out.println(Thread.currentThread().getName() + " gestartet");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            fertig.countDown();
        }
    }).start();
}

startschuss.countDown(); // alle 5 Threads gleichzeitig loslassen
fertig.await();
System.out.println("Alle Threads beendet");
```

> **Praxishinweis:** `CountDownLatch` ist einmalig verwendbar. Der Zähler kann nicht erhöht oder zurückgesetzt werden. Für Tests eignet er sich gut, um auf asynchrone Events zu warten.

---

### 13.2 CyclicBarrier [Fortgeschritten]

Eine `CyclicBarrier` synchronisiert eine feste Anzahl von Threads an einem gemeinsamen Treffpunkt (Barrier). Sobald alle Teilnehmer `await()` aufgerufen haben, werden sie gleichzeitig freigegeben – und die Barriere wird automatisch zurückgesetzt, sodass sie in Schleifen wiederverwendet werden kann. Optional lässt sich eine Barrier-Aktion definieren, die genau einmal ausgeführt wird, bevor die Threads weiterlaufen.

```java
import java.util.concurrent.*;

// 3 Threads verarbeiten jede Runde gemeinsam
CyclicBarrier barriere = new CyclicBarrier(3, () ->
    System.out.println("--- Alle Threads haben Runde abgeschlossen ---"));

ExecutorService pool = Executors.newFixedThreadPool(3);

for (int t = 0; t < 3; t++) {
    final int threadNr = t;
    pool.submit(() -> {
        try {
            for (int runde = 1; runde <= 3; runde++) {
                // Simuliert Arbeit in dieser Runde
                Thread.sleep((long)(Math.random() * 500));
                System.out.printf("Thread %d: Runde %d abgeschlossen%n",
                    threadNr, runde);

                barriere.await(); // warten bis alle anderen auch fertig sind
            }
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    });
}

pool.shutdown();
pool.awaitTermination(30, TimeUnit.SECONDS);
```

| Merkmal             | `CountDownLatch`          | `CyclicBarrier`                  |
|---------------------|---------------------------|----------------------------------|
| Wiederverwendbar    | Nein                      | Ja (automatisches Reset)         |
| Wartet auf          | Zähler erreicht 0         | Alle Teilnehmer an Barriere      |
| Barrier-Aktion      | Nein                      | Ja (optionaler `Runnable`)       |
| Typischer Einsatz   | Einmaliges Event          | Iterative, phasenweise Arbeit    |

> **Praxishinweis:** Bei `BrokenBarrierException` ist die Barriere dauerhaft defekt (z.B. durch Interrupt oder Timeout eines Teilnehmers). Danach müssen alle Threads neu synchronisiert werden.

---

### 13.3 Semaphore [Fortgeschritten]

Ein `Semaphore` kontrolliert den gleichzeitigen Zugriff auf eine begrenzte Ressource durch einen Zähler von Permits. `acquire()` fordert ein Permit an (blockiert wenn keins verfügbar), `release()` gibt es zurück. Mit einem Permit von 1 verhält sich ein Semaphore wie ein Mutex; mit mehr Permits begrenzt er gleichzeitige Zugriffe auf eine festgelegte Anzahl.

```java
import java.util.concurrent.*;

// Maximal 3 gleichzeitige Datenbankverbindungen
Semaphore verbindungsPool = new Semaphore(3);

ExecutorService pool = Executors.newFixedThreadPool(10);

for (int i = 0; i < 10; i++) {
    final int aufgabeNr = i;
    pool.submit(() -> {
        try {
            System.out.println("Aufgabe " + aufgabeNr + " wartet auf Verbindung...");
            verbindungsPool.acquire();  // blockiert wenn alle 3 belegt
            try {
                System.out.println("Aufgabe " + aufgabeNr + " hat Verbindung");
                Thread.sleep(1000); // Simuliert DB-Abfrage
            } finally {
                verbindungsPool.release(); // Permit zurückgeben
                System.out.println("Aufgabe " + aufgabeNr + " gibt Verbindung frei");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}

pool.shutdown();
pool.awaitTermination(30, TimeUnit.SECONDS);
```

```java
// Weitere nützliche Semaphore-Methoden
Semaphore sem = new Semaphore(5);

sem.acquire(2);             // 2 Permits gleichzeitig anfordern
sem.release(2);             // 2 Permits gleichzeitig freigeben

boolean erworben = sem.tryAcquire();           // sofort, ohne Blockieren
boolean mitTimeout = sem.tryAcquire(1, TimeUnit.SECONDS); // mit Timeout

System.out.println("Verfügbare Permits: " + sem.availablePermits());
System.out.println("Wartende Threads:   " + sem.getQueueLength());

// Fairer Semaphore: Threads werden in FIFO-Reihenfolge bedient
Semaphore fairer = new Semaphore(3, true);
```

> **Praxishinweis:** `release()` sollte immer in einem `finally`-Block stehen. Ein Semaphore mit einem Permit kann als Mutex verwendet werden, anders als `synchronized` ist er aber nicht reentrant.

---

## 14. ScopedValue (Java 21) [Fortgeschritten]

### 14.1 Motivation und Unterschied zu ThreadLocal

`ScopedValue` (eingeführt in Java 21 als Preview, ab Java 25 stabil) ist ein Mechanismus zum unveränderlichen Weitergeben von Kontext-Daten an einen definierten Ausführungsbereich – ohne sie als Parameter durchzureichen. Im Gegensatz zu `ThreadLocal` sind `ScopedValue`-Werte unveränderlich (`final` im Scope), werden automatisch aufgeräumt wenn der Scope endet, und funktionieren korrekt mit Virtual Threads und Structured Concurrency.

```java
import java.lang.ScopedValue;

// ScopedValue als Klassenkonstante deklarieren
public class WebServer {

    // Globale Konstante – kein Wert gesetzt bis Scope geöffnet wird
    static final ScopedValue<String> BENUTZER_ID = ScopedValue.newInstance();
    static final ScopedValue<String> ANFRAGE_ID  = ScopedValue.newInstance();

    void handleAnfrage(String userId, String requestId) {
        // Werte für diesen Scope binden – unveränderlich innerhalb des Scope
        ScopedValue.where(BENUTZER_ID, userId)
                   .where(ANFRAGE_ID, requestId)
                   .run(() -> {
                       // Innerhalb des Scope können alle Methoden den Wert lesen
                       verarbeitungsLogik();
                       datenbankAbruf();
                   });
        // Nach Ende des Scope: Werte nicht mehr zugänglich
    }

    void verarbeitungsLogik() {
        // Kein Parameter nötig – Wert aus Scope lesen
        String userId = BENUTZER_ID.get(); // wirft NoSuchElementException wenn kein Scope
        System.out.println("Verarbeite Anfrage für Benutzer: " + userId);
    }

    void datenbankAbruf() {
        System.out.printf("DB-Abfrage [Anfrage: %s, Benutzer: %s]%n",
            ANFRAGE_ID.get(), BENUTZER_ID.get());
    }
}
```

### 14.2 ScopedValue mit Virtual Threads

```java
import java.lang.ScopedValue;
import java.util.concurrent.*;

static final ScopedValue<String> KONTEXT = ScopedValue.newInstance();

// ScopedValue wird korrekt an erzeugte Virtual Threads vererbt
ScopedValue.where(KONTEXT, "Eltern-Kontext").run(() -> {
    try (var scope = StructuredTaskScope.open()) {
        // Jeder Sub-Task erbt den ScopedValue des Eltern-Scopes
        scope.fork(() -> {
            System.out.println("Kind-Thread sieht: " + KONTEXT.get());
            return null;
        });
        scope.join();
    } catch (Exception e) {
        Thread.currentThread().interrupt();
    }
});
```

| Merkmal           | `ThreadLocal`              | `ScopedValue`                     |
|-------------------|----------------------------|------------------------------------|
| Veränderlichkeit  | Ja (`set()`)               | Nein (unveränderlich im Scope)     |
| Lebensdauer       | Manuell (`remove()`)       | Automatisch (Scope-Ende)           |
| Virtual Threads   | Problematisch (Leak-Risiko)| Nativ unterstützt                  |
| Vererbung         | `InheritableThreadLocal`   | Automatisch in StructuredTaskScope |
| Lesbarkeit        | Implizit global            | Explizit, scope-gebunden           |

> **Praxishinweis:** `ScopedValue.get()` wirft `NoSuchElementException` wenn der Wert nicht im aktuellen Scope gebunden ist. Mit `ScopedValue.orElse(defaultWert)` kann ein Fallback angegeben werden.

---

## 15. Structured Concurrency und StructuredTaskScope (Java 21+) [Professionell]

### 15.1 Motivation

Structured Concurrency (Java 21 Preview, Java 25 stabil) bringt die Struktur von sequentiellem Code in nebenläufige Programme: Subtasks werden innerhalb eines klar definierten Scopes gestartet und enden garantiert vor dem Scope-Ende. Fehler und Abbrüche werden einheitlich propagiert, ohne dass `Future`-Ergebnisse manuell auf Exceptions geprüft werden müssen.

### 15.2 StructuredTaskScope

```java
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.*;

// Beispiel: Preis und Verfügbarkeit parallel abfragen
String bestellungAufgeben(int artikelId) throws InterruptedException {
    try (var scope = StructuredTaskScope.open()) {
        // Subtasks starten
        Subtask<String> preis       = scope.fork(() -> holePdreisVonAPI(artikelId));
        Subtask<Boolean> verfuegbar = scope.fork(() -> pruefeVerfuegbarkeit(artikelId));

        scope.join(); // warten bis alle Subtasks beendet sind

        // Ergebnisse auslesen (keine Exceptions mehr hier)
        return "Preis: %s, Verfügbar: %s".formatted(
            preis.get(), verfuegbar.get());
    }
    // Bei Exception in einem Subtask: andere werden automatisch abgebrochen
}
```

```java
// ShutdownOnFailure: Abbruch aller Tasks bei erstem Fehler
String schnellsteAntwort() throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        Subtask<String> taskA = scope.fork(() -> quelleA());
        Subtask<String> taskB = scope.fork(() -> quelleB());

        scope.join().throwIfFailed(); // wirft erste aufgetretene Exception

        return taskA.get() + " | " + taskB.get();
    }
}

// ShutdownOnSuccess: Abbruch sobald einer erfolgreich ist
String schnellsteQuelle() throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
        scope.fork(() -> quelleA()); // langsam
        scope.fork(() -> quelleB()); // schnell

        return scope.join().result(); // gibt erstes erfolgreiches Ergebnis
    }
}
```

> **Praxishinweis:** `StructuredTaskScope` arbeitet am besten mit Virtual Threads. Beim Verlassen des `try`-Blocks werden alle noch laufenden Subtasks automatisch abgebrochen – kein manuelles `cancel()` nötig.

---

## 16. Parallel Streams [Fortgeschritten]

### 16.1 Grundlagen parallelStream()

Parallel Streams verteilen die Stream-Verarbeitung automatisch auf mehrere Threads des `ForkJoinPool.commonPool()`. Sie eignen sich für CPU-intensive Operationen auf großen Datensätzen, bei denen die Verarbeitungsreihenfolge keine Rolle spielt. Bei kleinen Listen oder I/O-Operationen kann der Overhead der Thread-Koordination die Verarbeitung langsamer machen als ein sequenzieller Stream.

```java
import java.util.List;
import java.util.stream.*;

List<Integer> zahlen = IntStream.rangeClosed(1, 1_000_000)
    .boxed()
    .collect(Collectors.toList());

// Sequenziell
long summeSeq = zahlen.stream()
    .mapToLong(Integer::longValue)
    .sum();

// Parallel – automatisch auf ForkJoinPool.commonPool() aufgeteilt
long summeParallel = zahlen.parallelStream()
    .mapToLong(Integer::longValue)
    .sum();

// Bestehenden Stream parallel schalten
long summeP2 = zahlen.stream()
    .parallel()             // ab hier parallel
    .mapToLong(Integer::longValue)
    .sum();

// Wieder sequenziell schalten
long summeS2 = zahlen.parallelStream()
    .sequential()           // ab hier wieder sequenziell
    .mapToLong(Integer::longValue)
    .sum();
```

### 16.2 Wann lohnen sich Parallel Streams?

```java
import java.util.concurrent.ForkJoinPool;

// Parallele Verarbeitung: teurer, unveränderlicher Vorgang auf großer Liste
List<Double> ergebnisse = IntStream.range(0, 100_000)
    .parallel()
    .mapToObj(i -> Math.sqrt(i) * Math.log(i + 1)) // CPU-intensiv
    .collect(Collectors.toList());

// Eigener ForkJoinPool (um commonPool nicht zu blockieren)
ForkJoinPool eigenerPool = new ForkJoinPool(4);
try {
    List<String> verarbeitet = eigenerPool.submit(() ->
        zahlen.parallelStream()
              .map(n -> "Ergebnis-" + n)
              .collect(Collectors.toList())
    ).get();
} catch (Exception e) {
    Thread.currentThread().interrupt();
} finally {
    eigenerPool.shutdown();
}

// Reihenfolge ist bei parallel NICHT garantiert!
List<Integer> ungeordnet = List.of(3, 1, 4, 1, 5, 9).parallelStream()
    .filter(n -> n > 2)
    .collect(Collectors.toList()); // Reihenfolge unbestimmt

// forEachOrdered erzwingt Reihenfolge (reduziert Parallelität)
zahlen.parallelStream()
    .forEachOrdered(System.out::println); // langsamer, aber geordnet
```

### 16.3 Thread-Sicherheit bei Parallel Streams

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

// FALSCH – ArrayList ist nicht thread-sicher!
List<Integer> unsicher = new ArrayList<>();
IntStream.range(0, 1000)
    .parallel()
    .forEach(unsicher::add); // Race Condition! Ergebnis unvorhersehbar

// RICHTIG – thread-sichere Sammlung verwenden
var sicher = new ConcurrentLinkedQueue<Integer>();
IntStream.range(0, 1000)
    .parallel()
    .forEach(sicher::add); // OK

// BESSER – collect() ist intern thread-sicher
List<Integer> korrekt = IntStream.range(0, 1000)
    .parallel()
    .boxed()
    .collect(Collectors.toList()); // thread-sicher, keine externe Sammlung
```

| Szenario                            | Parallel sinnvoll?  | Begründung                                |
|-------------------------------------|---------------------|-------------------------------------------|
| Liste mit > 10.000 Elementen        | Oft ja              | Overhead amortisiert sich                 |
| CPU-intensive Transformationen      | Ja                  | Mehrere Kerne auslasten                   |
| I/O-Operationen (DB, Netzwerk)      | Nein                | ForkJoinPool ist nicht für I/O ausgelegt  |
| Reihenfolge muss erhalten bleiben   | Mit Vorsicht        | `forEachOrdered` reduziert Vorteil        |
| Wenige Elemente (< 1000)            | Nein                | Overhead überwiegt                        |
| Zustandsbehaftete Lambdas           | Nein                | Thread-Safety muss selbst gewährleistet   |

> **Praxishinweis:** `parallelStream()` verwendet `ForkJoinPool.commonPool()`, der von der gesamten JVM geteilt wird. Für Server-Anwendungen kann das den gemeinsamen Pool blockieren. Ein eigener `ForkJoinPool` oder `Executors.newVirtualThreadPerTaskExecutor()` ist dann vorzuziehen.

---

## 17. Executors.newSingleThreadExecutor – Details [Anfänger]

### 17.1 Funktionsweise

`Executors.newSingleThreadExecutor()` erzeugt einen `ExecutorService` mit genau einem Thread. Alle eingereichten Aufgaben werden sequenziell in der Reihenfolge ihrer Einreichung ausgeführt (FIFO). Fällt der interne Thread durch eine unbehandelte Exception aus, wird automatisch ein neuer Thread erzeugt – die Aufgabenreihenfolge bleibt erhalten. Das macht ihn ideal für sequenzielle Hintergrundverarbeitung ohne manuelle Thread-Verwaltung.

```java
import java.util.concurrent.*;

ExecutorService einzelThread = Executors.newSingleThreadExecutor();

// Alle Aufgaben werden sequenziell ausgeführt – nie gleichzeitig
einzelThread.submit(() -> System.out.println("Aufgabe 1 von Thread: "
    + Thread.currentThread().getName()));
einzelThread.submit(() -> System.out.println("Aufgabe 2 – immer nach Aufgabe 1"));
einzelThread.submit(() -> System.out.println("Aufgabe 3 – immer zuletzt"));

// Gibt Future zurück – Ergebnis abholbar
Future<Integer> ergebnis = einzelThread.submit(() -> {
    Thread.sleep(100);
    return 42;
});
System.out.println("Ergebnis: " + ergebnis.get());

einzelThread.shutdown();
einzelThread.awaitTermination(10, TimeUnit.SECONDS);
```

```java
// Typischer Einsatz: Thread-sichere Protokollierung
ExecutorService logWriter = Executors.newSingleThreadExecutor(r -> {
    Thread t = new Thread(r, "Log-Writer");
    t.setDaemon(true); // JVM wartet nicht auf diesen Thread
    return t;
});

// Mehrere Threads können gleichzeitig Log-Einträge einreichen –
// der einzelne Log-Thread schreibt sie der Reihe nach
logWriter.submit(() -> schreibeInDatei("INFO: Anwendung gestartet"));
logWriter.submit(() -> schreibeInDatei("DEBUG: Verbindung hergestellt"));
```

| Merkmal                     | `newSingleThreadExecutor()` | `newFixedThreadPool(1)`        |
|-----------------------------|------------------------------|--------------------------------|
| Anzahl Threads              | 1                            | 1                              |
| Thread-Ersatz bei Exception | Ja (automatisch)             | Ja                             |
| Pool-Größe änderbar         | Nein (versiegelt)            | Ja (via cast)                  |
| Wrapper-Typ                 | Delegierender Wrapper        | `ThreadPoolExecutor`           |

---

## 18. Übungsaufgaben

### Aufgabe 1: CountDownLatch – Parallelinitialisierung

Schreibe eine Klasse `SystemStart`, die drei Dienste (`DatenbankDienst`, `CacheDienst`, `NetzwerkDienst`) parallel in eigenen Threads initialisiert. Der Haupt-Thread soll erst dann "System bereit" ausgeben, wenn alle drei Dienste hochgefahren sind. Verwende `CountDownLatch`.

**Erwartete Ausgabe (Reihenfolge der Dienste kann variieren):**
```
DatenbankDienst gestartet
CacheDienst gestartet
NetzwerkDienst gestartet
System bereit
```

### Aufgabe 2: Semaphore – Ressourcenpool

Implementiere einen `DruckerPool` mit 2 gleichzeitig nutzbaren Druckern. Zehn Threads wollen gleichzeitig drucken. Mit `Semaphore(2)` soll sichergestellt werden, dass nie mehr als 2 Threads gleichzeitig drucken.

### Aufgabe 3: Parallel Streams – Primzahlen

Berechne alle Primzahlen von 2 bis 1.000.000 mit einem parallelen Stream. Vergleiche die Laufzeit mit einer sequenziellen Variante. Achte darauf, eine thread-sichere Sammlung oder `collect()` für das Ergebnis zu verwenden.

### Aufgabe 4: ScopedValue – Anfrage-Kontext

Simuliere einen Mini-Webserver mit `ScopedValue`. Jede "Anfrage" bindet eine `ANFRAGE_ID` und eine `BENUTZER_ID`. Drei Hilfsmethoden (`Authentifizierung`, `Validierung`, `Protokollierung`) sollen diese Werte lesen, ohne sie als Parameter zu erhalten.

---

## 19. Multiple-Choice-Fragen

**Frage 1:** Welche Aussage zu `CountDownLatch` ist korrekt?

- A) Der Zähler kann nach Erreichen von 0 zurückgesetzt werden
- B) Mehrere Threads können gleichzeitig `countDown()` aufrufen, ohne Synchronisation zu benötigen
- **C) Sobald der Zähler 0 erreicht, werden alle wartenden Threads sofort freigegeben** ✓
- D) `await()` blockiert nur genau einen wartenden Thread

**Frage 2:** Was unterscheidet `CyclicBarrier` von `CountDownLatch`?

- A) `CyclicBarrier` kann nur von einem Thread verwendet werden
- **B) `CyclicBarrier` kann nach jeder Runde wiederverwendet werden, `CountDownLatch` nicht** ✓
- C) `CountDownLatch` unterstützt eine Barrier-Aktion, `CyclicBarrier` nicht
- D) Beide sind funktional identisch

**Frage 3:** Ein `Semaphore` wird mit `new Semaphore(1)` erstellt. Wie verhält er sich?

- A) Er erlaubt unbegrenzte gleichzeitige Zugriffe
- B) Er erlaubt genau 2 gleichzeitige Zugriffe (ein Zähler, ein Slot)
- **C) Er verhält sich wie ein Mutex – nur ein Thread kann gleichzeitig `acquire()` halten** ✓
- D) Er entspricht einem `ReentrantLock` und ist damit reentrant

**Frage 4:** Welche Aussage zu `ScopedValue` im Vergleich zu `ThreadLocal` ist korrekt?

- A) `ScopedValue` erlaubt `set()`-Aufrufe zum Ändern des Werts im selben Scope
- B) `ThreadLocal`-Werte werden automatisch aufgeräumt; bei `ScopedValue` muss `remove()` aufgerufen werden
- **C) `ScopedValue`-Werte sind unveränderlich innerhalb ihres Scopes und werden automatisch aufgeräumt** ✓
- D) `ScopedValue` ist nur für Platform-Threads verwendbar, nicht für Virtual Threads

**Frage 5:** Was gibt `ScopedValue.get()` zurück, wenn kein Wert gebunden wurde?

- A) `null`
- B) Den zuletzt gesetzten Wert aus einem äußeren Scope
- C) Den Standardwert, der bei `newInstance()` angegeben wurde
- **D) Es wird eine `NoSuchElementException` geworfen** ✓

**Frage 6:** Welcher der folgenden Anwendungsfälle ist am besten für `parallelStream()` geeignet?

- A) Schreiben von Datenbankeinträgen mit JDBC
- **B) Berechnung von SHA-256-Hashes für eine Million Strings** ✓
- C) Sequentielles Schreiben von Log-Einträgen in eine Datei
- D) Abrufen von Daten aus 10 REST-APIs

**Frage 7:** Was passiert, wenn in einem `parallelStream()` mit `forEach()` in eine normale `ArrayList` geschrieben wird?

- A) Java sperrt die Liste automatisch (thread-sicher durch JVM)
- B) Es wird eine `ConcurrentModificationException` geworfen
- **C) Race Conditions können auftreten – das Ergebnis ist unvorhersehbar** ✓
- D) Die Parallelisierung wird automatisch deaktiviert

**Frage 8:** Welchen `ForkJoinPool` verwendet `parallelStream()` standardmäßig?

- A) Einen neu erstellten, dedizierten Pool pro Stream-Aufruf
- B) Den `ExecutorService` des aktuellen Threads
- **C) Den `ForkJoinPool.commonPool()`, der JVM-weit geteilt wird** ✓
- D) `Executors.newWorkStealingPool()` mit fixer Thread-Anzahl

**Frage 9:** `Executors.newSingleThreadExecutor()` – was unterscheidet ihn von `Executors.newFixedThreadPool(1)`?

- **A) `newSingleThreadExecutor()` gibt einen versiegelten Wrapper zurück, dessen Pool-Größe nicht geändert werden kann** ✓
- B) `newSingleThreadExecutor()` verwendet Virtual Threads
- C) Bei `newFixedThreadPool(1)` wird ein ausgefallener Thread nicht ersetzt
- D) Es gibt keinen funktionalen Unterschied

**Frage 10:** Was ist der Zweck von `StructuredTaskScope.ShutdownOnFailure`?

- A) Es beendet alle Threads der JVM wenn ein Fehler auftritt
- **B) Es bricht alle noch laufenden Subtasks ab, sobald einer eine Exception wirft** ✓
- C) Es ignoriert Exceptions in Subtasks und gibt `null` zurück
- D) Es startet fehlgeschlagene Subtasks automatisch neu

---

## Skill Check: Ergänzte Themen

Beantworte folgende Fragen, um den Lernfortschritt zu den neuen Themen zu prüfen (Ziel: mind. 80 %):

- [ ] Erkläre den Unterschied zwischen `CountDownLatch` und `CyclicBarrier` und nenne je einen typischen Einsatzfall.
- [ ] Implementiere einen `Semaphore`-basierten Ressourcenpool (z.B. für 3 Datenbankverbindungen).
- [ ] Erkläre, warum `ScopedValue` bei Virtual Threads `ThreadLocal` überlegen ist.
- [ ] Binde einen Wert mit `ScopedValue.where(...).run(...)` und lies ihn in einer Hilfsmethode aus.
- [ ] Beschreibe zwei Szenarien, in denen `parallelStream()` sinnvoll ist, und zwei, in denen es kontraproduktiv ist.
- [ ] Erkläre, warum `collect()` bei parallelen Streams thread-sicher ist, `ArrayList::add` dagegen nicht.
- [ ] Erkläre, was `Executors.newSingleThreadExecutor()` bei einer unbehandelten Exception in einer Aufgabe macht.
- [ ] Beschreibe, wie `StructuredTaskScope.ShutdownOnSuccess` bei redundanten parallelen Anfragen helfen kann.
