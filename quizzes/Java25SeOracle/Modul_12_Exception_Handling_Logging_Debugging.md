# Modul 12: Exception Handling, Logging und Debugging

## Übersicht

Dieses Modul behandelt das Exception-Handling als eines der wichtigsten Konzepte für robuste Java-Anwendungen. Sie lernen die Exception-Hierarchie, verschiedene try-catch-Muster, eigene Ausnahmen und das Java Logging Framework kennen. Zusätzlich werden grundlegende Debugging-Techniken besprochen.

| Thema | Dauer |
|---|---|
| Exception Hierarchy | 18 min |
| Checked vs Unchecked | 10 min |
| try-catch-finally | 20 min |
| Custom Exceptions | 18 min |
| try-with-resources | 11 min |
| Java Logging | 16 min |
| Practice 12-1 | 29 min |
| Practice 12-2 | 42 min |
| **Skill Check: Exception Handling** | **mind. 80%** |

---

## 1. Exception-Hierarchie

### 1.1 Die Throwable-Hierarchie

```
java.lang.Throwable
    ├── java.lang.Error                  (nicht fangen! Systemfehler)
    │       ├── OutOfMemoryError
    │       ├── StackOverflowError
    │       ├── VirtualMachineError
    │       └── AssertionError
    │
    └── java.lang.Exception              (sollte behandelt werden)
            ├── IOException              (checked)
            │       ├── FileNotFoundException
            │       └── SocketException
            ├── SQLException             (checked)
            ├── ReflectiveOperationException (checked)
            │
            └── RuntimeException         (unchecked)
                    ├── NullPointerException
                    ├── ArrayIndexOutOfBoundsException
                    ├── ClassCastException
                    ├── ArithmeticException
                    ├── NumberFormatException
                    ├── IllegalArgumentException
                    │       └── IllegalStateException
                    └── UnsupportedOperationException
```

### 1.2 Error vs. Exception

```java
public class HierarchieDemo {
    public static void main(String[] args) {
        // Error - NIEMALS fangen (außer in sehr speziellen Fällen)
        // Diese repräsentieren schwerwiegende Systemprobleme

        // StackOverflowError - rekursion ohne Basisfall
        // unendlicheRekursion(); // würde StackOverflowError werfen

        // OutOfMemoryError
        // byte[] riesig = new byte[Integer.MAX_VALUE]; // würde OOM werfen

        // Exception - sollte behandelt werden
        try {
            int[] array = new int[5];
            array[10] = 42; // ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Gefangen: " + e.getMessage());
        }

        // Wichtige Eigenschaften von Throwable
        try {
            throw new RuntimeException("Testnachricht");
        } catch (RuntimeException e) {
            System.out.println("getMessage():   " + e.getMessage());
            System.out.println("getClass():     " + e.getClass().getSimpleName());
            // getStackTrace() gibt Array von StackTraceElement zurück
            StackTraceElement[] trace = e.getStackTrace();
            System.out.println("Erste Zeile:    " + trace[0]);
        }

        // Throwable kann mit instanceof geprüft werden
        Exception ex = new NullPointerException("Null-Wert");
        System.out.println("ist RuntimeException: " + (ex instanceof RuntimeException));
        System.out.println("ist Exception:        " + (ex instanceof Exception));
        System.out.println("ist Throwable:        " + (ex instanceof Throwable));
    }

    static void unendlicheRekursion() {
        unendlicheRekursion(); // StackOverflowError
    }
}
```

### 1.3 Häufige Exceptions und ihre Ursachen

```java
import java.util.*;

public class HaeufigeExceptions {
    public static void main(String[] args) {
        // 1. NullPointerException
        try {
            String s = null;
            s.length(); // NPE
        } catch (NullPointerException e) {
            System.out.println("NPE: " + e.getMessage()); // Java 14+: hilfreiche Meldung
        }

        // 2. ArrayIndexOutOfBoundsException
        try {
            int[] arr = {1, 2, 3};
            System.out.println(arr[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ArrayIndex: " + e.getMessage());
        }

        // 3. ClassCastException
        try {
            Object obj = "Ich bin ein String";
            Integer zahl = (Integer) obj; // ClassCastException
        } catch (ClassCastException e) {
            System.out.println("ClassCast: " + e.getMessage());
        }

        // 4. NumberFormatException (extends IllegalArgumentException)
        try {
            int n = Integer.parseInt("keine Zahl");
        } catch (NumberFormatException e) {
            System.out.println("NumberFormat: " + e.getMessage());
        }

        // 5. ArithmeticException
        try {
            int ergebnis = 10 / 0; // Division durch null bei int
        } catch (ArithmeticException e) {
            System.out.println("Arithmetic: " + e.getMessage()); // / by zero
        }
        // double durch 0 wirft KEINE Exception: ergibt Infinity oder NaN

        // 6. StackOverflowError
        try {
            throw new StackOverflowError("Simuliert"); // direkt werfen zum Demo
        } catch (StackOverflowError e) {
            System.out.println("StackOverflow: " + e.getMessage());
        }

        // 7. ConcurrentModificationException
        try {
            List<String> liste = new ArrayList<>(Arrays.asList("A", "B", "C"));
            for (String s : liste) {
                if (s.equals("B")) {
                    liste.remove(s); // ConcurrentModificationException!
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("ConcurrentMod: " + e.getClass().getSimpleName());
        }
    }
}
```

---

## 2. Checked vs. Unchecked Exceptions

### 2.1 Unterschied und Compiler-Anforderungen

```java
import java.io.*;

public class CheckedVsUnchecked {
    // Checked Exception: MUSS im Methodensignatur deklariert oder gefangen werden
    static void leseGecheckt(String dateiname) throws IOException {
        // IOException ist checked - muss mit throws deklariert werden
        FileReader reader = new FileReader(dateiname);
        int zeichen = reader.read();
        reader.close();
    }

    // Unchecked Exception: KANN gefangen werden, muss aber nicht
    static int dividiereUnchecked(int a, int b) {
        // ArithmeticException ist unchecked - keine throws-Deklaration nötig
        return a / b;
    }

    // Kombination: Checked weitergeben, Unchecked behandeln
    static String verarbeite(String dateiname, int teiler) throws IOException {
        // IOException wird weitergegeben (checked)
        String inhalt = new String(new FileInputStream(dateiname).readAllBytes());

        try {
            // ArithmeticException wird intern behandelt (unchecked)
            int wert = Integer.parseInt(inhalt.trim());
            return String.valueOf(wert / teiler);
        } catch (ArithmeticException | NumberFormatException e) {
            return "Fehler: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        // Unchecked: kein try-catch zwingend erforderlich
        int ergebnis = dividiereUnchecked(10, 2); // funktioniert
        System.out.println("Ergebnis: " + ergebnis);

        // Checked: MUSS behandelt werden
        try {
            leseGecheckt("existiert_nicht.txt");
        } catch (IOException e) {
            System.out.println("IO Fehler: " + e.getMessage());
        }

        // Alternativ: in aufrufende Methode weitergeben
        // (dann müsste main 'throws IOException' deklarieren)
    }
}
```

### 2.2 Checked vs. Unchecked – Wann was?

| Merkmal | Checked Exception | Unchecked Exception |
|---|---|---|
| Superklasse | `Exception` (nicht RuntimeException) | `RuntimeException` |
| Compiler-Check | Ja – muss behandelt oder deklariert werden | Nein |
| Typische Ursachen | Externe Ressourcen (IO, Netzwerk, DB) | Programmierfehler |
| Beispiele | `IOException`, `SQLException`, `ClassNotFoundException` | `NullPointerException`, `IllegalArgumentException` |
| Recovery möglich? | Ja (Datei nicht gefunden, Netzwerkfehler) | Oft nein (Logikfehler) |
| Empfehlung | Wenn Aufrufer sinnvoll reagieren kann | Wenn Fehler auf Logikfehler hindeutet |

```java
import java.io.*;

public class WannWelcheException {
    // Checked Exception sinnvoll: Aufrufer kann reagieren
    static String ladeKonfiguration(String pfad) throws FileNotFoundException {
        // Aufrufer kann alternative Konfiguration laden
        if (!new java.io.File(pfad).exists()) {
            throw new FileNotFoundException("Konfiguration nicht gefunden: " + pfad);
        }
        return "Konfiguration geladen";
    }

    // Unchecked Exception sinnvoll: Programmierfehler
    static int berechneAlter(int geburtsjahr) {
        if (geburtsjahr < 1900 || geburtsjahr > 2100) {
            throw new IllegalArgumentException(
                "Ungültiges Geburtsjahr: " + geburtsjahr
            );
        }
        return 2025 - geburtsjahr;
    }

    // Unchecked Exception: Voraussetzung nicht erfüllt
    static void prüfeObjekt(Object obj) {
        Objects.requireNonNull(obj, "Objekt darf nicht null sein"); // wirft NPE
        // Objects.requireNonNull ist Standard-Methode für Null-Checks
    }

    public static void main(String[] args) {
        // Checked: Fehlerbehandlung durch Aufrufer
        try {
            String config = ladeKonfiguration("/etc/myapp.conf");
        } catch (FileNotFoundException e) {
            System.out.println("Verwende Standardkonfiguration");
            // Sinnvolle Reaktion!
        }

        // Unchecked: Fehler im Code beheben statt fangen
        try {
            int alter = berechneAlter(1990);
            System.out.println("Alter: " + alter);

            int ungültig = berechneAlter(1800); // Programmierfehler!
        } catch (IllegalArgumentException e) {
            System.out.println("Ungültige Eingabe: " + e.getMessage());
        }
    }
}
```

---

## 3. try-catch-finally

### 3.1 Grundlegende Syntax

```java
public class TryCatchFinally {
    public static void main(String[] args) {
        // Einfaches try-catch
        try {
            int ergebnis = Integer.parseInt("keine Zahl");
        } catch (NumberFormatException e) {
            System.out.println("Ungültige Zahl: " + e.getMessage());
        }

        // try-catch-finally
        System.out.println("\n--- try-catch-finally ---");
        try {
            System.out.println("Im try-Block");
            String s = null;
            s.length(); // wirft NPE
            System.out.println("Wird nie erreicht");
        } catch (NullPointerException e) {
            System.out.println("Im catch-Block: " + e.getClass().getSimpleName());
        } finally {
            System.out.println("Im finally-Block (IMMER ausgeführt)");
        }

        // finally wird IMMER ausgeführt, auch wenn catch Exception wirft
        System.out.println("\n--- finally bei return ---");
        System.out.println("Ergebnis: " + methodeMitReturn());

        // try ohne catch (nur mit finally)
        System.out.println("\n--- try-finally ohne catch ---");
        try {
            System.out.println("Try-Anweisung");
        } finally {
            System.out.println("Finally-Anweisung");
        }
    }

    static String methodeMitReturn() {
        try {
            return "Wert aus try";
        } finally {
            System.out.println("finally vor return");
            // return "Wert aus finally"; // würde try-return überschreiben!
        }
    }
}
```

### 3.2 Multi-Catch und Exception-Reihenfolge

```java
import java.io.*;

public class MultiCatch {
    static void verarbeiteEingabe(String eingabe, String datei) throws IOException {
        try {
            // Verschiedene Exceptions möglich
            int zahl = Integer.parseInt(eingabe);     // NumberFormatException
            int[] array = new int[zahl];
            array[zahl + 1] = 42;                     // ArrayIndexOutOfBoundsException
            FileReader reader = new FileReader(datei); // FileNotFoundException (checked)

        } catch (NumberFormatException e) {
            System.out.println("Keine Zahl: " + eingabe);
            // Nur bei dieser spezifischen Exception
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array-Fehler: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("Datei nicht gefunden: " + datei);
            throw e; // weitergeben an Aufrufer
        }
    }

    // Multi-Catch (Java 7+): mehrere Typen in einem catch
    static void multiCatchDemo(String eingabe) {
        try {
            if (eingabe.equals("null")) throw new NullPointerException("Null!");
            if (eingabe.equals("cast")) {
                Object obj = 42;
                String s = (String) obj; // ClassCastException
            }
            Integer.parseInt(eingabe); // NumberFormatException
        } catch (NullPointerException | ClassCastException | NumberFormatException e) {
            // Wird bei jeder dieser drei Exceptions aufgerufen
            // e ist effektiv final in multi-catch!
            System.out.println("Einer von drei Fehlern: " + e.getClass().getSimpleName());
            // e = new RuntimeException(); // Compilerfehler! e ist final
        }
    }

    // FALSCH: Superklasse vor Subklasse fangen
    static void falscheReihenfolge() {
        try {
            throw new NumberFormatException("Test");
        } catch (IllegalArgumentException e) {
            System.out.println("Superklasse: " + e.getClass().getSimpleName());
        }
        // catch (NumberFormatException e) { // Compilerfehler: unerreichbar!
        //     System.out.println("Subklasse");
        // }
    }

    // RICHTIG: Subklasse vor Superklasse
    static void richtigeReihenfolge(String eingabe) {
        try {
            Integer.parseInt(eingabe);
        } catch (NumberFormatException e) {       // Subklasse zuerst
            System.out.println("NumberFormat: " + e.getMessage());
        } catch (IllegalArgumentException e) {    // Superklasse danach
            System.out.println("IllegalArg: " + e.getMessage());
        } catch (RuntimeException e) {            // noch weiter oben
            System.out.println("Runtime: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        verarbeiteEingabe("keine Zahl", "test.txt");
        multiCatchDemo("text");
        falscheReihenfolge();
        richtigeReihenfolge("abc");
    }
}
```

### 3.3 Exception-Informationen und Stack Trace

```java
public class ExceptionInformationen {
    static void ebene3() {
        throw new RuntimeException("Fehler in Ebene 3");
    }

    static void ebene2() {
        ebene3();
    }

    static void ebene1() {
        ebene2();
    }

    public static void main(String[] args) {
        try {
            ebene1();
        } catch (RuntimeException e) {
            // getMessage(): nur die Fehlermeldung
            System.out.println("Message:    " + e.getMessage());

            // getClass(): der Ausnahme-Typ
            System.out.println("Klasse:     " + e.getClass().getName());

            // getLocalizedMessage(): lokalisierte Meldung (falls überschrieben)
            System.out.println("Localized:  " + e.getLocalizedMessage());

            // toString(): Klasse + Meldung
            System.out.println("toString:   " + e.toString());

            // Stack Trace ausgeben
            System.out.println("\n--- Stack Trace ---");
            e.printStackTrace(); // geht auf System.err

            // Stack Trace als String
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            String stackTrace = sw.toString();
            System.out.println("Stack Trace (erste Zeile): " + stackTrace.lines().findFirst().orElse(""));

            // StackTraceElement analysieren
            System.out.println("\n--- Stack Trace Elemente ---");
            for (StackTraceElement element : e.getStackTrace()) {
                System.out.println("  " + element.getClassName() + "."
                    + element.getMethodName() + " (Line: "
                    + element.getLineNumber() + ")");
            }
        }
    }
}
```

---

## 4. Eigene Exceptions (Custom Exceptions)

### 4.1 Eigene Checked Exception

```java
// Eigene checked Exception (extends Exception)
public class GeschaeftsfehlerException extends Exception {
    private final String fehlerCode;
    private final String kundenId;

    // Konstruktor mit Nachricht
    public GeschaeftsfehlerException(String nachricht) {
        super(nachricht);
        this.fehlerCode = "UNBEKANNT";
        this.kundenId = null;
    }

    // Konstruktor mit Code und Nachricht
    public GeschaeftsfehlerException(String fehlerCode, String nachricht) {
        super(nachricht);
        this.fehlerCode = fehlerCode;
        this.kundenId = null;
    }

    // Vollständiger Konstruktor
    public GeschaeftsfehlerException(String fehlerCode, String nachricht, String kundenId) {
        super(nachricht);
        this.fehlerCode = fehlerCode;
        this.kundenId = kundenId;
    }

    // Konstruktor mit Ursache (Exception Chaining)
    public GeschaeftsfehlerException(String nachricht, Throwable ursache) {
        super(nachricht, ursache);
        this.fehlerCode = "INTERN";
        this.kundenId = null;
    }

    public String getFehlerCode() { return fehlerCode; }
    public String getKundenId() { return kundenId; }

    @Override
    public String toString() {
        return "GeschaeftsfehlerException[" + fehlerCode + "]: "
            + getMessage()
            + (kundenId != null ? " (Kunde: " + kundenId + ")" : "");
    }
}
```

### 4.2 Eigene Unchecked Exception

```java
// Eigene unchecked Exception (extends RuntimeException)
public class ValidierungsFehlerException extends RuntimeException {
    private final String feld;
    private final Object wert;

    public ValidierungsFehlerException(String feld, Object wert, String nachricht) {
        super(String.format("Validierungsfehler für Feld '%s' mit Wert '%s': %s",
            feld, wert, nachricht));
        this.feld = feld;
        this.wert = wert;
    }

    public ValidierungsFehlerException(String feld, Object wert, String nachricht, Throwable cause) {
        super(String.format("Validierungsfehler für Feld '%s': %s", feld, nachricht), cause);
        this.feld = feld;
        this.wert = wert;
    }

    public String getFeld() { return feld; }
    public Object getWert() { return wert; }
}

// Verwendung der eigenen Exceptions
class BenutzerService {
    record Benutzer(String name, String email, int alter) {}

    Benutzer erstelleBenutzer(String name, String email, int alter)
            throws GeschaeftsfehlerException {

        // Validierung (unchecked - Programmierfehler des Aufrufers)
        if (name == null || name.isBlank()) {
            throw new ValidierungsFehlerException("name", name, "darf nicht leer sein");
        }
        if (alter < 0 || alter > 150) {
            throw new ValidierungsFehlerException("alter", alter, "muss zwischen 0 und 150 sein");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidierungsFehlerException("email", email, "ungültige Email-Adresse");
        }

        // Geschäftslogik-Prüfung (checked - Aufrufer muss reagieren)
        if (existiertEmail(email)) {
            throw new GeschaeftsfehlerException("DUPLIZIERT",
                "Email bereits registriert: " + email);
        }

        return new Benutzer(name, email, alter);
    }

    private boolean existiertEmail(String email) {
        return email.equals("vorhandener@test.com"); // Simulation
    }

    public static void main(String[] args) {
        BenutzerService service = new BenutzerService();

        // Test 1: Ungültige Eingabe (unchecked)
        try {
            service.erstelleBenutzer("", "test@test.com", 25);
        } catch (ValidierungsFehlerException e) {
            System.out.println("Validierung: " + e.getMessage());
            System.out.println("Feld: " + e.getFeld());
        } catch (GeschaeftsfehlerException e) {
            System.out.println("Geschäft: " + e);
        }

        // Test 2: Duplizierte Email (checked)
        try {
            service.erstelleBenutzer("Max", "vorhandener@test.com", 30);
        } catch (GeschaeftsfehlerException e) {
            System.out.println("Geschäftsfehler: " + e);
            System.out.println("Code: " + e.getFehlerCode());
        }

        // Test 3: Erfolg
        try {
            var neuer = service.erstelleBenutzer("Alice", "alice@neu.com", 25);
            System.out.println("Erstellt: " + neuer);
        } catch (GeschaeftsfehlerException e) {
            System.out.println("Fehler: " + e);
        }
    }
}
```

### 4.3 Exception Chaining [Fortgeschritten]

Exception Chaining bewahrt die ursprüngliche Fehlerursache, wenn eine Exception in eine andere verpackt wird. Es gibt zwei Wege, die Ursache zu setzen: den `cause`-Konstruktor (bevorzugt, da die Ursache unveränderlich festgelegt wird) und die Methode `initCause()` (für Fälle, in denen kein passender Konstruktor verfügbar ist). Mit `getCause()` lässt sich die gesamte Exception-Kette zur Diagnose traversieren.

```java
import java.sql.*;

public class ExceptionChaining {
    // Exception Chaining: Ursache einer Exception bewahren
    static void datenbankOperation() throws SQLException {
        throw new SQLException("Verbindung abgebrochen", "08006", 1);
    }

    static void benutzerSpeichern(String name) throws GeschaeftsfehlerException {
        try {
            datenbankOperation();
        } catch (SQLException sqlEx) {
            // Methode 1 (bevorzugt): cause über Konstruktor setzen
            throw new GeschaeftsfehlerException(
                "Benutzer konnte nicht gespeichert werden: " + name,
                sqlEx  // WICHTIG: Ursache weitergeben!
            );
        }
    }

    static String dateiLesen(String pfad) {
        try {
            throw new java.io.IOException("Datei nicht lesbar: " + pfad);
        } catch (java.io.IOException e) {
            // Checked -> Unchecked: cause-Konstruktor von RuntimeException
            throw new RuntimeException("Systemfehler beim Lesen", e);
        }
    }

    public static void main(String[] args) {
        // Exception Chain analysieren
        try {
            benutzerSpeichern("Max Mustermann");
        } catch (GeschaeftsfehlerException e) {
            System.out.println("Oberste Exception: " + e.getMessage());

            // getCause() gibt die Ursache zurück
            Throwable ursache = e.getCause();
            if (ursache != null) {
                System.out.println("Verursacht durch: " + ursache.getClass().getSimpleName());
                System.out.println("Ursache: " + ursache.getMessage());
            }

            // Vollständige Chain traversieren
            System.out.println("\n--- Exception Chain ---");
            Throwable current = e;
            int tiefe = 0;
            while (current != null) {
                System.out.println("  ".repeat(tiefe) + current.getClass().getSimpleName()
                    + ": " + current.getMessage());
                current = current.getCause();
                tiefe++;
            }
        }

        // RuntimeException-Wrapping
        try {
            dateiLesen("/etc/geheim.conf");
        } catch (RuntimeException e) {
            System.out.println("\nRuntime: " + e.getMessage());
            System.out.println("Ursache: " + e.getCause().getMessage());
        }
    }
}
```

### 4.4 initCause() – nachträgliches Setzen der Ursache [Fortgeschritten]

`initCause(Throwable cause)` ist die Alternative zum cause-Konstruktor für Situationen, in denen kein Konstruktor mit `Throwable`-Parameter existiert — etwa bei älteren oder fremden Exception-Klassen. Die Methode darf nur **einmal** aufgerufen werden; ein zweiter Aufruf wirft `IllegalStateException`. Wurde die Ursache bereits über den Konstruktor gesetzt, schlägt `initCause()` ebenfalls fehl. Der Rückgabewert ist `this`, was Method Chaining ermöglicht.

```java
public class InitCauseDemo {
    // Alte Exception ohne cause-Konstruktor (simuliert Legacy-Code)
    static class LegacyException extends Exception {
        public LegacyException(String message) {
            super(message); // kein Konstruktor mit Throwable!
        }
    }

    // initCause() verwenden, wenn kein cause-Konstruktor verfügbar ist
    static void legacyOperation() throws LegacyException {
        try {
            int ergebnis = 10 / 0; // ArithmeticException
        } catch (ArithmeticException e) {
            LegacyException le = new LegacyException("Berechnung fehlgeschlagen");
            le.initCause(e); // Ursache nachträglich setzen
            throw le;
        }
    }

    // initCause() gibt 'this' zurück -> Method Chaining möglich
    static void chainedInitCause() throws Exception {
        try {
            String s = null;
            s.length(); // NullPointerException
        } catch (NullPointerException e) {
            // Rückgabewert this erlaubt direkte Verwendung im throw
            throw (RuntimeException) new RuntimeException("Null-Zugriff").initCause(e);
        }
    }

    // FEHLER: initCause() nur einmal erlaubt
    static void initCauseZweimalFehler() {
        RuntimeException ex = new RuntimeException("Nachricht");
        ex.initCause(new IllegalArgumentException("Ursache 1"));
        try {
            ex.initCause(new IllegalStateException("Ursache 2")); // wirft IllegalStateException!
        } catch (IllegalStateException ise) {
            System.out.println("Fehler: " + ise.getMessage()); // cause already initialized
        }
    }

    // FEHLER: initCause() schlägt fehl, wenn cause im Konstruktor gesetzt wurde
    static void initCauseNachKonstruktorFehler() {
        Throwable ursache = new ArithmeticException("Division");
        RuntimeException ex = new RuntimeException("Nachricht", ursache); // cause gesetzt
        try {
            ex.initCause(new IllegalArgumentException("Andere Ursache")); // IllegalStateException!
        } catch (IllegalStateException ise) {
            System.out.println("Fehler: " + ise.getMessage());
        }
    }

    public static void main(String[] args) {
        // Legacy-Exception mit initCause
        try {
            legacyOperation();
        } catch (LegacyException e) {
            System.out.println("LegacyException: " + e.getMessage());
            System.out.println("Ursache: " + e.getCause().getClass().getSimpleName()
                + " - " + e.getCause().getMessage());
        }

        // Method Chaining mit initCause
        try {
            chainedInitCause();
        } catch (Exception e) {
            System.out.println("Chained: " + e.getMessage()
                + " <- " + e.getCause().getClass().getSimpleName());
        }

        // Fehlerszenarien demonstrieren
        initCauseZweimalFehler();
        initCauseNachKonstruktorFehler();
    }
}
```

**Praktische Hinweise:**
- Bevorzuge immer den `cause`-Konstruktor (`new MyException("msg", cause)`) — er ist kürzer, unveränderlich und explizit.
- `initCause()` nur verwenden, wenn kein passender Konstruktor existiert (Legacy-Code, fremde Bibliotheken).
- `getCause()` gibt `null` zurück, wenn keine Ursache gesetzt wurde — immer prüfen, bevor auf `getCause()` zugegriffen wird.
- Beide Methoden gehören zur Klasse `Throwable` und sind damit für alle Exceptions und Errors verfügbar.

---

## 5. try-with-resources

### 5.1 Das AutoCloseable-Interface

```java
import java.io.*;

public class TryWithResources {
    // Eigene AutoCloseable-Implementierung
    static class Datenbankverbindung implements AutoCloseable {
        private final String url;
        private boolean offen = false;

        Datenbankverbindung(String url) throws Exception {
            this.url = url;
            System.out.println("Verbindung zu " + url + " geöffnet");
            this.offen = true;
        }

        String abfragen(String sql) {
            if (!offen) throw new IllegalStateException("Verbindung geschlossen");
            return "Ergebnis von: " + sql;
        }

        @Override
        public void close() throws Exception {
            if (offen) {
                System.out.println("Verbindung zu " + url + " geschlossen");
                offen = false;
            }
        }
    }

    public static void main(String[] args) {
        // Ohne try-with-resources (altmodisch, fehleranfällig)
        Datenbankverbindung verbOhne = null;
        try {
            verbOhne = new Datenbankverbindung("jdbc:mysql://localhost/db");
            String ergebnis = verbOhne.abfragen("SELECT * FROM users");
            System.out.println(ergebnis);
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        } finally {
            if (verbOhne != null) {
                try {
                    verbOhne.close(); // close() kann auch Exception werfen!
                } catch (Exception e) {
                    System.out.println("Fehler beim Schließen: " + e.getMessage());
                }
            }
        }

        System.out.println("---");

        // Mit try-with-resources (Java 7+): sauber und sicher
        try (Datenbankverbindung verb = new Datenbankverbindung("jdbc:mysql://localhost/db")) {
            String ergebnis = verb.abfragen("SELECT * FROM users");
            System.out.println(ergebnis);
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        }
        // close() wird AUTOMATISCH aufgerufen, auch bei Exception!
    }
}
```

### 5.2 Mehrere Ressourcen und Suppressed Exceptions

```java
import java.io.*;

public class MehrereRessourcen {
    // Ressource die beim Schließen Exception wirft
    static class ProblemRessource implements AutoCloseable {
        private final String name;
        private final boolean closeWirftException;

        ProblemRessource(String name, boolean closeWirftException) throws Exception {
            this.name = name;
            this.closeWirftException = closeWirftException;
            System.out.println("Geöffnet: " + name);
        }

        void benutzen() throws Exception {
            System.out.println("Benutzt: " + name);
        }

        @Override
        public void close() throws Exception {
            System.out.println("Schließe: " + name);
            if (closeWirftException) {
                throw new Exception("Fehler beim Schließen von " + name);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Mehrere Ressourcen: werden in umgekehrter Reihenfolge geschlossen!
        System.out.println("--- Mehrere Ressourcen ---");
        try (ProblemRessource r1 = new ProblemRessource("Ressource 1", false);
             ProblemRessource r2 = new ProblemRessource("Ressource 2", false);
             ProblemRessource r3 = new ProblemRessource("Ressource 3", false)) {

            r1.benutzen();
            r2.benutzen();
            r3.benutzen();
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        }
        // Schließreihenfolge: r3, r2, r1

        System.out.println("\n--- Suppressed Exceptions ---");
        // Wenn close() AND der Body Exceptions werfen:
        // Haupt-Exception = Body-Exception
        // Suppressed Exception = Close-Exception(s)
        try (ProblemRessource r = new ProblemRessource("ProblemRessource", true)) {
            throw new Exception("Hauptfehler im Body");
        } catch (Exception e) {
            System.out.println("Hauptfehler: " + e.getMessage());
            // Suppressed Exceptions
            Throwable[] suppressed = e.getSuppressed();
            System.out.println("Suppressed: " + suppressed.length);
            for (Throwable s : suppressed) {
                System.out.println("  Suppressed: " + s.getMessage());
            }
        }

        // Effektiv-finale Variablen in try-with-resources (Java 9+)
        System.out.println("\n--- Effektiv finale Ressource (Java 9+) ---");
        ProblemRessource existierend = new ProblemRessource("Extern erstellt", false);
        try (existierend) {  // Java 9+: Variable statt Deklaration
            existierend.benutzen();
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        // Standard-Ressourcen: BufferedReader, etc.
        System.out.println("\n--- Reales Beispiel: Datei lesen ---");
        File tempFile = File.createTempFile("test", ".txt");
        try (var writer = new java.io.FileWriter(tempFile);
             var bw = new BufferedWriter(writer)) {
            bw.write("Testinhalt\nZweite Zeile");
        }

        try (var reader = new java.io.FileReader(tempFile);
             var br = new BufferedReader(reader)) {
            String zeile;
            while ((zeile = br.readLine()) != null) {
                System.out.println("Gelesen: " + zeile);
            }
        }
        tempFile.delete();
    }
}
```

---

## 6. throw vs. throws

### 6.1 throw – Exception werfen

```java
public class ThrowVsThrows {
    // 'throw': wirft eine Exception (Anweisung)
    static void prüfeAlter(int alter) {
        if (alter < 0) {
            throw new IllegalArgumentException("Alter darf nicht negativ sein: " + alter);
        }
        if (alter > 150) {
            throw new IllegalArgumentException("Unrealistisches Alter: " + alter);
        }
        System.out.println("Alter ist gültig: " + alter);
    }

    // 'throws': deklariert, dass eine checked Exception geworfen werden kann
    static void ladeBenutzer(int id) throws java.sql.SQLException {
        if (id <= 0) {
            throw new java.sql.SQLException("Ungültige ID: " + id);
        }
        System.out.println("Benutzer " + id + " geladen");
    }

    // throws bei mehreren Exceptions
    static void komplexeOperation(String datei, int id)
            throws java.io.IOException, java.sql.SQLException {
        if (datei == null) throw new java.io.IOException("Datei ist null");
        if (id <= 0) throw new java.sql.SQLException("Ungültige ID");
    }

    // Exception re-throw: fangen und neu werfen
    static void rethrowDemo() throws Exception {
        try {
            throw new RuntimeException("Original");
        } catch (RuntimeException e) {
            System.out.println("Gefangen: " + e.getMessage());
            throw e; // erneut werfen (selbe Exception-Instanz)
        }
    }

    // Exception wrappen und weiterwerfen
    static void wrapDemo() throws Exception {
        try {
            throw new java.io.IOException("IO-Fehler");
        } catch (java.io.IOException e) {
            throw new RuntimeException("Verarbeitungsfehler", e); // gewrappt
        }
    }

    public static void main(String[] args) {
        // throw testen
        try {
            prüfeAlter(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // throws testen
        try {
            ladeBenutzer(0);
        } catch (java.sql.SQLException e) {
            System.out.println("SQL: " + e.getMessage());
        }

        // re-throw
        try {
            rethrowDemo();
        } catch (Exception e) {
            System.out.println("Re-thrown: " + e.getMessage());
        }
    }
}
```

---

## 7. Java Logging

### 7.1 java.util.logging – Grundlagen

```java
import java.util.logging.*;
import java.io.*;

public class LoggingGrundlagen {
    // Logger für diese Klasse
    private static final Logger LOG = Logger.getLogger(LoggingGrundlagen.class.getName());

    public static void main(String[] args) throws Exception {
        // Log-Level (von niedrigster zu höchster Priorität):
        // FINEST  -> FINER -> FINE -> CONFIG -> INFO -> WARNING -> SEVERE
        // (ALL = alle, OFF = keine)

        // Standard-Level ist INFO

        LOG.info("Anwendung gestartet");
        LOG.warning("Warnung: Ressource fast erschöpft");
        LOG.severe("Schwerwiegender Fehler!");

        // Standardmäßig nicht angezeigt (unter INFO):
        LOG.config("Konfigurationsnachricht");
        LOG.fine("Debug-Information");
        LOG.finer("Detailliertere Debug-Info");
        LOG.finest("Sehr detaillierte Info");

        // Level setzen
        LOG.setLevel(Level.ALL); // zeigt alle Level

        // Handler erstellen und konfigurieren
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        LOG.addHandler(handler);

        // Jetzt werden alle Level angezeigt:
        LOG.fine("Fine ist jetzt sichtbar");
        LOG.finest("Finest auch");

        // Formatierte Nachrichten
        String benutzer = "Alice";
        int versuche = 3;
        LOG.log(Level.WARNING, "Benutzer {0} nach {1} Versuchen gesperrt",
            new Object[]{benutzer, versuche});

        // Exception loggen
        try {
            int[] arr = new int[5];
            arr[10] = 42;
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.log(Level.SEVERE, "Array-Zugriffsfehler aufgetreten", e);
        }

        // Methoden-Tracing
        LOG.entering("LoggingGrundlagen", "main", new Object[]{"args"});
        // ... Methodenrumpf ...
        LOG.exiting("LoggingGrundlagen", "main", "Ergebnis");
    }
}
```

### 7.2 Logger-Hierarchie und Handler

```java
import java.util.logging.*;
import java.io.*;
import java.util.*;

public class LoggerKonfiguration {
    // Root Logger: "" (leerer String)
    // Hierarchie: com.example.MyClass -> com.example -> com -> ""(Root)

    static final Logger rootLogger = Logger.getLogger("");
    static final Logger appLogger = Logger.getLogger("com.meineapp");
    static final Logger serviceLogger = Logger.getLogger("com.meineapp.service");

    public static void main(String[] args) throws Exception {
        // Handler-Typen:
        // ConsoleHandler: auf System.err
        // FileHandler: in Datei
        // StreamHandler: auf OutputStream
        // SocketHandler: über Netzwerk
        // MemoryHandler: im Puffer

        // Root-Logger konfigurieren (beeinflusst alle Logger)
        rootLogger.setLevel(Level.ALL);

        // ConsoleHandler: auf Konsole
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);

        // FileHandler: in Datei loggen
        FileHandler fileHandler = new FileHandler(
            System.getProperty("java.io.tmpdir") + "/myapp.log",
            true // append
        );
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new SimpleFormatter());
        appLogger.addHandler(fileHandler);

        // Eigener Formatter
        fileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s %s: %s%n",
                    new Date(record.getMillis()),
                    record.getLevel(),
                    record.getLoggerName(),
                    record.getMessage()
                );
            }
        });

        // Logging testen
        appLogger.info("Anwendung gestartet");
        serviceLogger.warning("Service-Warnung");
        appLogger.severe("Kritischer Fehler");

        // Logger-Properties per logging.properties konfigurieren:
        /*
        handlers = java.util.logging.ConsoleHandler
        .level = INFO
        com.meineapp.level = ALL
        com.meineapp.handlers = java.util.logging.FileHandler
        java.util.logging.FileHandler.pattern = %h/myapp%g.log
        java.util.logging.FileHandler.limit = 1048576
        java.util.logging.FileHandler.count = 5
        java.util.logging.SimpleFormatter.format = [%1$tF %1$tT] [%4$-7s] %5$s %n
        */

        // Programmatisch laden:
        // LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));

        fileHandler.close();
    }
}
```

### 7.3 Logging Best Practices

```java
import java.util.logging.*;

public class LoggingBestPractices {
    private static final Logger LOG = Logger.getLogger(LoggingBestPractices.class.getName());

    // Best Practice 1: Prüfe Level vor teuren Operationen
    static void performanteLogging(int[] daten) {
        // FALSCH: String immer erstellt, auch wenn FINE nicht aktiv
        // LOG.fine("Daten: " + Arrays.toString(daten)); // toString immer ausgeführt

        // RICHTIG: nur bei aktivem Level
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Daten: " + java.util.Arrays.toString(daten));
        }

        // Mit Lambda (Java 8+, lazy evaluation):
        LOG.log(Level.FINE, () -> "Daten: " + java.util.Arrays.toString(daten));
    }

    // Best Practice 2: Einheitliche Logger-Namen
    // IMMER: Logger.getLogger(Klasse.class.getName())
    // NICHT: Logger.getLogger("MyLogger") (Tippfehler möglich!)

    // Best Practice 3: Exceptions korrekt loggen
    static void fehlerBehandlung() {
        try {
            throw new RuntimeException("Testfehler");
        } catch (RuntimeException e) {
            // FALSCH: Exception-Info geht verloren
            // LOG.warning("Fehler: " + e.getMessage());

            // RICHTIG: Exception übergeben (Stack Trace wird geloggt)
            LOG.log(Level.WARNING, "Verarbeitungsfehler aufgetreten", e);
        }
    }

    // Best Practice 4: Sinnvolle Level wählen
    static void levelVerwenden(String benutzerName, boolean erfolg) {
        LOG.finest("Methode aufgerufen mit: " + benutzerName); // nur für tiefstes Debugging
        LOG.finer("Detaillierter Ablauf...");                  // sehr detailliert
        LOG.fine("Debug-Information");                         // normale Entwicklung
        LOG.config("Konfiguration: Server=localhost");         // einmalig beim Start
        LOG.info("Benutzer " + benutzerName + " hat sich angemeldet"); // normaler Betrieb
        if (!erfolg) {
            LOG.warning("Anmeldung fehlgeschlagen für: " + benutzerName); // mögl. Problem
        }
        // LOG.severe() nur für wirklich schwerwiegende Fehler!
    }

    // Best Practice 5: Keine sensiblen Daten loggen!
    static void sicheresLoggen(String benutzername, String passwort) {
        // FALSCH: Passwort wird geloggt!
        // LOG.info("Login-Versuch: " + benutzername + "/" + passwort);

        // RICHTIG: Nur harmlose Daten
        LOG.info("Login-Versuch für Benutzer: " + benutzername);
        // Passwort nie in Logs!
    }

    public static void main(String[] args) {
        LOG.info("=== Logging Best Practices Demo ===");
        performanteLogging(new int[]{1, 2, 3, 4, 5});
        fehlerBehandlung();
        levelVerwenden("alice", true);
        levelVerwenden("bob", false);
        sicheresLoggen("alice", "geheimesPasswort123");
    }
}
```

---

## 8. Debugging-Grundlagen

### 8.1 Debugging-Techniken

```java
import java.util.*;
import java.util.logging.*;

public class DebuggingTechniken {
    private static final Logger LOG = Logger.getLogger(DebuggingTechniken.class.getName());

    // 1. Assertions: Invarianten prüfen
    static int fakultaet(int n) {
        assert n >= 0 : "Eingabe muss nicht-negativ sein, war: " + n;

        if (n == 0 || n == 1) return 1;
        int ergebnis = n * fakultaet(n - 1);

        assert ergebnis > 0 : "Fakultät muss positiv sein";
        return ergebnis;
    }
    // Assertions aktivieren mit: java -ea MeineKlasse

    // 2. Defensive Programmierung mit Objects.requireNonNull
    static void verarbeite(String eingabe, List<Integer> daten) {
        Objects.requireNonNull(eingabe, "Eingabe darf nicht null sein");
        Objects.requireNonNull(daten, "Daten dürfen nicht null sein");

        if (eingabe.isBlank()) {
            throw new IllegalArgumentException("Eingabe darf nicht leer sein");
        }
        if (daten.isEmpty()) {
            throw new IllegalArgumentException("Daten dürfen nicht leer sein");
        }

        LOG.fine("Verarbeite " + daten.size() + " Elemente");
    }

    // 3. Stack Trace manuell erzeugen (ohne Exception)
    static void aktuelleProgrammposition() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        System.out.println("Aktuelle Position:");
        // Index 0 = getStackTrace, Index 1 = diese Methode, Index 2 = Aufrufer
        for (int i = 1; i < Math.min(trace.length, 5); i++) {
            System.out.println("  " + trace[i]);
        }
    }

    // 4. Debugging mit Logging
    static int berechneKomplex(int[] eingaben) {
        LOG.entering("DebuggingTechniken", "berechneKomplex", eingaben);

        int summe = 0;
        for (int i = 0; i < eingaben.length; i++) {
            int wert = eingaben[i];
            LOG.log(Level.FINEST, () -> "Verarbeite Index " + i + " mit Wert " + wert);

            if (wert < 0) {
                LOG.warning("Negativer Wert bei Index " + i + ": " + wert);
                wert = Math.abs(wert);
            }
            summe += wert;
        }

        LOG.exiting("DebuggingTechniken", "berechneKomplex", summe);
        return summe;
    }

    // 5. Invarianten-Prüfung
    static class BankKonto {
        private double saldo;

        BankKonto(double anfangssaldo) {
            assert anfangssaldo >= 0 : "Anfangssaldo negativ!";
            this.saldo = anfangssaldo;
            prüfeInvariante();
        }

        void einzahlen(double betrag) {
            assert betrag > 0 : "Betrag muss positiv sein";
            double vorher = saldo;
            saldo += betrag;
            assert saldo > vorher : "Saldo muss nach Einzahlung größer sein";
            prüfeInvariante();
        }

        private void prüfeInvariante() {
            assert saldo >= 0 : "INVARIANTE VERLETZT: Saldo negativ: " + saldo;
        }
    }

    public static void main(String[] args) {
        // Assertions testen (nur mit -ea aktiv)
        System.out.println("Fakultät von 5: " + fakultaet(5));

        // Defensive Programmierung
        try {
            verarbeite(null, Arrays.asList(1, 2, 3));
        } catch (NullPointerException e) {
            System.out.println("NPE: " + e.getMessage());
        }

        // Stack Trace
        aktuelleProgrammposition();

        // Debugging mit Logging
        int ergebnis = berechneKomplex(new int[]{1, -3, 5, 2, -1});
        System.out.println("Ergebnis: " + ergebnis);

        // BankKonto testen
        BankKonto konto = new BankKonto(100.0);
        konto.einzahlen(50.0);
        System.out.println("Saldo: " + konto.saldo);
    }
}
```

### 8.2 Häufige Fehler und Lösungsstrategien

```java
public class HaeufigeDebugFehler {
    public static void main(String[] args) {
        // 1. NullPointerException analysieren (Java 14+ hilfreiche NPE)
        try {
            String text = null;
            System.out.println(text.toUpperCase()); // NPE mit Details in Java 14+
        } catch (NullPointerException e) {
            System.out.println("NPE: " + e.getMessage()); // Hilfreiche Meldung in Java 14+
        }

        // 2. Equals mit null-Check
        String meinString = null;
        // FALSCH: meinString.equals("test") -> NPE
        // RICHTIG:
        if ("test".equals(meinString)) { // Konstante zuerst
            System.out.println("Gleich");
        }
        // oder:
        if (java.util.Objects.equals(meinString, "test")) {
            System.out.println("Gleich");
        }

        // 3. Integer-Vergleich: == vs. equals
        Integer a = 127;
        Integer b = 127;
        System.out.println("127 == 127: " + (a == b));  // true (gecacht)

        Integer c = 128;
        Integer d = 128;
        System.out.println("128 == 128: " + (c == d));  // FALSE! (nicht gecacht)
        System.out.println("128 equals: " + c.equals(d)); // true

        // IMMER equals() für Objekte verwenden!

        // 4. String += in Schleife (Performance)
        // FALSCH:
        String ergebnisFalsch = "";
        for (int i = 0; i < 100; i++) {
            ergebnisFalsch += i; // Neues String-Objekt bei jeder Iteration!
        }

        // RICHTIG:
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(i);
        }
        String ergebnisRichtig = sb.toString();

        // 5. Return im finally (überschreibt try/catch return!)
        System.out.println("Problematisches finally: " + problematiischesFinally());
    }

    static String problematiischesFinally() {
        try {
            return "aus try";
        } catch (Exception e) {
            return "aus catch";
        } finally {
            return "aus finally"; // ÜBERSCHREIBT try und catch! Vermeiden!
        }
    }
}
```

---

## Zusammenfassung

### Exception-Hierarchie im Überblick

| Klasse | Art | Behandlung | Beispiele |
|---|---|---|---|
| `Error` | unkontrollierbar | Nicht fangen | `OutOfMemoryError`, `StackOverflowError` |
| `Exception` (nicht Runtime) | checked | Pflicht | `IOException`, `SQLException` |
| `RuntimeException` | unchecked | Optional | `NullPointerException`, `ClassCastException` |

### try-Varianten

```java
// 1. try-catch
try { ... } catch (ExceptionType e) { ... }

// 2. try-catch-finally
try { ... } catch (ExceptionType e) { ... } finally { ... }

// 3. Multi-Catch (Java 7+)
try { ... } catch (Type1 | Type2 | Type3 e) { ... }

// 4. try-with-resources (Java 7+)
try (ResourceType r = new ResourceType()) { ... } catch (...) { ... }

// 5. try-finally (ohne catch)
try { ... } finally { ... }
```

### Logging-Level (aufsteigend)

| Level | Wert | Verwendung |
|---|---|---|
| `FINEST` | 300 | Tiefste Debugging-Stufe |
| `FINER` | 400 | Detailliertes Debugging |
| `FINE` | 500 | Normales Debugging |
| `CONFIG` | 700 | Konfigurationsnachrichten |
| `INFO` | 800 | Normaler Betrieb (Standard) |
| `WARNING` | 900 | Mögliche Probleme |
| `SEVERE` | 1000 | Schwerwiegende Fehler |

### Custom Exception Checkliste

- Klasse von `Exception` oder `RuntimeException` ableiten
- Beide Konstruktoren implementieren: `(String message)` und `(String message, Throwable cause)`
- Domänenspezifische Felder hinzufügen (Fehlercode, Kontext)
- `@Override toString()` für aussagekräftige Ausgabe
- Exception-Chaining mit `cause`-Konstruktor verwenden
- Checked Exception: wenn Aufrufer sinnvoll reagieren kann
- Unchecked Exception: wenn Fehler auf Programmierfehler hindeutet

---

## Multiple-Choice-Fragen

**Frage 1:** Welche Aussage zu `initCause()` ist korrekt?

- A) `initCause()` kann beliebig oft aufgerufen werden, um die Ursache zu aktualisieren.
- **B) `initCause()` wirft `IllegalStateException`, wenn die Ursache bereits gesetzt wurde — entweder durch einen vorherigen `initCause()`-Aufruf oder durch den Konstruktor.** ✓
- C) `initCause()` ist nur in `RuntimeException` verfügbar, nicht in `Exception`.
- D) `initCause()` gibt `null` zurück und kann daher nicht für Method Chaining genutzt werden.

**Frage 2:** Wann sollte `initCause()` anstelle des cause-Konstruktors verwendet werden?

- A) Wenn die Ursache eine `RuntimeException` ist, immer `initCause()` bevorzugen.
- B) Bei checked Exceptions ist `initCause()` Pflicht.
- **C) Wenn die Exception-Klasse keinen Konstruktor mit `Throwable`-Parameter besitzt, z. B. bei Legacy- oder Fremd-Exceptions.** ✓
- D) `initCause()` ist identisch mit dem cause-Konstruktor und kann immer austauschbar verwendet werden.

**Frage 3:** Was gibt `getCause()` zurück, wenn keine Ursache gesetzt wurde?

- A) Eine leere `RuntimeException` ohne Nachricht.
- B) `Optional.empty()`
- **C) `null`** ✓
- D) Eine `IllegalStateException` mit der Meldung "no cause".

**Frage 4:** Gegeben sei folgender Code:
```java
RuntimeException ex = new RuntimeException("Msg", new ArithmeticException("Division"));
ex.initCause(new IllegalArgumentException("Andere"));
```
Was passiert beim Aufruf von `initCause()`?

- A) Die Ursache wird auf `IllegalArgumentException` aktualisiert.
- B) Der Code kompiliert nicht, weil `initCause()` nur auf `Exception` aufgerufen werden kann.
- **C) Es wird eine `IllegalStateException` geworfen, weil die Ursache bereits im Konstruktor gesetzt wurde.** ✓
- D) Die ursprüngliche `ArithmeticException` wird als Suppressed Exception hinzugefügt.

**Frage 5:** Welche Methode durchläuft korrekt eine vollständige Exception-Chain?

- A) `e.getStackTrace()` liefert alle verketteten Exceptions.
- B) `e.getSuppressed()` enthält alle Ursachen in der Kette.
- **C) Iterativ `getCause()` aufrufen, bis `null` zurückgegeben wird.** ✓
- D) `e.getMessage()` gibt automatisch die vollständige Chain als String aus.

---

## Skill Check: Exception Chaining und initCause

Nach Abschluss dieses Abschnitts solltest du folgendes können:

- [ ] Den Unterschied zwischen constructor-based chaining und `initCause()` erklären
- [ ] `initCause()` korrekt einsetzen, wenn kein cause-Konstruktor verfügbar ist
- [ ] Wissen, dass `initCause()` nur einmal aufgerufen werden darf
- [ ] Mit `getCause()` eine Exception-Chain manuell traversieren
- [ ] Den Rückgabewert `this` von `initCause()` für Method Chaining nutzen
- [ ] Eine Exception-Chain im Stack Trace lesen und die Wurzelursache identifizieren
