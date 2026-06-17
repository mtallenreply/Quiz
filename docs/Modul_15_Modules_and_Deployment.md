# Modul 15: Modules and Deployment

## Übersicht

Dieses Modul behandelt das **Java Platform Module System (JPMS)**, das mit Java 9 eingeführt wurde. Sie lernen, Module zu definieren, den Modulpfad zu nutzen, Abhängigkeiten mit `jdeps` zu analysieren und mit `jlink` sowie `jpackage` eigenständige Deployments zu erstellen.

| Thema                        | Dauer |
|------------------------------|-------|
| Classpath Problems           | 13 m  |
| Module Types                 | 13 m  |
| module-info.java             |  4 m  |
| Module Directives            |  8 m  |
| Compiling Modules            | 11 m  |
| jdeps and jmod               | 11 m  |
| jlink                        | 23 m  |
| jpackage                     | 13 m  |
| Practice 15-1                | 36 m  |
| Practice 15-2                | 45 m  |
| **Skill Check: Modules**     | **mind. 80 %** |

---

## 1. Das Classpath-Problem – Warum JPMS?

### 1.1 Probleme vor Java 9

Vor der Einführung des Modulsystems gab es strukturelle Schwächen in der Java-Plattform:

```
Klasspfad-Probleme (pre-Java 9):
├── JAR Hell
│   ├── Keine Versionsangaben in JAR-Metadaten
│   ├── Konflikte bei gleichnamigen Klassen (shadowing)
│   └── Mehrere Versionen derselben Bibliothek unmöglich
├── Fehlende Kapselung
│   ├── Alle public-Klassen weltweit sichtbar
│   ├── Interne JDK-APIs (sun.misc.Unsafe) nutzbar
│   └── Keine erzwungenen Grenzen zwischen Bibliotheken
└── Aufblähung
    ├── JRE enthielt immer alle ~4.000 JDK-Klassen
    └── Kein Trimmen für kleine Geräte / Container
```

### 1.2 Was JPMS löst

```java
// Vor Java 9: Alles sichtbar, Abhängigkeiten implizit
import sun.misc.Unsafe;    // interne API – jetzt verboten!
import com.sun.xml.internal.ws.api.server.Container; // JDK-intern

// Mit JPMS: Explizite Abhängigkeiten, starke Kapselung
// module-info.java erzwingt:
// - Welche Pakete exportiert werden
// - Welche Module benötigt werden
// - Welche Pakete für Reflection offen sind
```

### 1.3 Classpath vs. Modulpfad

| Merkmal                   | Classpath (vor Java 9)              | Modulpfad (JPMS)                      |
|---------------------------|-------------------------------------|---------------------------------------|
| Kapselung                 | Keine (alle public sichtbar)        | Stark (nur exportierte Pakete)        |
| Zirkuläre Abhängigkeiten  | Erlaubt (und häufig)                | Verboten (bei compile-time)           |
| Versionen                 | Keine Unterstützung                 | Nicht direkt, aber isoliert           |
| Startzeit                 | Alles geladen                       | Nur benötigte Module                  |
| Fehler bei Start          | ClassNotFoundException zur Laufzeit | Fehlende Module beim Start erkannt    |

---

## 2. Module-Typen

### 2.1 Übersicht der vier Typen

```
Java Module
├── Named Module
│   ├── Explizit benannt in module-info.java
│   ├── Liegt auf dem Modulpfad
│   └── Hat starke Kapselung
├── Automatic Module
│   ├── JAR auf dem Modulpfad OHNE module-info.java
│   ├── Name = JAR-Dateiname (Bindestriche → Punkte)
│   └── Exportiert alle Pakete, requires alle anderen Module
├── Unnamed Module
│   ├── JARs auf dem Classpath
│   ├── Kann alle anderen Packages sehen
│   └── Kann von Named Modules NICHT required werden
└── Platform Module
    ├── JDK-eigene Module (java.base, java.sql, etc.)
    └── Immer verfügbar
```

### 2.2 Named Module

```java
// Projektstruktur
// com.beispiel.app/
// ├── module-info.java
// └── src/com/beispiel/app/
//     ├── Main.java
//     └── Service.java

// module-info.java
module com.beispiel.app {
    requires java.base;     // implizit vorhanden (muss nicht angegeben werden)
    requires java.logging;
    requires com.beispiel.lib;  // eigenes Modul
}
```

### 2.3 Automatic Module

```java
// jackson-databind-2.15.jar auf dem Modulpfad
// → Automatisches Modul: Name = "jackson.databind"
// → Alle Pakete exportiert
// → Kann in module-info.java mit requires verwendet werden

module com.meinprojekt {
    requires jackson.databind; // Automatisches Modul
}

// Besser: Bibliothek gibt eigenen Modulnamen an
// META-INF/MANIFEST.MF:
// Automatic-Module-Name: com.fasterxml.jackson.databind
```

### 2.4 Plattform-Module

```bash
# Alle JDK-Module anzeigen
java --list-modules

# Ausgabe (Auswahl):
# java.base@25
# java.sql@25
# java.desktop@25
# java.logging@25
# jdk.compiler@25
# jdk.jlink@25

# Modul-Inhalt anzeigen
java --describe-module java.sql
```

---

## 3. module-info.java – Die Modul-Deskriptor-Datei

### 3.1 Grundstruktur

```java
// Datei: src/module-info.java (im Source-Root, nicht in einem Paket!)
module com.beispiel.app {
    // Direktiven hier
}
```

### 3.2 Namenskonventionen

```
Modul-Namensregeln:
├── Umgekehrte Domain-Notation (wie Pakete): com.firma.modul
├── Kleinbuchstaben
├── Punkte als Trennzeichen (keine Bindestriche!)
├── Sollte mindestens das erste Paket enthalten
└── Eindeutig im System (kein globales Register)
```

---

## 4. Modul-Direktiven

### 4.1 requires

```java
module com.shop.app {
    // Normales requires: Compile-Zeit und Laufzeit
    requires java.sql;
    requires com.shop.domain;

    // requires transitive: Abhängigkeit wird weitergegeben (re-exportiert)
    // Wenn A requires transitive B, dann sehen Nutzer von A auch B
    requires transitive com.shop.api;

    // requires static: Nur zur Compile-Zeit (Laufzeit optional)
    // Nützlich für Annotations-Prozessoren
    requires static com.google.auto.service;
}
```

### 4.2 exports

```java
module com.shop.domain {
    // Paket für alle anderen Module exportieren
    exports com.shop.domain.model;

    // Paket nur für spezifisches Modul exportieren (qualifiziertes Export)
    exports com.shop.domain.internal to com.shop.app;
    exports com.shop.domain.spi     to com.shop.app, com.shop.admin;

    // NICHT exportierte Pakete: stark gekapselt
    // com.shop.domain.impl – nicht sichtbar von außen!
}
```

### 4.3 opens (für Reflection)

```java
module com.shop.persistence {
    // opens: Paket für Deep Reflection freigeben
    // (Frameworks wie Hibernate, Jackson benötigen das)
    opens com.shop.persistence.entities;

    // Qualifiziertes opens: nur für bestimmte Module
    opens com.shop.persistence.entities to com.fasterxml.jackson.databind;

    // Unterschied exports vs. opens:
    // exports: Compile-Time + normaler Zugriff
    // opens:   nur Laufzeit-Reflection (private Felder zugänglich)
}
```

### 4.4 uses und provides (Service Loader)

```java
// Modul, das einen Service konsumiert
module com.shop.app {
    uses com.shop.api.PaymentService; // SPI-Interface
}

// Modul, das einen Service implementiert
module com.shop.paypal {
    requires com.shop.api;
    provides com.shop.api.PaymentService
        with com.shop.paypal.PayPalPaymentService;
}

// Verwendung im Code
ServiceLoader<PaymentService> loader =
    ServiceLoader.load(PaymentService.class);
loader.forEach(service ->
    System.out.println("Provider: " + service.getClass().getName()));
```

### 4.5 Alle Direktiven auf einen Blick

| Direktive                         | Zweck                                                     |
|-----------------------------------|-----------------------------------------------------------|
| `requires M`                      | Hängt von Modul M ab                                      |
| `requires transitive M`           | Re-exportiert Abhängigkeit M                              |
| `requires static M`               | Nur Compile-Zeit-Abhängigkeit                             |
| `exports P`                       | Paket P für alle Module sichtbar                          |
| `exports P to M`                  | Paket P nur für Modul M sichtbar                          |
| `opens P`                         | Paket P für Deep Reflection offen                         |
| `opens P to M`                    | Paket P nur für Modul M für Reflection offen              |
| `uses I`                          | Konsumiert Service-Interface I via ServiceLoader          |
| `provides I with C`               | Stellt Implementierung C für Interface I bereit           |

---

## 5. Module kompilieren und ausführen

### 5.1 Einfaches Ein-Modul-Projekt

```
Projektstruktur:
myapp/
├── src/
│   ├── module-info.java
│   └── com/beispiel/app/Main.java
└── mods/           (Ausgabeverzeichnis)
```

```bash
# Kompilieren
javac -d mods/com.beispiel.app \
      src/module-info.java \
      src/com/beispiel/app/Main.java

# Ausführen
java --module-path mods \
     --module com.beispiel.app/com.beispiel.app.Main
```

### 5.2 Mehr-Modul-Projekt

```
multi-module/
├── com.shop.api/
│   ├── module-info.java
│   └── src/com/shop/api/PaymentService.java
├── com.shop.domain/
│   ├── module-info.java
│   └── src/com/shop/domain/model/Product.java
└── com.shop.app/
    ├── module-info.java
    └── src/com/shop/app/Main.java
```

```bash
# Reihenfolge: Abhängigkeiten zuerst
javac -d mods/com.shop.api \
      com.shop.api/module-info.java \
      com.shop.api/src/com/shop/api/PaymentService.java

javac --module-path mods \
      -d mods/com.shop.domain \
      com.shop.domain/module-info.java \
      com.shop.domain/src/com/shop/domain/model/Product.java

javac --module-path mods \
      -d mods/com.shop.app \
      com.shop.app/module-info.java \
      com.shop.app/src/com/shop/app/Main.java

# Ausführen
java --module-path mods \
     --module com.shop.app/com.shop.app.Main
```

### 5.3 Modular JAR erstellen

```bash
# Modular JAR (enthält module-info.class)
jar --create \
    --file libs/com.shop.api.jar \
    --module-version 1.0 \
    -C mods/com.shop.api .

# Mit Main-Klasse
jar --create \
    --file libs/com.shop.app.jar \
    --main-class com.shop.app.Main \
    -C mods/com.shop.app .

# JAR-Inhalt prüfen
jar --describe-module --file libs/com.shop.api.jar
```

---

## 6. jdeps – Abhängigkeitsanalyse

### 6.1 Grundlegende Nutzung

```bash
# Abhängigkeiten einer Klasse analysieren
jdeps MyApplication.jar

# Ausgabe:
# MyApplication.jar -> java.base
# MyApplication.jar -> java.logging
#    com.beispiel  -> java.lang    java.base
#    com.beispiel  -> java.util    java.base

# Detaillierte Ausgabe (mit Paketen)
jdeps -verbose MyApplication.jar

# Nur Zusammenfassung
jdeps -summary MyApplication.jar
```

### 6.2 Auf JDK-interne APIs prüfen

```bash
# Findet Nutzung interner sun.*-APIs
jdeps --jdk-internals MyLegacyApp.jar

# Ausgabe wenn problematisch:
# MyLegacyApp.jar -> JDK removed internal API
#    com.legacy.util.FileUtil -> sun.misc.BASE64Encoder (JDK internal)

# Modulabhängigkeiten für jlink ermitteln
jdeps --print-module-deps MyApplication.jar
# Ausgabe: java.base,java.logging,java.sql
```

### 6.3 jdeps für Maven-Projekte

```bash
# JAR nach Kompilierung analysieren
jdeps --module-path target/dependency \
      --multi-release 25 \
      target/myapp-1.0.jar

# Alle Abhängigkeiten als Graph
jdeps --dot-output graphs target/myapp-1.0.jar
# Erzeugt .dot-Dateien für GraphViz
```

---

## 7. jmod – JMOD-Format

### 7.1 Was ist JMOD?

JMOD ist ein Containerformat (ähnlich JAR) für JDK-Module. Es kann **native Code**, **Konfigurationsdateien** und **Ressourcen** enthalten – Dinge, die JARs nicht können.

```bash
# JMOD-Datei erstellen
jmod create \
    --class-path mods/com.shop.app \
    --main-class com.shop.app.Main \
    jmods/com.shop.app.jmod

# Mit nativen Bibliotheken
jmod create \
    --class-path mods/com.mylib \
    --libs native-libs/ \
    --cmds bin/ \
    --config config/ \
    jmods/com.mylib.jmod

# JMOD-Inhalt anzeigen
jmod list com.shop.app.jmod
jmod describe com.shop.app.jmod

# JMOD-Inhalt extrahieren
jmod extract com.shop.app.jmod
```

| JAR                        | JMOD                               |
|----------------------------|------------------------------------|
| Laufzeit und Classpath     | Nur für jlink (kein Classpath)     |
| Keine nativen Bibliotheken | Native Bibliotheken möglich        |
| Weit verbreitet            | JDK-intern                         |
| Kleiner                    | Größer                             |

---

## 8. jlink – Custom JRE erstellen

### 8.1 Warum jlink?

```
Standard-JRE: ~200 MB (enthält alle JDK-Module)
Custom-JRE:   ~30–50 MB (nur benötigte Module)

Vorteile:
├── Kleineres Docker-Image
├── Schnellerer Start
├── Reduzierte Angriffsfläche
└── Keine JRE-Installation auf Zielmaschine nötig
```

### 8.2 Einfaches jlink-Beispiel

```bash
# 1. Benötigte Module ermitteln
jdeps --print-module-deps myapp.jar
# Ausgabe: java.base,java.logging

# 2. Custom JRE erstellen
jlink \
    --module-path $JAVA_HOME/jmods:jmods/ \
    --add-modules com.shop.app,java.base,java.logging \
    --output custom-jre \
    --launcher start=com.shop.app/com.shop.app.Main \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --strip-debug

# 3. Starten
custom-jre/bin/start
# oder
custom-jre/bin/java --module com.shop.app/com.shop.app.Main
```

### 8.3 jlink-Optionen im Detail

```bash
jlink \
    # Pfad zu .jmod-Dateien
    --module-path $JAVA_HOME/jmods:meine-jmods/ \

    # Welche Module einschließen
    --add-modules java.base,java.sql,com.mein.modul \

    # Ausgabeverzeichnis
    --output dist/meine-jre \

    # Startskript erstellen
    # Format: name=modul/hauptklasse
    --launcher meinprogramm=com.mein.modul/com.mein.Main \

    # Komprimierung: 0=keine, 1=konstante Strings, 2=ZIP
    --compress 2 \

    # Spart Speicher
    --no-header-files \
    --no-man-pages \
    --strip-debug \
    --strip-native-commands \

    # Service-Provider einschließen
    --include-locales de,en \

    # Größe anzeigen
    --verbose
```

### 8.4 Cross-Compilation (andere Plattform)

```bash
# JRE für Linux auf Windows erstellen
# Benötigt Linux-jmods

jlink \
    --module-path linux-jmods/:meine-jmods/ \
    --add-modules com.mein.modul \
    --output linux-jre
```

### 8.5 Größenvergleich

```
Standard JDK 25: ~340 MB
Standard JRE 25: ~190 MB
jlink (nur java.base): ~37 MB
jlink (java.base + java.sql + java.logging): ~55 MB
jlink (typische Anwendung): ~60–90 MB
```

---

## 9. jpackage – Native Installer erstellen

### 9.1 Was erzeugt jpackage?

```
Unterstützte Zielformate:
├── Windows: .exe (Installer), .msi (MSI-Paket)
├── macOS:   .dmg (Disk-Image), .pkg (Paket)
└── Linux:   .deb (Debian), .rpm (Red Hat)
```

### 9.2 Einfaches Beispiel

```bash
# Voraussetzung: modular JAR vorhanden

# Minimales Beispiel
jpackage \
    --type app-image \
    --name "MeineApp" \
    --module-path libs/:$JAVA_HOME/jmods \
    --module com.mein.modul/com.mein.Main \
    --app-version 1.0.0

# Windows-Installer
jpackage \
    --type msi \
    --name "ShopApp" \
    --vendor "Mein Unternehmen GmbH" \
    --description "Shop-Anwendung" \
    --module-path libs/:$JAVA_HOME/jmods \
    --module com.shop.app/com.shop.app.Main \
    --app-version 1.2.3 \
    --win-shortcut \
    --win-menu \
    --win-menu-group "Meine Apps" \
    --icon app.ico \
    --dest installer/

# macOS DMG
jpackage \
    --type dmg \
    --name "ShopApp" \
    --module-path libs/:$JAVA_HOME/jmods \
    --module com.shop.app/com.shop.app.Main \
    --mac-bundle-identifier com.shop.app \
    --icon app.icns
```

### 9.3 Für nicht-modulare Anwendungen (Fat-JAR)

```bash
# Nicht-modulare Anwendung mit Fat-JAR
jpackage \
    --type exe \
    --name "MeineApp" \
    --input ./app/ \
    --main-jar myapp-all.jar \
    --main-class com.mein.Main \
    --app-version 1.0 \
    --win-shortcut
```

### 9.4 jpackage vs. andere Ansätze

| Methode                | Vorteil                           | Nachteil                          |
|------------------------|-----------------------------------|-----------------------------------|
| `jpackage`             | JDK-eingebaut, plattformnativ     | Verschiedene Plattformen = mehrere Läufe |
| GraalVM native-image   | Kleinste Größe, schnellster Start | Kein JIT, lange Build-Zeit        |
| Docker/Container       | Plattformunabhängig               | Docker-Runtime nötig              |
| Fat-JAR                | Einfach                           | JRE-Installation nötig            |

---

## 10. Multi-Modul-Projekt mit Maven

### 10.1 Projektstruktur

```
shop-project/
├── pom.xml                          (Parent-POM)
├── com.shop.api/
│   ├── pom.xml
│   └── src/main/java/
│       ├── module-info.java
│       └── com/shop/api/PaymentService.java
├── com.shop.domain/
│   ├── pom.xml
│   └── src/main/java/
│       ├── module-info.java
│       └── com/shop/domain/model/Product.java
└── com.shop.app/
    ├── pom.xml
    └── src/main/java/
        ├── module-info.java
        └── com/shop/app/Main.java
```

### 10.2 Parent-POM

```xml
<!-- shop-project/pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.shop</groupId>
    <artifactId>shop-project</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <modules>
        <module>com.shop.api</module>
        <module>com.shop.domain</module>
        <module>com.shop.app</module>
    </modules>

    <properties>
        <java.version>25</java.version>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>25</release>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 10.3 Modul-POM (com.shop.api)

```xml
<!-- com.shop.api/pom.xml -->
<project>
    <parent>
        <groupId>com.shop</groupId>
        <artifactId>shop-project</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>com.shop.api</artifactId>
    <packaging>jar</packaging>
</project>
```

### 10.4 App-Modul-POM (com.shop.app)

```xml
<!-- com.shop.app/pom.xml -->
<project>
    <parent>
        <groupId>com.shop</groupId>
        <artifactId>shop-project</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>com.shop.app</artifactId>

    <dependencies>
        <dependency>
            <groupId>com.shop</groupId>
            <artifactId>com.shop.api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.shop</groupId>
            <artifactId>com.shop.domain</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

### 10.5 Module-Info-Dateien

```java
// com.shop.api/src/main/java/module-info.java
module com.shop.api {
    exports com.shop.api;
}

// com.shop.domain/src/main/java/module-info.java
module com.shop.domain {
    requires com.shop.api;
    exports com.shop.domain.model;
    // com.shop.domain.impl bleibt gekapselt
}

// com.shop.app/src/main/java/module-info.java
module com.shop.app {
    requires com.shop.api;
    requires com.shop.domain;
    requires java.logging;
}
```

---

## 11. Migration bestehender Anwendungen zu JPMS

### 11.1 Migrationsstrategien

```
Strategie 1: Bottom-up (von Bibliotheken nach oben)
├── Bibliotheken zuerst modularisieren
├── Dann Anwendungsmodule
└── Sicher, aber aufwändig

Strategie 2: Top-down (von Anwendung nach unten)
├── Anwendung als Named Module
├── Nicht-modularisierte Bibliotheken als Automatic Modules
└── Schneller, aber manche Bibliotheken bleiben automatisch

Strategie 3: Bottom + Classpath
├── Eigenen Code modularisieren
├── Bibliotheken auf Classpath belassen (Unnamed Module)
└── Kompromiss
```

### 11.2 Schrittweise Migration

```bash
# Schritt 1: Prüfen ob interne APIs genutzt werden
jdeps --jdk-internals myapp.jar

# Schritt 2: Abhängigkeiten analysieren
jdeps -summary myapp.jar

# Schritt 3: Problematische Bibliotheken identifizieren
jdeps --multi-release 25 \
      --module-path libs/ \
      myapp.jar

# Schritt 4: Temporär erlauben (Migration)
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-exports java.base/sun.misc=ALL-UNNAMED \
     -jar myapp.jar
```

### 11.3 Häufige Migrationsprobleme

```java
// Problem 1: Reflection auf private Felder (Hibernate, Jackson)
// Lösung: opens in module-info.java
module com.mein.modul {
    opens com.mein.entities to com.fasterxml.jackson.databind;
}

// Problem 2: ServiceLoader nutzen
// Alt:
ServiceLoader<SomeService> loader = ServiceLoader.load(SomeService.class);

// Neu: module-info.java anpassen
module com.mein.modul {
    uses com.mein.SomeService;
}

// Problem 3: Ressourcen aus anderen Modulen
// Nur möglich wenn Paket offen ist (opens) oder geexportet (exports)
InputStream is = getClass().getModule()
    .getResourceAsStream("com/mein/config.properties");
```

---

## 12. Vollständiges Beispiel – Hello-World-Modul

### 12.1 Code

```java
// src/module-info.java
module com.hallo {
    requires java.base;  // implizit – kann weggelassen werden
}

// src/com/hallo/Grueßer.java
package com.hallo;

public class Grueßer {
    public String grueße(String name) {
        return "Hallo, " + name + "! Von Modul: "
            + getClass().getModule().getName();
    }
}

// src/com/hallo/Main.java
package com.hallo;

public class Main {
    public static void main(String[] args) {
        Grueßer g = new Grueßer();
        System.out.println(g.grueße("Welt"));

        // Modul-Info
        Module m = Main.class.getModule();
        System.out.println("Modul-Name: " + m.getName());
        System.out.println("Benannte Pakete: " + m.getPackages());
    }
}
```

### 12.2 Build und Run

```bash
# Kompilieren
javac -d mods/com.hallo \
      src/module-info.java \
      src/com/hallo/Grueßer.java \
      src/com/hallo/Main.java

# Ausführen
java --module-path mods \
     --module com.hallo/com.hallo.Main

# Ausgabe:
# Hallo, Welt! Von Modul: com.hallo
# Modul-Name: com.hallo
# Benannte Pakete: [com.hallo]

# JAR erstellen
jar --create \
    --file com.hallo.jar \
    --main-class com.hallo.Main \
    -C mods/com.hallo .

# JAR ausführen
java --module-path . --module com.hallo

# Custom JRE
jlink \
    --module-path $JAVA_HOME/jmods:. \
    --add-modules com.hallo \
    --output hallo-jre \
    --launcher hallo=com.hallo/com.hallo.Main

hallo-jre/bin/hallo
```

---

## 13. Zusammenfassung

| Werkzeug       | Zweck                                        | Typischer Einsatz                         |
|----------------|----------------------------------------------|-------------------------------------------|
| `module-info.java` | Modul definieren, Abhängigkeiten deklarieren | In jedem benannten Modul                 |
| `javac`        | Module kompilieren                           | Mit `--module-path`                       |
| `java`         | Module ausführen                             | Mit `--module-path --module`              |
| `jar`          | Modulare JARs erstellen                      | Mit `--module-version`                    |
| `jdeps`        | Abhängigkeiten analysieren                   | Vor Migration, für jlink-Vorbereitung     |
| `jmod`         | JMOD-Dateien erstellen                       | Native Code einschließen                  |
| `jlink`        | Custom JRE erstellen                         | Deployment, Docker, IoT                   |
| `jpackage`     | Native Installer erstellen                   | Desktop-Anwendungen                       |

| Direktive           | Kurzform                                    |
|---------------------|---------------------------------------------|
| `requires`          | Abhängigkeit von anderem Modul              |
| `requires transitive`| Abhängigkeit weitergeben                   |
| `requires static`   | Nur Compile-Zeit                            |
| `exports`           | Paket nach außen sichtbar machen            |
| `exports ... to`    | Paket nur für bestimmtes Modul              |
| `opens`             | Paket für Reflection öffnen                 |
| `opens ... to`      | Reflection nur für bestimmtes Modul         |
| `uses`              | Service-Interface konsumieren               |
| `provides ... with` | Service-Implementierung bereitstellen       |
