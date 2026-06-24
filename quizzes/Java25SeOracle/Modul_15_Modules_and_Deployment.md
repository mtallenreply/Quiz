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

---

## 14. Compact Source Files und Instance Main Methods [Java 21+]

### 14.1 Compact Source Files (Single-File Programs) [Anfänger]

Seit Java 11 können einfache Java-Programme mit `java MeinProgramm.java` direkt ausgeführt werden, ohne vorheriges Kompilieren. Java 21 hat dieses Konzept unter dem Namen **Compact Source Files** (JEP 445 Preview, JEP 463 Second Preview) weiter ausgebaut. Es erlaubt, eine vollständige Anwendung in einer einzigen `.java`-Datei zu schreiben, wobei Imports, Klasse und Methode auf das Nötigste reduziert werden. In Java 25 ist das Feature final (JEP 495). Der Compiler erkennt automatisch, ob es sich um ein normales oder ein Compact-Programm handelt.

```java
// HelloWorld.java – minimales Compact-Source-File (kein explizites class-Schlüsselwort nötig)
// Seit Java 25 final (JEP 495: Simple Source Files and Instance Main Methods)

void main() {
    System.out.println("Hallo, Welt!");
}
```

```java
// Ausführen ohne vorheriges Kompilieren:
// java HelloWorld.java

// Komplexeres Beispiel: Imports und Methoden auf Datei-Ebene erlaubt
import java.util.List;

void main() {
    var namen = List.of("Anna", "Ben", "Clara");
    namen.forEach(name -> System.out.println("Hallo, " + name + "!"));
}
```

**Praktische Hinweise:**
- Geeignet für Lernprogramme, kleine Skripte und Demos.
- Die Datei muss nach der öffentlichen Klasse (falls vorhanden) oder nach dem Dateinamen benannt sein.
- Kein `public class`, kein `public static void main(String[] args)` erforderlich.
- Compact Source Files laufen im **Unnamed Package** und können nicht auf Klassen anderer Pakete zugreifen.

---

### 14.2 Instance Main Methods [Anfänger]

Java 21 führte **Instance Main Methods** (JEP 445) ein, die in Java 25 final sind (JEP 495). Statt der klassischen `public static void main(String[] args)`-Signatur kann die Main-Methode jetzt als Instanzmethode ohne `static`, ohne `public` und ohne Parameter deklariert werden. Die JVM wählt die passendste Main-Methode nach einer festgelegten Priorität aus. Dies senkt die Einstiegshürde für Lernende erheblich.

```java
// Klassisch (weiterhin gültig):
public class KlassischMain {
    public static void main(String[] args) {
        System.out.println("Klassisch");
    }
}

// Neu seit Java 25 (Instance Main Method):
class ModernMain {
    void main() {
        System.out.println("Moderne Instance Main Method");
    }
}

// Auch möglich: mit args-Parameter (aber static weglassen)
class FlexMain {
    void main(String[] args) {
        System.out.println("Args: " + args.length);
    }
}
```

```java
// Prioritätsreihenfolge der JVM beim Suchen der Main-Methode:
// 1. static void main(String[] args)   – klassisch, höchste Priorität
// 2. static void main()                – ohne Parameter
// 3. void main(String[] args)          – Instanz mit args
// 4. void main()                       – Instanz ohne Parameter, niedrigste Priorität

// In Compact Source Files (ohne class-Deklaration) wird die Datei
// implizit in eine anonyme Klasse eingebettet:
void main() {
    greet("Welt");
}

void greet(String name) {
    System.out.println("Hallo, " + name + "!");
}
```

**Praktische Hinweise:**
- Instance Main Methods können Instanzfelder und Instanzmethoden direkt nutzen – kein `static` mehr notwendig.
- Die klassische `public static void main(String[] args)`-Variante bleibt vollständig kompatibel.
- Für Produktionscode in modularen Projekten wird weiterhin die explizite Klasse empfohlen.

---

### 14.3 Multi-File Source-Code Programs [Fortgeschritten]

**Multi-File Source Programs** (JEP 458, Java 22+, in Java 25 final) erlauben es, eine aus mehreren `.java`-Dateien bestehende Anwendung direkt mit `java` zu starten, ohne vorher mit `javac` zu kompilieren. Der Java Launcher übernimmt die Kompilierung automatisch im Hintergrund. Dies ist besonders nützlich für Prototypen und Lernprojekte, die etwas größer sind als ein einzelnes File, aber noch keine vollständige Build-Infrastruktur rechtfertigen.

```
Projektstruktur (kein Build-Tool nötig):
greet-app/
├── Main.java
├── Greeter.java
└── Formatter.java
```

```java
// Greeter.java
public class Greeter {
    private final Formatter formatter;

    public Greeter(Formatter formatter) {
        this.formatter = formatter;
    }

    public String greet(String name) {
        return formatter.format("Hallo, " + name + "!");
    }
}

// Formatter.java
public class Formatter {
    public String format(String text) {
        return "*** " + text + " ***";
    }
}

// Main.java – Einstiegspunkt
void main() {
    var formatter = new Formatter();
    var greeter = new Greeter(formatter);
    System.out.println(greeter.greet("Welt"));
}
```

```bash
# Direkt starten – kein javac nötig!
java Main.java

# Ausgabe:
# *** Hallo, Welt! ***

# Alle referenzierten Dateien im selben Verzeichnis werden automatisch mitgekompiliert.
# Wichtig: Die Startat-Datei muss als erstes Argument angegeben werden.
```

**Praktische Hinweise:**
- Der Launcher kompiliert alle referenzierten Klassen im selben Verzeichnis automatisch.
- Es gibt keine `module-info.java` – das Programm läuft im Unnamed Module.
- Für größere Projekte mit Abhängigkeiten bleibt `javac` + `java --module-path` oder Maven/Gradle notwendig.
- Klassen dürfen nicht in Paketen liegen (Unnamed Package).

---

## 15. Modulare vs. Nicht-modulare JARs

### 15.1 Vergleich: Modular vs. Non-modular JAR [Fortgeschritten]

Ein **modulares JAR** enthält eine `module-info.class`-Datei im Root-Verzeichnis und deklariert damit seine Abhängigkeiten, exportierten Pakete und weiteren Modul-Metadaten explizit. Ein **nicht-modulares JAR** (auch *Plain JAR*) hat keine `module-info.class` und verhält sich je nach Platzierung entweder als **Unnamed Module** (auf dem Classpath) oder als **Automatic Module** (auf dem Modulpfad).

| Merkmal                      | Modulares JAR                         | Nicht-modulares JAR (Classpath)       | Nicht-modulares JAR (Modulpfad = Automatic) |
|------------------------------|---------------------------------------|---------------------------------------|---------------------------------------------|
| Enthält `module-info.class`  | Ja                                    | Nein                                  | Nein                                        |
| Modul-Typ                    | Named Module                          | Unnamed Module                        | Automatic Module                            |
| Kapselung                    | Stark (nur exportierte Pakete)        | Keine                                 | Keine (alle Pakete exportiert)              |
| Kann von Named Module required werden | Ja                          | Nein                                  | Ja (Name aus Dateiname oder Manifest)       |
| Kann andere Named Modules sehen | Ja (via `requires`)              | Alle (transitiv)                      | Alle (automatisch)                          |
| `requires`-Direktive nötig   | Ja                                    | Nein                                  | Nein                                        |
| Einsatz                      | Neue, modularisierte Bibliotheken     | Legacy-Code, Migration                | Bibliotheken ohne JPMS-Support              |

```bash
# Modulares JAR identifizieren
jar --describe-module --file mylib.jar
# Ausgabe: com.mylib@1.0 jar:file:///pfad/mylib.jar!/module-info.class
# requires java.base mandated
# exports com.mylib.api

# Nicht-modulares JAR auf Modulpfad (Automatic Module)
jar --describe-module --file legacy.jar
# Ausgabe: No module descriptor found. Derived automatic module.
# legacy@0.0 automatic
# requires java.base mandated
# contains com.legacy

# Classpath-Nutzung (Unnamed Module – kein Modulname)
java -cp legacy.jar:. com.legacy.Main
```

```java
// Automatic-Module-Name im Manifest setzen (empfohlene Praxis für Bibliotheks-Autoren)
// META-INF/MANIFEST.MF:
// Automatic-Module-Name: com.mylib.legacy
//
// → Stabilisiert den Modulnamen, unabhängig vom JAR-Dateinamen
// → Erlaubt anderen Modulen, requires com.mylib.legacy zu schreiben,
//    ohne dass sich der Name bei Umbenennung ändert.

module com.mein.app {
    requires com.mylib.legacy;  // Automatic Module via Manifest-Name
}
```

**Praktische Hinweise:**
- Unnamed Module kann alle anderen Module sehen, aber **kein Named Module kann Unnamed Module required**.
- Automatic Module ist der Übergangsweg für nicht-modularisierte Bibliotheken.
- Für neue Bibliotheken sollte immer ein `module-info.java` erstellt werden.

---

## 16. Erweiterte Kommandozeilen-Flags

### 16.1 javac und java mit Classpath [Anfänger]

Neben `--module-path` unterstützen `javac` und `java` weiterhin den klassischen Classpath. Beide Varianten können auch kombiniert werden (gemischte Projekte).

```bash
# Kompilieren mit klassischem Classpath (-cp oder --class-path)
javac -cp libs/jackson.jar:libs/slf4j.jar \
      -d out \
      src/com/mein/Main.java

# Ausführen mit Classpath
java -cp out:libs/jackson.jar:libs/slf4j.jar \
     com.mein.Main

# Windows: Semikolon als Trennzeichen statt Doppelpunkt
javac -cp "libs\jackson.jar;libs\slf4j.jar" -d out src\com\mein\Main.java
java -cp "out;libs\jackson.jar;libs\slf4j.jar" com.mein.Main

# Gemischt: Modulpfad + Classpath
java --module-path mods \
     --class-path legacy-libs/ \
     --module com.mein.app/com.mein.app.Main
```

---

### 16.2 --add-exports und --add-opens [Fortgeschritten]

Diese Flags erlauben es, **zur Laufzeit** Pakete zu exportieren oder für Reflection zu öffnen, ohne die `module-info.java` zu ändern. Sie werden vor allem bei der Migration und bei Framework-Kompatibilität eingesetzt.

```bash
# --add-exports: Paket für ein anderes Modul oder ALL-UNNAMED sichtbar machen
# Syntax: --add-exports <Modul>/<Paket>=<Zielmodul>
java --add-exports java.base/sun.nio.ch=ALL-UNNAMED \
     -jar myapp.jar

# --add-opens: Paket für Deep Reflection öffnen
# Syntax: --add-opens <Modul>/<Paket>=<Zielmodul>
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.util=com.mein.framework \
     -jar myapp.jar

# Für mehrere Module gleichzeitig (häufig bei Spring Boot / Hibernate nötig):
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens java.base/java.io=ALL-UNNAMED \
     --add-opens java.rmi/sun.rmi.transport=ALL-UNNAMED \
     --module-path mods \
     --module com.shop.app/com.shop.app.Main
```

```java
// Unterschied --add-exports vs. --add-opens:
// --add-exports: Macht Paket sichtbar für normalen Zugriff (public API)
//                Entspricht 'exports P to M' in module-info.java
//
// --add-opens:   Öffnet Paket für Deep Reflection (private Felder, Methoden)
//                Entspricht 'opens P to M' in module-info.java
//                Beinhaltet automatisch auch exports-Effekt

// In module-info.java (bevorzugt für eigenen Code):
module com.mein.modul {
    exports com.mein.intern to com.mein.test;   // nur Compile-Time + normaler Zugriff
    opens   com.mein.intern to com.mein.test;   // zusätzlich Deep Reflection
}
```

---

### 16.3 --add-modules und --add-reads [Fortgeschritten]

```bash
# --add-modules: Zusätzliche Module in den Modul-Graph aufnehmen
# Nützlich wenn ein Modul nicht direkt im requires-Graph steht
java --add-modules java.xml.bind,java.activation \
     --module-path mods \
     --module com.mein.app/com.mein.app.Main

# ALL-SYSTEM: alle System-Module hinzufügen
java --add-modules ALL-SYSTEM -jar myapp.jar

# ALL-DEFAULT: alle Default-Module (wie JDK ohne jdk.*-interne Module)
java --add-modules ALL-DEFAULT -jar myapp.jar

# --add-reads: Modul A darf von Modul B lesen (ohne requires in module-info)
# Syntax: --add-reads <Modul>=<anderes-Modul>
java --add-reads com.mein.app=com.fremde.lib \
     --module-path mods \
     --module com.mein.app/com.mein.app.Main
```

---

### 16.4 --patch-module [Professionell]

`--patch-module` erlaubt es, Klassen eines Moduls durch eigene Versionen zu ersetzen oder zu ergänzen – hauptsächlich für Tests und Migrations-Szenarien eingesetzt.

```bash
# --patch-module: Klassen in ein Modul einpatchen
# Syntax: --patch-module <Modul>=<Pfad>
# Nützlich um Test-Stubs in das zu testende Modul einzuschleusen

javac --patch-module com.mein.modul=test-patches/src \
      --module-path mods \
      -d test-out \
      test-patches/src/com/mein/modul/TestHelper.java

java --patch-module com.mein.modul=test-out \
     --module-path mods \
     --module com.mein.app/com.mein.app.Main

# Typischer Einsatz: JUnit-Tests für Named Module
# ohne das Modul zu öffnen (opens in module-info.java)
java --patch-module com.mein.modul=target/test-classes \
     --add-opens com.mein.modul/com.mein.intern=ALL-UNNAMED \
     --module-path mods \
     --add-modules org.junit.platform.launcher \
     --module org.junit.platform.launcher/... 
```

---

## 17. Unnamed Module und Automatic Module im Detail

### 17.1 Unnamed Module – Verhalten aus Named-Module-Perspektive [Fortgeschritten]

Das **Unnamed Module** fasst alle JARs und Klassen zusammen, die auf dem Classpath liegen. Es hat keinen Namen, exportiert alle seine Pakete und kann alle anderen Module lesen. **Kein Named Module kann jedoch das Unnamed Module mit `requires` einbinden** – das ist die zentrale Einschränkung bei der Migration.

```
Sichtbarkeitsregeln:
┌────────────────────┐         ┌──────────────────────┐
│   Named Module A   │──────>  │   Named Module B     │  Möglich via: requires B
│  (module-info.java)│         │  (module-info.java)  │
└────────────────────┘         └──────────────────────┘
         │                              │
         │ NICHT MÖGLICH                │ MÖGLICH (lesen)
         ▼                              ▼
┌────────────────────┐         ┌──────────────────────┐
│   Unnamed Module   │──────>  │   Named Module C     │  Unnamed kann alles lesen
│   (Classpath)      │         │                      │
└────────────────────┘         └──────────────────────┘
```

```java
// Unnamed Module: kein module-info.java
// Liest: java.base und alle anderen Platform-Module
//        alle Automatic Module
//        alle anderen Named Module (wenn diese exportieren)
// Kann gelesen werden von: Automatic Modules, anderen Unnamed Modules
// Kann NICHT gelesen werden von: Named Modules (kein 'requires ALL-UNNAMED')

// Workaround für Named Module, das Unnamed Module-Code aufrufen muss:
// Option 1: Code in Named Module verschieben
// Option 2: --add-reads-Flag nutzen (Runtime-Patch)
// Option 3: Service-Loader-Muster verwenden
```

```bash
# Named Module auf Unnamed Module zugreifen lassen (Workaround, nur Migration):
java --add-reads com.mein.named=ALL-UNNAMED \
     --module-path mods \
     --class-path legacy.jar \
     --module com.mein.named/com.mein.Main
```

---

### 17.2 Automatic Module – Naming und Fallstricke [Fortgeschritten]

```bash
# Automatic-Module-Name wird in folgender Reihenfolge bestimmt:
# 1. Manifest-Attribut: Automatic-Module-Name (bevorzugt, stabil)
# 2. JAR-Dateiname: Bindestriche und Versionsstrings werden normalisiert
#    jackson-databind-2.15.3.jar → jackson.databind (Versionsteil entfernt)
#    log4j-core-2.20.0.jar      → log4j.core

# Wichtiger Unterschied zu Named Modules:
# Automatic Module exportiert ALLE seine Pakete (keine Kontrolle möglich)
# Automatic Module hat implizit 'requires' auf ALLE anderen Module im Modul-Graph
# → Kann zu unerwartetem Zugriff führen!

# Empfehlung für Bibliotheks-Autoren:
# Immer Automatic-Module-Name im MANIFEST.MF setzen:
jar --create \
    --file mylib.jar \
    --manifest manifest.txt \
    -C classes .

# manifest.txt:
# Manifest-Version: 1.0
# Automatic-Module-Name: com.meine.bibliothek
```

---

## 18. Übungsaufgaben

### Aufgabe 1: Compact Source File erstellen

Schreiben Sie ein Compact Source File `Rechner.java` (ohne explizite Klassen-Deklaration), das zwei Zahlen addiert und das Ergebnis ausgibt. Führen Sie es direkt mit `java Rechner.java` aus.

**Lösung:**
```java
// Rechner.java
void main() {
    int a = 42;
    int b = 58;
    System.out.println(a + " + " + b + " = " + (a + b));
}
// Ausführen: java Rechner.java
// Ausgabe: 42 + 58 = 100
```

### Aufgabe 2: Instance Main Method

Schreiben Sie eine Klasse `Begruessung`, die eine Instanzvariable `String name` hat und eine `void main()`-Methode, die `"Hallo, <name>!"` ausgibt.

**Lösung:**
```java
// Begruessung.java
class Begruessung {
    String name = "Java 25";

    void main() {
        System.out.println("Hallo, " + name + "!");
    }
}
// Ausführen: java Begruessung.java
// Ausgabe: Hallo, Java 25!
```

### Aufgabe 3: Modular vs. Non-modular JAR

Erklären Sie, was passiert, wenn Sie eine Legacy-JAR ohne `module-info.class`:
a) auf den Classpath legen
b) auf den Modulpfad legen

Nennen Sie den Modultyp und beschreiben Sie je zwei Eigenschaften.

**Lösung:**
- a) Unnamed Module: kein Modulname, exportiert alle Pakete, kann von Named Modules nicht required werden
- b) Automatic Module: Name aus Dateiname/Manifest, exportiert alle Pakete, kann von Named Modules required werden

---

## 19. Multiple-Choice-Fragen

**Frage 1:** Welche Aussage zu Compact Source Files in Java 25 ist korrekt?

- A) Sie erfordern zwingend eine `public class`-Deklaration.
- B) Sie müssen mit `javac` vorkompiliert werden, bevor `java` sie ausführt.
- **C) Eine `void main()`-Methode ohne Klasse genügt als vollständiges Programm.** ✓
- D) Sie können nur eine einzige Methode enthalten.

**Frage 2:** In welcher Reihenfolge sucht die JVM die Main-Methode bei Instance Main Methods (höchste Priorität zuerst)?

- A) `void main()` → `void main(String[])` → `static void main()` → `static void main(String[])`
- **B) `static void main(String[])` → `static void main()` → `void main(String[])` → `void main()`** ✓
- C) `static void main(String[])` → `void main(String[])` → `static void main()` → `void main()`
- D) Die JVM wählt immer die erste Methode namens `main` in der Datei.

**Frage 3:** Was erlaubt `java Main.java` mit Multi-File Source Programs (JEP 458)?

- A) Nur `Main.java` wird kompiliert; andere Dateien müssen separat mit `javac` kompiliert werden.
- B) Alle `.java`-Dateien des gesamten Projekts werden automatisch kompiliert.
- **C) Der Launcher kompiliert automatisch alle referenzierten Klassen aus dem selben Verzeichnis.** ✓
- D) Die Dateien müssen explizit mit `--source-files` angegeben werden.

**Frage 4:** Was ist der Hauptunterschied zwischen `--add-exports` und `--add-opens`?

- A) `--add-exports` wirkt nur zur Compile-Zeit, `--add-opens` nur zur Laufzeit.
- B) `--add-exports` erlaubt Reflection auf private Felder, `--add-opens` nur öffentlichen Zugriff.
- **C) `--add-opens` erlaubt Deep Reflection (auch private Member), `--add-exports` nur normalen öffentlichen Zugriff.** ✓
- D) Beide sind identisch, nur der Name unterscheidet sich.

**Frage 5:** Welche Aussage über das Unnamed Module ist korrekt?

- A) Named Modules können das Unnamed Module mit `requires ALL-UNNAMED` direkt einbinden.
- B) Das Unnamed Module sieht nur `java.base`.
- C) Das Unnamed Module ist das Äquivalent zu einem Automatic Module auf dem Modulpfad.
- **D) Das Unnamed Module kann alle anderen Module lesen, aber kein Named Module kann es via `requires` einbinden.** ✓

**Frage 6:** Wie wird der Name eines Automatic Module bestimmt, wenn keine `Automatic-Module-Name`-Angabe im Manifest vorhanden ist?

- A) Der Name wird vom JDK zufällig generiert.
- B) Der Name entspricht dem ersten Paket im JAR.
- **C) Der Name wird aus dem JAR-Dateinamen abgeleitet (Bindestriche zu Punkten, Versionsstring entfernt).** ✓
- D) Das JAR wird als Unnamed Module behandelt, nicht als Automatic Module.

**Frage 7:** Wofür wird `--patch-module` hauptsächlich verwendet?

- A) Um den Modulpfad zur Laufzeit zu erweitern.
- **B) Um Klassen eines bestehenden Moduls durch eigene Versionen zu ersetzen oder zu ergänzen (z. B. für Tests).** ✓
- C) Um Abhängigkeiten zwischen Modulen zur Laufzeit hinzuzufügen.
- D) Um exportierte Pakete eines Moduls zu überschreiben.

**Frage 8:** Welche Aussage zu modularen vs. nicht-modularen JARs ist korrekt?

- A) Nicht-modulare JARs können nicht mit Java 25 verwendet werden.
- B) Modulare JARs auf dem Classpath verhalten sich wie Automatic Modules.
- **C) Nicht-modulare JARs auf dem Modulpfad werden als Automatic Module behandelt und exportieren alle Pakete.** ✓
- D) Modulare JARs auf dem Classpath werden als Named Module behandelt.

---

## 20. Skill Check: Modules and Deployment

Stellen Sie sicher, dass Sie folgende Fähigkeiten beherrschen (mind. 80 % der Fragen korrekt):

- [ ] `module-info.java` mit allen Direktiven schreiben (`requires`, `exports`, `opens`, `uses`, `provides`)
- [ ] Den Unterschied zwischen Named, Unnamed, Automatic und Platform Module erklären
- [ ] Ein Mehr-Modul-Projekt mit `javac` und `java` kompilieren und ausführen
- [ ] Modulare JARs mit `jar` erstellen und beschreiben
- [ ] `jdeps` zur Abhängigkeitsanalyse und für jlink-Vorbereitung einsetzen
- [ ] Eine Custom JRE mit `jlink` erstellen
- [ ] Einen nativen Installer mit `jpackage` erstellen
- [ ] Classpath (`-cp`) und Modulpfad (`--module-path`) korrekt einsetzen und kombinieren
- [ ] `--add-exports`, `--add-opens`, `--add-modules` und `--add-reads` erklären und anwenden
- [ ] `--patch-module` für Test-Szenarien einsetzen
- [ ] Ein Compact Source File (Single-File Program) schreiben und mit `java` direkt ausführen
- [ ] Instance Main Methods schreiben und die Prioritätsreihenfolge der JVM erklären
- [ ] Ein Multi-File Source Program ohne `javac` starten
- [ ] Den Unterschied zwischen modularen und nicht-modularen JARs in einer Vergleichstabelle erklären
- [ ] Migrationsstrategien (Bottom-up, Top-down) und den Einsatz von Automatic Modules beschreiben
- [ ] `Automatic-Module-Name` im Manifest setzen und dessen Bedeutung erklären
