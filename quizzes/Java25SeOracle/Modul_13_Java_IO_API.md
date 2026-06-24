# Modul 13: Java IO API

## Übersicht

Dieses Modul behandelt die Java Ein-/Ausgabe-APIs: die klassische `java.io`-Hierarchie sowie die moderne `java.nio.2`-API. Sie lernen, Dateien zu lesen und zu schreiben, Verzeichnisse zu verwalten, Daten zu serialisieren und Konsoleneingaben entgegenzunehmen.

| Thema                        | Dauer |
|------------------------------|-------|
| java.io Overview             | 14 m  |
| File and Path                | 14 m  |
| Reading Files                |  9 m  |
| Writing Files                | 15 m  |
| Directory Operations         | 15 m  |
| NIO.2 Advanced               | 18 m  |
| Serialization                | 16 m  |
| Console Input                |  8 m  |
| Practice 13-1                | 22 m  |
| Practice 13-2                | 42 m  |
| Practice 13-3                | 25 m  |
| **Skill Check: IO API**      | **mind. 80 %** |

---

## 1. java.io – Überblick und Hierarchie

Das Paket `java.io` existiert seit Java 1.0 und bildet die Grundlage aller dateibasierten Ein-/Ausgabeoperationen. Es basiert auf dem **Stream-Konzept**: Daten fließen als kontinuierliche Byte- oder Zeichenfolge zwischen Quelle und Ziel.

### 1.1 Die zwei Hauptäste

```
java.io
├── Byte-Streams (8-Bit)
│   ├── InputStream  (abstrakt)
│   │   ├── FileInputStream
│   │   ├── BufferedInputStream
│   │   └── ObjectInputStream
│   └── OutputStream (abstrakt)
│       ├── FileOutputStream
│       ├── BufferedOutputStream
│       └── ObjectOutputStream
└── Character-Streams (16-Bit Unicode)
    ├── Reader (abstrakt)
    │   ├── FileReader
    │   ├── BufferedReader
    │   └── InputStreamReader
    └── Writer (abstrakt)
        ├── FileWriter
        ├── BufferedWriter
        └── PrintWriter
```

### 1.2 Byte-Streams vs. Character-Streams

| Merkmal              | Byte-Streams                       | Character-Streams                   |
|----------------------|------------------------------------|-------------------------------------|
| Basisklassen         | `InputStream` / `OutputStream`     | `Reader` / `Writer`                 |
| Einheit              | 1 Byte (8 Bit)                     | 1 char (16 Bit Unicode)             |
| Einsatz              | Binärdaten (Bilder, ZIP, etc.)     | Textdateien                         |
| Encoding             | Keine Konvertierung                | Automatische Charset-Konvertierung  |
| Typische Klassen     | `FileInputStream`, `FileOutputStream` | `FileReader`, `FileWriter`       |

### 1.3 Das Decorator-Pattern in java.io

`java.io` nutzt das **Decorator-Muster**: einfache Streams werden durch Wrapper-Klassen erweitert.

```java
// Nur FileInputStream: kein Puffer, langsam
InputStream fis = new FileInputStream("daten.bin");

// Mit BufferedInputStream: Puffer verbessert Performance
InputStream bis = new BufferedInputStream(new FileInputStream("daten.bin"));

// Mit DataInputStream: typisiertes Lesen (int, double, ...)
DataInputStream dis = new DataInputStream(
    new BufferedInputStream(new FileInputStream("daten.bin"))
);
```

---

## 2. Die Klasse `java.io.File`

`File` repräsentiert einen **Pfad im Dateisystem** – es ist keine geöffnete Datei, sondern nur ein Pfad-Handle. Die Klasse existiert seit Java 1.0 und wurde in Java 7 durch `java.nio.2` ergänzt (aber nicht ersetzt).

### 2.1 File-Objekte erstellen

```java
import java.io.File;

// Absoluter Pfad
File f1 = new File("/home/user/dokumente/bericht.txt");

// Relativer Pfad (relativ zum Arbeitsverzeichnis)
File f2 = new File("bericht.txt");

// Eltern + Kind
File verzeichnis = new File("/home/user/dokumente");
File f3 = new File(verzeichnis, "bericht.txt");

// Plattformunabhängig mit File.separator
File f4 = new File("daten" + File.separator + "kunden.csv");
```

### 2.2 Wichtige File-Methoden

```java
File f = new File("beispiel.txt");

// Metadaten
System.out.println(f.exists());          // true/false
System.out.println(f.isFile());          // ist normale Datei?
System.out.println(f.isDirectory());     // ist Verzeichnis?
System.out.println(f.length());          // Größe in Bytes
System.out.println(f.lastModified());    // Zeitstempel (Millisekunden)
System.out.println(f.canRead());         // Leseberechtigung?
System.out.println(f.canWrite());        // Schreibberechtigung?

// Pfad-Informationen
System.out.println(f.getName());         // "beispiel.txt"
System.out.println(f.getParent());       // übergeordnetes Verzeichnis
System.out.println(f.getAbsolutePath()); // vollständiger Pfad
System.out.println(f.getCanonicalPath()); // normalisierter Pfad (IOException!)

// Operationen
f.createNewFile();   // Datei anlegen (IOException)
f.delete();          // Datei/leeres Verzeichnis löschen
f.renameTo(new File("neu.txt")); // umbenennen/verschieben
f.mkdir();           // ein Verzeichnis anlegen
f.mkdirs();          // Verzeichnisbaum anlegen
```

### 2.3 Verzeichnis-Inhalte auflisten

```java
File dir = new File("C:/Projekte");

// Alle Einträge als String-Array
String[] namen = dir.list();

// Alle Einträge als File-Array
File[] dateien = dir.listFiles();

// Gefiltert: nur .java-Dateien
File[] javaDat = dir.listFiles(
    (d, name) -> name.endsWith(".java")
);

// Mit FilenameFilter
FilenameFilter filter = (d, name) -> name.toLowerCase().endsWith(".txt");
File[] txtDateien = dir.listFiles(filter);
```

---

## 3. java.nio.2 – Path, Paths und Files

Java 7 führte mit **NIO.2** (`java.nio.file`) eine moderne, funktionsreiche Alternative zu `java.io.File` ein.

### 3.1 Das Interface `Path`

```java
import java.nio.file.Path;
import java.nio.file.Paths;

// Path erstellen (Java 7)
Path p1 = Paths.get("/home/user/dokumente/bericht.txt");

// Moderner: Path.of() seit Java 11
Path p2 = Path.of("/home/user/dokumente/bericht.txt");
Path p3 = Path.of("/home/user", "dokumente", "bericht.txt");

// Pfad-Operationen
System.out.println(p1.getFileName());    // bericht.txt
System.out.println(p1.getParent());      // /home/user/dokumente
System.out.println(p1.getRoot());        // / (Linux) oder C:\ (Windows)
System.out.println(p1.getNameCount());   // Anzahl der Segmente
System.out.println(p1.getName(0));       // erstes Segment
System.out.println(p1.isAbsolute());     // true

// Pfade kombinieren
Path basis = Path.of("/home/user");
Path komplett = basis.resolve("dokumente/bericht.txt");

// Relativierung
Path von = Path.of("/home/user");
Path zu  = Path.of("/home/user/dokumente/bericht.txt");
Path rel = von.relativize(zu); // dokumente/bericht.txt

// Normalisieren (.. und . auflösen)
Path unordentlich = Path.of("/home/user/../user/./dokumente");
Path sauber = unordentlich.normalize(); // /home/user/dokumente

// Umwandlung
File altesFile = p1.toFile();
Path vonFile   = altesFile.toPath();
```

### 3.2 File vs. Path – Vergleich

| Aspekt                    | `java.io.File`           | `java.nio.file.Path`               |
|---------------------------|--------------------------|------------------------------------|
| Einführung                | Java 1.0                 | Java 7                             |
| Fehlerbehandlung          | Gibt `false` zurück      | Wirft spezifische Exceptions        |
| Symbolische Links         | Keine Unterstützung      | Vollständige Unterstützung          |
| Metadaten                 | Begrenzt                 | Umfangreich via `BasicFileAttributes` |
| Operationen               | In `File` selbst         | In `Files`-Utility-Klasse          |
| NFS / Netzwerkpfade       | Problematisch            | Unterstützt via `FileSystem`        |
| Wandel zu Path            | `file.toPath()`          | –                                  |

---

## 4. Dateien lesen

### 4.1 Mit FileReader und BufferedReader (klassisch)

```java
import java.io.*;

// Ohne Puffer: zeichenweise (langsam)
try (FileReader fr = new FileReader("text.txt")) {
    int zeichen;
    while ((zeichen = fr.read()) != -1) {
        System.out.print((char) zeichen);
    }
}

// Mit BufferedReader: zeilenweise (empfohlen für große Dateien)
try (BufferedReader br = new BufferedReader(new FileReader("text.txt"))) {
    String zeile;
    while ((zeile = br.readLine()) != null) {
        System.out.println(zeile);
    }
}

// Mit Stream (Java 8+)
try (BufferedReader br = new BufferedReader(new FileReader("text.txt"))) {
    br.lines()
      .filter(z -> !z.isBlank())
      .forEach(System.out::println);
}
```

### 4.2 Mit Files-Utility (NIO.2 – empfohlen)

```java
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

Path pfad = Path.of("text.txt");

// Alle Zeilen auf einmal lesen (für kleine Dateien)
List<String> zeilen = Files.readAllLines(pfad);
List<String> mitCharset = Files.readAllLines(pfad, StandardCharsets.UTF_8);

// Gesamten Inhalt als String (Java 11+)
String inhalt = Files.readString(pfad);
String inhaltUtf8 = Files.readString(pfad, StandardCharsets.UTF_8);

// Als Stream (für große Dateien – lazy loading)
try (var stream = Files.lines(pfad)) {
    stream.filter(z -> z.contains("Fehler"))
          .forEach(System.out::println);
}

// Alle Bytes lesen
byte[] bytes = Files.readAllBytes(pfad);
```

### 4.3 Mit InputStreamReader und Charset

```java
import java.io.*;
import java.nio.charset.StandardCharsets;

// Explizite Charset-Angabe – wichtig für Windows-Systeme!
try (BufferedReader br = new BufferedReader(
        new InputStreamReader(
            new FileInputStream("text.txt"),
            StandardCharsets.UTF_8))) {
    br.lines().forEach(System.out::println);
}
```

---

## 5. Dateien schreiben

### 5.1 Mit FileWriter und BufferedWriter (klassisch)

```java
import java.io.*;

// FileWriter – überschreibt Datei (append = false)
try (FileWriter fw = new FileWriter("ausgabe.txt")) {
    fw.write("Erste Zeile\n");
    fw.write("Zweite Zeile\n");
}

// Anhängen (append = true)
try (FileWriter fw = new FileWriter("ausgabe.txt", true)) {
    fw.write("Neue Zeile wird angehängt\n");
}

// Mit BufferedWriter (gepuffert und effizienter)
try (BufferedWriter bw = new BufferedWriter(new FileWriter("ausgabe.txt"))) {
    bw.write("Erste Zeile");
    bw.newLine();   // plattformunabhängiger Zeilenumbruch
    bw.write("Zweite Zeile");
    bw.newLine();
}

// PrintWriter: komfortables Schreiben wie System.out
try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("log.txt")))) {
    pw.println("Log-Eintrag 1");
    pw.printf("Wert: %d%n", 42);
    pw.format("Name: %-10s Alter: %3d%n", "Alice", 30);
}
```

### 5.2 Mit Files-Utility (NIO.2 – empfohlen)

```java
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

Path pfad = Path.of("ausgabe.txt");

// String schreiben (Java 11+)
Files.writeString(pfad, "Hallo Welt\n");
Files.writeString(pfad, "Angehängt\n", StandardOpenOption.APPEND);

// Alle Zeilen schreiben
List<String> zeilen = List.of("Zeile 1", "Zeile 2", "Zeile 3");
Files.write(pfad, zeilen);
Files.write(pfad, zeilen, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

// Bytes schreiben
byte[] daten = "Binärdaten".getBytes(StandardCharsets.UTF_8);
Files.write(pfad, daten);
```

### 5.3 StandardOpenOption – Optionen beim Schreiben

```java
import java.nio.file.StandardOpenOption;

// Datei überschreiben (Standard)
Files.writeString(pfad, "Text", StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);

// Anhängen
Files.writeString(pfad, "Text", StandardOpenOption.APPEND);

// Nur schreiben wenn Datei noch nicht existiert
Files.writeString(pfad, "Text", StandardOpenOption.CREATE_NEW);

// Synchrone Schreibvorgänge (kein OS-Puffer)
Files.writeString(pfad, "Text", StandardOpenOption.SYNC);
```

| Option                  | Beschreibung                                            |
|-------------------------|---------------------------------------------------------|
| `CREATE`                | Datei anlegen falls nicht vorhanden                     |
| `CREATE_NEW`            | Nur anlegen wenn nicht vorhanden, sonst Exception       |
| `TRUNCATE_EXISTING`     | Vorhandene Datei leeren                                 |
| `APPEND`                | An vorhandene Datei anhängen                            |
| `WRITE`                 | Schreibzugriff (Standard)                               |
| `READ`                  | Lesezugriff                                             |
| `SYNC`                  | Metadaten + Inhalt sofort auf Datenträger               |
| `DSYNC`                 | Nur Inhalt sofort auf Datenträger                       |
| `DELETE_ON_CLOSE`       | Datei beim Schließen löschen (Temporärdateien)          |
| `SPARSE`                | Sparse-Datei anlegen (für große Dateien mit Lücken)     |

---

## 6. Verzeichnisoperationen

### 6.1 Verzeichnisse erstellen und löschen

```java
import java.nio.file.*;
import java.io.IOException;

// Ein Verzeichnis
Files.createDirectory(Path.of("neuesVerz"));

// Verzeichnisbaum anlegen
Files.createDirectories(Path.of("a/b/c/d"));

// Temporäres Verzeichnis
Path tmp = Files.createTempDirectory("meinProgramm_");
System.out.println("Temp: " + tmp);

// Datei löschen (Exception wenn nicht vorhanden)
Files.delete(Path.of("alte.txt"));

// Löschen wenn vorhanden (keine Exception)
Files.deleteIfExists(Path.of("vielleicht.txt"));
```

### 6.2 Kopieren und Verschieben

```java
import java.nio.file.*;

Path quelle  = Path.of("original.txt");
Path ziel    = Path.of("kopie.txt");

// Kopieren
Files.copy(quelle, ziel);

// Kopieren und vorhandene Datei überschreiben
Files.copy(quelle, ziel, StandardCopyOption.REPLACE_EXISTING);

// Symbolische Links folgen
Files.copy(quelle, ziel, LinkOption.NOFOLLOW_LINKS);

// Verschieben / umbenennen
Files.move(quelle, Path.of("umbenannt.txt"));
Files.move(quelle, ziel, StandardCopyOption.REPLACE_EXISTING);

// Atomares Verschieben (garantiert atomar)
Files.move(quelle, ziel, StandardCopyOption.ATOMIC_MOVE);
```

### 6.3 Verzeichnisse auflisten

```java
import java.nio.file.*;
import java.io.IOException;

Path verz = Path.of("src/main/java");

// Direkte Kinder auflisten (nicht rekursiv)
try (var stream = Files.list(verz)) {
    stream.forEach(System.out::println);
}

// Nur Unterverzeichnisse
try (var stream = Files.list(verz)) {
    stream.filter(Files::isDirectory)
          .forEach(System.out::println);
}

// Rekursiv alle Dateien durchlaufen
try (var stream = Files.walk(verz)) {
    stream.filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".java"))
          .forEach(System.out::println);
}

// Mit maximaler Tiefe
try (var stream = Files.walk(verz, 2)) {
    stream.forEach(System.out::println);
}
```

### 6.4 Glob-Muster und find()

```java
import java.nio.file.*;

Path basis = Path.of("C:/Projekte");

// find() mit Prädikat
try (var stream = Files.find(basis, Integer.MAX_VALUE,
        (pfad, attr) -> attr.isRegularFile()
                     && pfad.toString().endsWith(".java"))) {
    stream.forEach(System.out::println);
}

// DirectoryStream mit Glob
try (DirectoryStream<Path> ds = Files.newDirectoryStream(
        Path.of("docs"), "*.{md,txt}")) {
    for (Path p : ds) {
        System.out.println(p.getFileName());
    }
}
```

---

## 7. NIO.2 Advanced – Datei-Metadaten und Attribute

### 7.1 BasicFileAttributes

```java
import java.nio.file.*;
import java.nio.file.attribute.*;

Path pfad = Path.of("bericht.pdf");

// Alle Basis-Attribute auf einmal lesen
BasicFileAttributes attrs = Files.readAttributes(
    pfad, BasicFileAttributes.class);

System.out.println("Größe:       " + attrs.size() + " Bytes");
System.out.println("Erstellt:    " + attrs.creationTime());
System.out.println("Geändert:    " + attrs.lastModifiedTime());
System.out.println("Zugegriffen: " + attrs.lastAccessTime());
System.out.println("Datei?       " + attrs.isRegularFile());
System.out.println("Verz.?       " + attrs.isDirectory());
System.out.println("Symlink?     " + attrs.isSymbolicLink());

// Einzelne Attribute lesen (Files.getAttribute)
long groesse = (long) Files.getAttribute(pfad, "basic:size");
FileTime aenderung = (FileTime) Files.getAttribute(pfad, "basic:lastModifiedTime");
```

### 7.2 Dateien überwachen mit WatchService

```java
import java.nio.file.*;

WatchService watcher = FileSystems.getDefault().newWatchService();

Path dir = Path.of("C:/Beobachtet");
dir.register(watcher,
    StandardWatchEventKinds.ENTRY_CREATE,
    StandardWatchEventKinds.ENTRY_DELETE,
    StandardWatchEventKinds.ENTRY_MODIFY);

System.out.println("Beobachte " + dir + " ...");

while (true) {
    WatchKey key = watcher.take(); // blockiert bis Ereignis
    for (WatchEvent<?> event : key.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();
        Path dateiname = (Path) event.context();
        System.out.printf("Ereignis: %-20s Datei: %s%n", kind, dateiname);
    }
    if (!key.reset()) break; // Verzeichnis nicht mehr zugänglich
}
```

### 7.3 Temporäre Dateien

```java
import java.nio.file.*;

// Temporäre Datei
Path tmpDatei = Files.createTempFile("prefix_", "_suffix.tmp");
System.out.println("Temp-Datei: " + tmpDatei);

// Automatisch löschen beim Beenden der JVM
tmpDatei.toFile().deleteOnExit();

// Oder mit DELETE_ON_CLOSE
try (var channel = Files.newByteChannel(tmpDatei,
        StandardOpenOption.DELETE_ON_CLOSE,
        StandardOpenOption.WRITE)) {
    // Datei wird beim Schließen gelöscht
}
```

---

## 8. Serialisierung

Serialisierung ermöglicht es, Java-Objekte in einen Byte-Strom umzuwandeln (und zurück), um sie zu speichern oder zu übertragen.

### 8.1 Das Interface Serializable

```java
import java.io.Serializable;

// Eine serialisierbare Klasse
public class Kunde implements Serializable {

    // Versionskennung für Kompatibilitätsprüfung
    private static final long serialVersionUID = 1L;

    private String name;
    private String email;
    private int kundennummer;

    // transient: Feld wird NICHT serialisiert
    private transient String passwort;

    // static: Klassenfelder werden nie serialisiert
    private static int naechsteNummer = 1000;

    public Kunde(String name, String email, int kundennummer) {
        this.name = name;
        this.email = email;
        this.kundennummer = kundennummer;
        this.passwort = "geheim123";
    }

    @Override
    public String toString() {
        return "Kunde{name='" + name + "', email='" + email
             + "', nr=" + kundennummer
             + ", passwort='" + passwort + "'}";
    }
}
```

### 8.2 Objekte schreiben mit ObjectOutputStream

```java
import java.io.*;
import java.util.List;

List<Kunde> kunden = List.of(
    new Kunde("Alice", "alice@example.com", 1001),
    new Kunde("Bob",   "bob@example.com",   1002)
);

// Serialisieren
try (ObjectOutputStream oos = new ObjectOutputStream(
        new BufferedOutputStream(
            new FileOutputStream("kunden.ser")))) {
    oos.writeObject(kunden);
    System.out.println("Kunden serialisiert.");
}
```

### 8.3 Objekte lesen mit ObjectInputStream

```java
import java.io.*;
import java.util.List;

// Deserialisieren
try (ObjectInputStream ois = new ObjectInputStream(
        new BufferedInputStream(
            new FileInputStream("kunden.ser")))) {

    @SuppressWarnings("unchecked")
    List<Kunde> geladenKunden = (List<Kunde>) ois.readObject();

    for (Kunde k : geladenKunden) {
        System.out.println(k);
        // ACHTUNG: passwort ist null (transient!)
    }
} catch (ClassNotFoundException e) {
    System.err.println("Klasse nicht gefunden: " + e.getMessage());
}
```

### 8.4 Serialisierung – wichtige Regeln

| Regel                            | Details                                                      |
|----------------------------------|--------------------------------------------------------------|
| `implements Serializable`        | Pflicht für serialisierbare Klassen                         |
| `serialVersionUID`               | Sollte explizit gesetzt werden (Kompatibilität)              |
| `transient`                      | Felder werden nicht serialisiert (Wert = null / 0)           |
| `static`                         | Klassenfelder werden nie serialisiert                        |
| Elternklasse nicht serialisierbar | Eltern-Felder werden nicht gespeichert                      |
| `readResolve()` / `writeReplace()` | Hooks für benutzerdefinierte Serialisierung                |
| Sicherheit                       | Niemals ungeprüfte Daten deserialisieren (Angriffspotenzial) |

### 8.5 Serialisierung mit Records (Java 16+)

```java
// Records sind automatisch serialisierbar (wenn explizit deklariert)
public record ProduktRecord(String name, double preis, int menge)
        implements Serializable {
    private static final long serialVersionUID = 1L;
}

// Verwendung
ProduktRecord p = new ProduktRecord("Laptop", 999.99, 5);

try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream("produkt.ser"))) {
    oos.writeObject(p);
}

try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream("produkt.ser"))) {
    ProduktRecord geladen = (ProduktRecord) ois.readObject();
    System.out.println(geladen); // ProduktRecord[name=Laptop, preis=999.99, menge=5]
}
```

---

## 9. Konsoleneingabe mit Scanner

### 9.1 Scanner mit System.in

```java
import java.util.Scanner;

Scanner scanner = new Scanner(System.in);

System.out.print("Bitte Namen eingeben: ");
String name = scanner.nextLine();

System.out.print("Bitte Alter eingeben: ");
int alter = scanner.nextInt();
scanner.nextLine(); // Zeilenende konsumieren!

System.out.print("Bitte Gehalt eingeben: ");
double gehalt = scanner.nextDouble();

System.out.printf("Name: %s, Alter: %d, Gehalt: %.2f%n",
    name, alter, gehalt);

scanner.close();
```

### 9.2 Scanner-Methoden im Überblick

```java
Scanner sc = new Scanner(System.in);

// Nächste komplette Zeile
String zeile = sc.nextLine();

// Nächstes Token (durch Whitespace getrennt)
String token = sc.next();

// Primitive Typen lesen
int    i = sc.nextInt();
long   l = sc.nextLong();
double d = sc.nextDouble();
float  f = sc.nextFloat();
boolean b = sc.nextBoolean();

// Prüfen ob weiteres Token vorhanden
while (sc.hasNextInt()) {
    System.out.println("Zahl: " + sc.nextInt());
}

// Mit Pattern prüfen
if (sc.hasNext("[A-Z][a-z]+")) {
    System.out.println("Großgeschrieben: " + sc.next());
}
```

### 9.3 Scanner mit Dateien

```java
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

// CSV-Datei einlesen
try (Scanner sc = new Scanner(new File("kunden.csv"))) {
    sc.useDelimiter("[,\n]"); // Komma oder Zeilenumbruch als Trennzeichen
    while (sc.hasNext()) {
        String feld = sc.next().trim();
        System.out.println("Feld: " + feld);
    }
}

// Zeilenweise
try (Scanner sc = new Scanner(Path.of("daten.txt"))) {
    int zeilennummer = 0;
    while (sc.hasNextLine()) {
        System.out.printf("%3d: %s%n", ++zeilennummer, sc.nextLine());
    }
}
```

### 9.4 Eingabevalidierung mit Scanner

```java
import java.util.Scanner;
import java.util.InputMismatchException;

Scanner sc = new Scanner(System.in);
int zahl = 0;
boolean gueltig = false;

while (!gueltig) {
    System.out.print("Bitte eine ganze Zahl zwischen 1 und 100 eingeben: ");
    try {
        zahl = sc.nextInt();
        if (zahl >= 1 && zahl <= 100) {
            gueltig = true;
        } else {
            System.out.println("Zahl muss zwischen 1 und 100 liegen!");
        }
    } catch (InputMismatchException e) {
        System.out.println("Ungültige Eingabe. Bitte eine ganze Zahl eingeben.");
        sc.nextLine(); // Ungültige Eingabe aus dem Puffer entfernen
    }
}

System.out.println("Eingegebene Zahl: " + zahl);
```

### 9.5 java.io.Console – Sichere Konsoleneingabe [Fortgeschritten]

Die Klasse `java.io.Console` bietet eine direkte Schnittstelle zur Systemkonsole und ist der einzige standardmäßige Weg in Java, Passwörter **ohne Echo** einzulesen. Im Gegensatz zu `Scanner` ist `Console` auf eine echte Terminal-Verbindung angewiesen – in IDEs und bei Umleitungen über Pipes gibt `System.console()` `null` zurück. `Console` wird über `System.console()` bezogen (kein `new`-Aufruf möglich).

```java
import java.io.Console;

public class ConsoleDemo {
    public static void main(String[] args) {

        Console console = System.console();

        if (console == null) {
            System.err.println("Kein Terminal verfügbar (IDE oder Pipe).");
            return;
        }

        // Normale Eingabe – readLine() gibt null zurück wenn EOF erreicht
        String benutzername = console.readLine("Benutzername: ");

        // Passwort-Eingabe – Echo ist unterdrückt; gibt char[] zurück
        char[] passwort = console.readPassword("Passwort: ");

        console.printf("Anmeldung für: %s%n", benutzername);

        // char[] nach Verwendung sofort überschreiben (Sicherheit!)
        java.util.Arrays.fill(passwort, '\0');
    }
}
```

#### Formatierte Ausgabe mit Console

```java
Console console = System.console();
if (console != null) {
    // printf/format – wie System.out.printf
    console.printf("%-15s %5d EUR%n", "Gehalt:", 3500);

    // writer() liefert einen PrintWriter für weitere Operationen
    java.io.PrintWriter writer = console.writer();
    writer.println("Log-Meldung über Console.writer()");

    // reader() liefert einen BufferedReader
    java.io.BufferedReader reader = console.reader();
}
```

#### readLine() mit Format-String

```java
Console console = System.console();
if (console != null) {
    // Format-String direkt in readLine() – Eingabeaufforderung formatieren
    String stadt = console.readLine("Stadt [%s]: ", "Berlin");
    int alter    = Integer.parseInt(
                       console.readLine("Alter (1-120): "));

    // readPassword mit Format-String
    char[] pin = console.readPassword("PIN für Konto %d: ", 12345);
    java.util.Arrays.fill(pin, '\0');
}
```

#### Console vs. Scanner – Vergleich

| Merkmal                      | `Scanner(System.in)`             | `java.io.Console`                     |
|------------------------------|----------------------------------|---------------------------------------|
| Passwort ohne Echo           | Nicht möglich                    | `readPassword()` unterdrückt Echo     |
| Rückgabetyp Passwort         | `String` (bleibt im Heap)        | `char[]` (kann überschrieben werden)  |
| Verfügbarkeit in IDEs        | Immer verfügbar                  | Gibt `null` zurück (kein Terminal)    |
| Erhalt über                  | `new Scanner(System.in)`         | `System.console()` (Singleton)        |
| Typ-sichere Eingabe          | `nextInt()`, `nextDouble()` usw. | Nur `readLine()` / `readPassword()`   |
| Thread-Sicherheit            | Nicht thread-safe                | Thread-safe                           |
| Flush nach Prompt            | Manuell nötig                    | Automatisch                           |

> **Prüfungstipp:** `System.console()` kann `null` zurückgeben – immer auf `null` prüfen! Für Passwörter ist `char[]` sicherer als `String`, weil Arrays im Heap überschrieben werden können, `String`-Objekte hingegen immutable im String-Pool verbleiben können.

---

## 10. Try-with-Resources und AutoCloseable

Alle IO-Klassen implementieren `Closeable` (oder `AutoCloseable`) und sollten mit **try-with-resources** verwendet werden.

### 10.1 Grundprinzip

```java
// Ohne try-with-resources (fehleranfällig!)
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("datei.txt"));
    System.out.println(br.readLine());
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (br != null) {
        try { br.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}

// Mit try-with-resources (empfohlen!)
try (BufferedReader br = new BufferedReader(new FileReader("datei.txt"))) {
    System.out.println(br.readLine());
} catch (IOException e) {
    e.printStackTrace();
}
// br.close() wird automatisch aufgerufen – auch im Fehlerfall!
```

### 10.2 Mehrere Ressourcen

```java
// Mehrere Ressourcen werden in umgekehrter Reihenfolge geschlossen
try (
    BufferedReader ein  = new BufferedReader(new FileReader("quelle.txt"));
    BufferedWriter aus  = new BufferedWriter(new FileWriter("ziel.txt"))
) {
    String zeile;
    while ((zeile = ein.readLine()) != null) {
        aus.write(zeile.toUpperCase());
        aus.newLine();
    }
} // aus.close() zuerst, dann ein.close()
```

---

## 11. Vollständiges Praxisbeispiel – Log-Datei-Analyse

```java
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

public class LogAnalyse {

    record LogEintrag(String level, String nachricht) {}

    public static void main(String[] args) throws IOException {

        Path logPfad    = Path.of("application.log");
        Path ergebnisPfad = Path.of("fehler_bericht.txt");

        // Logdatei erstellen (Demo)
        Files.writeString(logPfad,
            "INFO  Server gestartet\n" +
            "DEBUG Verbindung hergestellt\n" +
            "ERROR NullPointerException in Service\n" +
            "WARN  Speicher unter 20%\n" +
            "ERROR FileNotFoundException: config.xml\n" +
            "INFO  Anfrage verarbeitet\n",
            StandardCharsets.UTF_8);

        // Einträge parsen
        List<LogEintrag> eintraege;
        try (var stream = Files.lines(logPfad, StandardCharsets.UTF_8)) {
            eintraege = stream
                .filter(z -> !z.isBlank())
                .map(z -> {
                    String[] teile = z.split("\\s+", 2);
                    return new LogEintrag(
                        teile[0],
                        teile.length > 1 ? teile[1] : "");
                })
                .collect(Collectors.toList());
        }

        // Statistik ausgeben
        Map<String, Long> statistik = eintraege.stream()
            .collect(Collectors.groupingBy(
                LogEintrag::level, Collectors.counting()));

        System.out.println("=== Log-Statistik ===");
        statistik.forEach((level, anzahl) ->
            System.out.printf("%-6s : %d%n", level, anzahl));

        // Fehler in Berichtsdatei schreiben
        List<String> fehlerZeilen = eintraege.stream()
            .filter(e -> "ERROR".equals(e.level()))
            .map(e -> "[FEHLER] " + e.nachricht())
            .collect(Collectors.toList());

        Files.write(ergebnisPfad, fehlerZeilen, StandardCharsets.UTF_8);
        System.out.println("\nFehler-Bericht geschrieben: " + ergebnisPfad);
        System.out.println("Inhalt:");
        Files.lines(ergebnisPfad).forEach(System.out::println);
    }
}
```

---

## 12. Häufige Fehler und Best Practices

### 12.1 Typische Fehler

```java
// FALSCH: Stream wird nicht geschlossen!
Files.list(Path.of(".")).forEach(System.out::println);

// RICHTIG: Stream in try-with-resources
try (var stream = Files.list(Path.of("."))) {
    stream.forEach(System.out::println);
}

// FALSCH: Charset nicht angegeben (plattformabhängig!)
BufferedReader br = new BufferedReader(new FileReader("datei.txt"));

// RICHTIG: Charset explizit angeben
BufferedReader br = new BufferedReader(
    new InputStreamReader(new FileInputStream("datei.txt"),
        StandardCharsets.UTF_8));

// MODERNER: Files-API
List<String> zeilen = Files.readAllLines(Path.of("datei.txt"),
    StandardCharsets.UTF_8);
```

### 12.2 Best Practices

| Empfehlung                           | Begründung                                              |
|--------------------------------------|---------------------------------------------------------|
| Immer try-with-resources verwenden   | Ressourcen werden garantiert geschlossen                |
| Charset explizit angeben             | Vermeidet plattformabhängige Encoding-Fehler            |
| `Files`-API statt `File` bevorzugen  | Bessere Fehlerbehandlung, mehr Funktionen               |
| `BufferedReader/Writer` verwenden    | Deutlich bessere Performance durch Pufferung            |
| `Path.of()` statt `new File()`       | Moderner, typsicherer, plattformunabhängig              |
| Große Dateien mit Streams verarbeiten | `Files.lines()` ist lazy – kein Speicherproblem        |
| `serialVersionUID` setzen            | Verhindert unerwartete Inkompatibilitäten               |
| Niemals blindes Deserialisieren      | Sicherheitsrisiko: Angreifer können Code einschleusen   |

---

## 13. Zusammenfassung

| Aufgabe                      | Empfohlene Lösung                         |
|------------------------------|-------------------------------------------|
| Kleine Textdatei lesen       | `Files.readString()` / `readAllLines()`   |
| Große Textdatei lesen        | `Files.lines()` (lazy Stream)             |
| Textdatei schreiben          | `Files.writeString()` / `Files.write()`   |
| Binärdaten lesen/schreiben   | `FileInputStream` / `FileOutputStream`    |
| Verzeichnis auflisten        | `Files.list()` (try-with-resources!)      |
| Rekursiv traversieren        | `Files.walk()` / `Files.find()`           |
| Objekte persistieren         | `ObjectOutputStream` / `ObjectInputStream`|
| Konsoleneingabe (einfach)    | `Scanner` mit `System.in`                 |
| Konsoleneingabe (sicher)     | `System.console()` / `Console.readPassword()` |
| Datei-Metadaten              | `Files.readAttributes()`                  |
| Dateiänderungen beobachten   | `WatchService`                            |

---

## 14. Multiple-Choice-Fragen

**Frage 1:** Welche Aussage zu `System.console()` ist korrekt?

- A) `System.console()` erzeugt immer ein gültiges `Console`-Objekt.
- B) `Console` kann mit `new Console()` instanziiert werden.
- **C) `System.console()` gibt `null` zurück, wenn kein Terminal verfügbar ist (z. B. in IDEs oder bei I/O-Umleitung).** ✓
- D) `Console` ist ein Interface im Paket `java.io`.

**Frage 2:** Welchen Rückgabetyp hat `Console.readPassword()`?

- A) `String`
- **B) `char[]`** ✓
- C) `byte[]`
- D) `StringBuilder`

**Frage 3:** Warum ist `Console.readPassword()` sicherer als das Einlesen eines Passworts mit `Scanner`?

- A) `readPassword()` verschlüsselt die Eingabe automatisch mit AES.
- B) `Scanner` kann keine Passwörter einlesen.
- C) `readPassword()` speichert die Eingabe im SecureRandom-Pool.
- **D) Der Rückgabetyp `char[]` kann nach der Verwendung im Speicher überschrieben werden; ein `String` ist immutable und bleibt im Heap.** ✓

**Frage 4:** Welche Methode der Klasse `Console` liest eine Zeile Text von der Konsole?

- A) `Console.readLine()` – sie ist eine statische Methode.
- **B) `console.readLine(String fmt, Object... args)` – Instanzmethode mit optionalem Format-String.** ✓
- C) `console.nextLine()`
- D) `Console.read()`

**Frage 5:** Wie erhält man eine `Console`-Instanz in Java?

- A) `Console c = new Console();`
- B) `Console c = Console.getInstance();`
- **C) `Console c = System.console();`** ✓
- D) `Console c = System.getConsole();`

**Frage 6:** Ein Programm wird in einer IDE gestartet, die keine echte Terminalverbindung bereitstellt. Was gibt `System.console()` zurück?

- **A) `null`** ✓
- B) Eine `Console`-Instanz, die auf `System.in`/`System.out` umleitet.
- C) Eine leere `Console`-Instanz.
- D) Es wird eine `ConsoleNotFoundException` geworfen.

---

## 15. Skill Check: IO API

Stellen Sie sicher, dass Sie folgende Aufgaben lösen können (Ziel: ≥ 80 %):

- [ ] Byte-Streams von Character-Streams unterscheiden und die richtigen Klassen benennen.
- [ ] `FileInputStream` / `FileOutputStream` und `BufferedReader` / `BufferedWriter` korrekt mit try-with-resources verwenden.
- [ ] `Path.of()`, `Files.readAllLines()`, `Files.writeString()` und `Files.lines()` anwenden.
- [ ] `StandardOpenOption`-Werte (`APPEND`, `CREATE_NEW`, `TRUNCATE_EXISTING`) erklären.
- [ ] Ein Verzeichnis rekursiv mit `Files.walk()` traversieren.
- [ ] Ein Java-Objekt mit `ObjectOutputStream` serialisieren und mit `ObjectInputStream` deserialisieren.
- [ ] `transient` und `serialVersionUID` im Kontext der Serialisierung erklären.
- [ ] `Scanner` für Konsoleneingaben und Dateien einsetzen, inklusive Eingabevalidierung.
- [ ] `System.console()` aufrufen, auf `null` prüfen und `Console.readLine()` sowie `Console.readPassword()` verwenden.
- [ ] Den Unterschied zwischen `String`-Rückgabe (Scanner) und `char[]`-Rückgabe (`readPassword`) im Sicherheitskontext erläutern.
- [ ] Erklären, warum `System.console()` in IDEs `null` zurückgibt.
- [ ] `WatchService` für Dateiänderungs-Benachrichtigungen einsetzen.
