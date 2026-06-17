# Modul 1: Introduction to Java

## Übersicht

Dieses Modul legt das Fundament für die gesamte Java-Schulung. Es beginnt mit der Geschichte von Java, erklärt die Kernkonzepte der objektorientierten Programmierung und führt durch das Erstellen, Kompilieren und Ausführen eines ersten Java-Programms.

| Thema | Dauer |
|---|---|
| Course Overview | 6m |
| Java History | 4m |
| Explain Java Features and Object-Oriented Concepts | 30m |
| Define a Java Program | 13m |
| Compile and Execute your first Java Program | 10m |
| Code snippets and Summary | 4m |
| Practice 1-1: Verify the JDK Installation | 5m |
| Practice 1-2: Create, Compile, and Execute a Java Application | 10m |
| Skill Check: Introduction to Java (mind. 80%) | — |

---

## 1. Java History

Java wurde 1991 von James Gosling bei Sun Microsystems unter dem Namen "Oak" entwickelt, ursprünglich für eingebettete Systeme. 1995 wurde es unter dem Namen **Java** veröffentlicht, mit dem Leitspruch:

> **"Write Once, Run Anywhere"** (WORA)

### Wichtige Meilensteine

| Jahr | Ereignis |
|---|---|
| 1991 | Projekt "Oak" bei Sun Microsystems |
| 1995 | Veröffentlichung von Java 1.0 |
| 1998 | Java 2 (J2SE, J2EE, J2ME) |
| 2006 | Sun stellt Java als Open Source bereit (OpenJDK) |
| 2010 | Oracle übernimmt Sun Microsystems |
| 2017 | Neuer Release-Zyklus: alle 6 Monate eine neue Version |
| 2021 | **Java 17 (LTS)**: Sealed Classes, Text Blocks, Records, Pattern Matching für `instanceof` — alles produktionsreif |
| 2023 | **Java 21 (LTS)**: Pattern Matching für Switch, Virtual Threads, Record Patterns, Sequenced Collections |
| 2025 | **Java 25 (LTS)** — Grundlage dieser Schulung: Instance Main Methods, Flexible Constructors, Module Import Declarations |

**LTS** = Long-Term Support. Diese Versionen erhalten mehrjährigen Support (mind. 8 Jahre) und sind für Produktivsysteme empfohlen. Zwischen LTS-Versionen erscheinen alle 6 Monate Non-LTS-Releases, in denen neue Features oft als Preview eingeführt werden.

### Was brachten die LTS-Versionen? (17 / 21 / 25)

| Version | Jahr | Wichtigste Neuerungen für Entwickler |
|---|---|---|
| **Java 17 LTS** `[Java 17]` | 2021 | Sealed Classes, Records (stabil seit 16), Text Blocks (stabil seit 15), Pattern Matching `instanceof` (stabil seit 16) |
| **Java 21 LTS** `[Java 21]` | 2023 | Pattern Matching für Switch (mit `when`), Virtual Threads, Record Patterns, Sequenced Collections |
| **Java 25 LTS** `[Java 25]` | 2025 | Instance Main Methods (`void main()`), Flexible Constructor Bodies, Module Import Declarations, Scoped Values |

---

## 2. Java Features und objektorientierte Konzepte

### Kernmerkmale von Java

**Plattformunabhängigkeit**
Java-Quellcode wird in **Bytecode** kompiliert (`.class`-Dateien). Die **Java Virtual Machine (JVM)** interpretiert diesen Bytecode auf jedem Betriebssystem. Das macht Java plattformunabhängig.

```
Quellcode (.java) → javac → Bytecode (.class) → JVM → Ausführung
```

**Objektorientierung**
Alles in Java ist ein Objekt (mit Ausnahme primitiver Typen). Das OOP-Paradigma basiert auf vier Säulen:

| Säule | Bedeutung |
|---|---|
| **Encapsulation** (Kapselung) | Daten und Methoden werden in einer Klasse gebündelt; interner Zustand ist nach außen verborgen |
| **Inheritance** (Vererbung) | Klassen können Eigenschaften und Methoden anderer Klassen übernehmen (`extends`) |
| **Polymorphism** (Polymorphismus) | Objekte können in verschiedenen Formen auftreten; Methoden verhalten sich je nach Objekt unterschiedlich |
| **Abstraction** (Abstraktion) | Komplexe Details werden versteckt, nur das Wesentliche wird nach außen sichtbar gemacht |

**Weitere Java-Eigenschaften**
- **Strongly typed**: Jede Variable muss einen deklarierten Typ haben
- **Automatic Memory Management**: Der Garbage Collector gibt nicht mehr benötigten Speicher automatisch frei
- **Multithreading**: Eingebaut in die Sprache und die JVM
- **Sicherheit**: Sandbox-Modell der JVM, kein direkter Speicherzugriff
- **Robustheit**: Exception Handling, strenge Typisierung verhindern viele Laufzeitfehler

---

## 3. Ein Java-Programm definieren

### Grundstruktur

```java
// Dateiname: HelloWorld.java
public class HelloWorld {                     // Klasse (Dateiname = Klassenname)

    public static void main(String[] args) {  // Einstiegspunkt jedes Java-Programms
        System.out.println("Hello, World!");  // Ausgabe auf der Konsole
    }
}
```

### Wichtige Regeln

- **Eine public Klasse pro Datei**: Der Dateiname muss exakt dem Klassennamen entsprechen (inkl. Groß-/Kleinschreibung)
- **`main`-Methode**: Ist der Einstiegspunkt; ohne sie kann das Programm nicht gestartet werden
- **`public static void main(String[] args)`**: Klassische Signatur; ab Java 25 auch vereinfachte Formen möglich (siehe unten)
- **Semikolon**: Jede Anweisung wird mit `;` abgeschlossen
- **Geschweifte Klammern `{}`**: Definieren Blöcke (Klassen, Methoden, Kontrollstrukturen)

### Vereinfachte main-Methode `[Java 25]`

Ab Java 25 ist die `main`-Methode wesentlich flexibler (JEP 495 — Instance Main Methods):

```java
// Minimale Form: kein public, kein static, kein String[] args
class HelloWorld {
    void main() {
        System.out.println("Hello, World!");
    }
}

// Noch kürzer: Implizite Klasse — keine Klassen-Deklaration nötig
void main() {
    System.out.println("Hello, World!");
}

// Alle Varianten sind gültig (Java wählt nach dieser Priorität):
static void main(String[] args) { ... }
static void main()              { ... }
void main(String[] args)        { ... }
void main()                     { ... }  // niedrigste Priorität
```

Diese Vereinfachung senkt die Einstiegshürde — Einsteiger müssen nicht sofort `public`, `static` und `String[]` verstehen. Für produktive Projekte bleibt die klassische Form üblich.

### Package-Deklaration

```java
package com.example.myapp;  // Optional, aber Best Practice

public class MyClass {
    // ...
}
```

Packages organisieren Klassen in Namensräumen und verhindern Namenskonflikte.

### Import-Anweisungen

```java
import java.util.ArrayList;     // Einzelne Klasse importieren
import java.util.*;             // Alle Klassen eines Packages importieren
```

---

## 4. Kompilieren und Ausführen

### Werkzeuge

Das **JDK (Java Development Kit)** enthält alles, was zum Entwickeln und Ausführen von Java-Programmen benötigt wird:

| Tool | Beschreibung |
|---|---|
| `javac` | Java Compiler — übersetzt `.java` in `.class` |
| `java` | JVM-Starter — führt `.class`-Dateien aus |
| `javadoc` | Generiert HTML-Dokumentation aus Kommentaren |
| `jar` | Erstellt und verwaltet JAR-Archivdateien |
| `jshell` | Interaktive Java-Shell (REPL) |

### Ablauf

```bash
# 1. Quellcode kompilieren
javac HelloWorld.java
# → erstellt HelloWorld.class

# 2. Programm ausführen
java HelloWorld
# Ausgabe: Hello, World!
```

### Häufige Compiler-Fehler

| Fehler | Ursache |
|---|---|
| `cannot find symbol` | Variable oder Methode nicht deklariert oder falsch geschrieben |
| `class X is public, should be declared in a file named X.java` | Dateiname stimmt nicht mit Klassenname überein |
| `reached end of file while parsing` | Fehlende schließende Klammer `}` |
| `';' expected` | Fehlendes Semikolon am Ende einer Anweisung |

---

## 5. Kommentare und Code-Konventionen

### Kommentartypen

```java
// Einzeiliger Kommentar

/* Mehrzeiliger
   Kommentar */

/**
 * Javadoc-Kommentar — wird für API-Dokumentation verwendet.
 * @param args Kommandozeilenargumente
 * @return void
 */
public static void main(String[] args) { ... }
```

### Java Code Conventions (Namensgebung)

| Element | Konvention | Beispiel |
|---|---|---|
| Klasse | PascalCase | `CustomerOrder` |
| Methode | camelCase | `calculateTotal()` |
| Variable | camelCase | `firstName` |
| Konstante | UPPER_SNAKE_CASE | `MAX_SIZE` |
| Package | alles klein, Domain-umgekehrt | `com.example.shop` |

---

## Übungsaufgaben

### Practice 1-1: Verify the JDK Installation (ca. 5 Minuten)

**Ziel:** Sicherstellen, dass das JDK korrekt installiert und konfiguriert ist.

**Aufgaben:**
1. Öffne ein Terminal (CMD, PowerShell oder bash).
2. Prüfe die installierte Java-Version:
   ```bash
   java -version
   ```
   Erwartete Ausgabe: Versionsnummer 25.x.x
3. Prüfe den Compiler:
   ```bash
   javac -version
   ```
4. Zeige alle verfügbaren JVM-Optionen:
   ```bash
   java -help
   ```
5. Stelle sicher, dass die Umgebungsvariable `JAVA_HOME` gesetzt ist:
   ```bash
   # Windows
   echo %JAVA_HOME%
   
   # Linux/macOS
   echo $JAVA_HOME
   ```

**Erwartetes Ergebnis:** Alle Befehle laufen ohne Fehler durch, die Java-Version wird korrekt angezeigt.

---

### Practice 1-2: Create, Compile, and Execute a Java Application (ca. 10 Minuten)

**Ziel:** Eine erste eigene Java-Anwendung schreiben, kompilieren und ausführen.

**Aufgaben:**

1. Erstelle einen Ordner `exercise01` und darin die Datei `HelloWorld.java`.

2. Schreibe folgendes Programm:
   ```java
   public class HelloWorld {
       public static void main(String[] args) {
           System.out.println("Hello, World!");
           System.out.println("Java SE 25 – hier starte ich!");
       }
   }
   ```

3. Kompiliere die Datei:
   ```bash
   javac HelloWorld.java
   ```
   Prüfe: Wurde eine `HelloWorld.class`-Datei erstellt?

4. Führe das Programm aus:
   ```bash
   java HelloWorld
   ```

5. **Erweiterung:** Füge eine weitere Ausgabe hinzu, die deinen Namen enthält:
   ```java
   System.out.println("Programmiert von: Max Mustermann");
   ```

6. **Bonus:** Übergib deinen Namen als Kommandozeilenargument und gib ihn aus:
   ```java
   public class HelloWorld {
       public static void main(String[] args) {
           if (args.length > 0) {
               System.out.println("Hallo, " + args[0] + "!");
           } else {
               System.out.println("Hallo, Welt!");
           }
       }
   }
   ```
   Ausführung: `java HelloWorld Anna`

**Erwartetes Ergebnis:** Das Programm gibt die Begrüßungsnachricht(en) korrekt auf der Konsole aus.

---

## Multiple-Choice-Fragen

**Frage 1:** Was bedeutet der Begriff "Bytecode" in Java?

- A) Maschinencode, der direkt von der CPU ausgeführt wird
- B) Quellcode, der für Menschen lesbar ist
- C) **Plattformunabhängiger Zwischencode, den die JVM ausführt** ✓
- D) Komprimierter Java-Quellcode

---

**Frage 2:** Welcher Befehl kompiliert `HelloWorld.java`?

- A) `java HelloWorld.java`
- B) **`javac HelloWorld.java`** ✓
- C) `compile HelloWorld.java`
- D) `jshell HelloWorld.java`

---

**Frage 3:** Eine Klasse `Rechner` soll als public deklariert werden. Wie muss die Datei heißen?

- A) `rechner.java`
- B) `RECHNER.java`
- C) beliebig, der Name spielt keine Rolle
- D) **`Rechner.java`** ✓

---

**Frage 4:** Welche OOP-Säule beschreibt das Verbergen des internen Zustands einer Klasse?

- A) Vererbung
- B) Polymorphismus
- C) **Kapselung** ✓
- D) Abstraktion

> *Kapselung (Encapsulation) bündelt Daten und Methoden und schützt den internen Zustand vor direktem Außenzugriff. Abstraktion hingegen vereinfacht komplexe Systeme durch das Ausblenden von Implementierungsdetails.*

---

**Frage 5:** Was ist der Unterschied zwischen JDK und JRE?

- A) JRE enthält den Compiler, JDK nur die Laufzeitumgebung
- B) **JDK enthält Compiler und Entwicklerwerkzeuge; JRE reicht nur zum Ausführen** ✓
- C) JDK ist für Server, JRE für Desktops
- D) Beide sind identisch, nur unterschiedliche Namen

---

**Frage 6:** Was ist JShell? `[Java 9]`

- A) Ein Build-Tool wie Maven
- B) Eine IDE für Java
- C) Ein Debugger für Java-Anwendungen
- D) **Eine interaktive REPL-Umgebung zum Ausprobieren von Java-Code ohne Kompilierung** ✓

---

**Frage 7:** Was hat Java 17 LTS als wichtigste eigene Neuerung eingeführt? `[Java 17]`

- A) Switch Expressions
- B) Text Blocks
- C) **Sealed Classes** ✓
- D) Records

> *Text Blocks (Java 15), Switch Expressions (Java 14) und Records (Java 16) wurden vor Java 17 eingeführt. Java 17 LTS machte sie offiziell produktionsreif und führte Sealed Classes ein.*

---

**Frage 8:** Welche main-Methoden-Form ist ab Java 25 gültig? `[Java 25]`

- A) Nur `public static void main(String[] args)`
- B) Nur `static void main()`
- C) Nur `void main()`
- D) **Alle vier Formen sind gültig — Java wählt nach Priorität** ✓

---

## Skill Check: Typische Prüfungsfragen

1. Was bedeutet "Write Once, Run Anywhere" in Bezug auf Java?
2. Was ist der Unterschied zwischen JDK, JRE und JVM?
3. Welcher Befehl kompiliert eine Java-Datei? Welcher führt sie aus?
4. Welche vier Säulen der OOP kennt Java?
5. Warum muss der Dateiname einer public Klasse dem Klassennamen entsprechen?
6. Was ist der Zweck des `main`-Methods in Java?
7. Was ist ein Package und warum wird es verwendet?
