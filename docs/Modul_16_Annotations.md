# Modul 16: Annotations

## Übersicht

Dieses Modul behandelt Java-Annotationen: eingebaute Standard-Annotationen, Meta-Annotationen zur Definition eigener Annotationen, das Erstellen benutzerdefinierter Annotationen, das Auslesen zur Laufzeit via Reflection sowie die Grundlagen von Annotation-Prozessoren.

| Thema                          | Dauer |
|--------------------------------|-------|
| Annotation Purpose             | 20 m  |
| Built-in Annotations           | 19 m  |
| Meta-annotations               | 15 m  |
| Custom Annotations             | 18 m  |
| Runtime Reflection             | 12 m  |
| Annotation Processors          | 11 m  |
| Practice 16-1                  | 20 m  |
| **Skill Check: Annotations**   | **mind. 80 %** |

---

## 1. Was sind Annotationen und wozu dienen sie?

### 1.1 Grundkonzept

Annotationen sind **Metadaten** – sie beschreiben Code, ohne dessen Verhalten direkt zu ändern. Sie beginnen immer mit `@` und können an Klassen, Methoden, Feldern, Parametern und anderen Programmelementen angebracht werden.

```java
// Annotation an einer Klasse
@Deprecated
public class AlteKlasse {

    // Annotation an einer Methode
    @Override
    public String toString() { return "Alt"; }

    // Annotation an einem Feld
    @SuppressWarnings("unused")
    private int nichtGenutzt;

    // Annotation an einem Parameter
    public void methode(@NotNull String name) {}
}
```

### 1.2 Die drei Verwendungsebenen

```
Annotation-Nutzung
├── Compile-Zeit
│   ├── Compiler-Hinweise (@Override, @SuppressWarnings)
│   └── Code-Generierung (@Getter, @Setter in Lombok)
├── Build-Zeit (Annotation-Prozessoren)
│   ├── Source-Code generieren
│   ├── Validierung
│   └── Ressourcen erzeugen (META-INF/services)
└── Laufzeit (Reflection)
    ├── Frameworks (Spring, Jakarta EE, Hibernate)
    ├── Dependency Injection (@Autowired)
    └── ORM-Mapping (@Entity, @Column)
```

### 1.3 Annotationen vs. Kommentare vs. XML-Konfiguration

| Merkmal                  | Kommentar         | XML-Konfiguration     | Annotation             |
|--------------------------|-------------------|-----------------------|------------------------|
| Syntaxprüfung            | Keine             | Nur Schema-Prüfung    | Vollständig durch Compiler |
| Nähe zum Code            | Im Code           | Getrennte Datei       | Direkt am Code-Element |
| Typsicherheit            | Keine             | Eingeschränkt         | Vollständig typsicher  |
| Refactoring-sicher       | Nein              | Nein                  | Ja (IDE-Unterstützung) |
| Laufzeit-Verarbeitung    | Nicht möglich     | Explizit nötig        | Via Reflection möglich |

---

## 2. Eingebaute Standard-Annotationen

### 2.1 @Override

Teilt dem Compiler mit, dass eine Methode eine geerbte Methode überschreibt. Ohne `@Override` könnte ein Tippfehler eine neue Methode erzeugen statt zu überschreiben.

```java
public class Tier {
    public String lautGeben() { return "..."; }
    public boolean equals(Object obj) { return super.equals(obj); }
}

public class Hund extends Tier {

    // @Override: Compiler prüft ob tatsächlich überschrieben wird
    @Override
    public String lautGeben() { return "Wuff"; }

    // FEHLER ohne @Override nicht erkennbar:
    public String lautgeben() { return "Wuff"; } // Tippfehler! Neue Methode!

    // @Override auf equals: Compiler prüft Signatur
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Hund)) return false;
        return true;
    }

    // Kompilierfehler: Methode existiert nicht in Oberklasse
    // @Override
    // public String rennen() { return "läuft"; }
}
```

### 2.2 @Deprecated

Markiert ein Element als veraltet. IDE und Compiler geben Warnungen aus, wenn es verwendet wird.

```java
public class AltesAPI {

    // Einfach deprecated
    @Deprecated
    public void alteMethode() {
        System.out.println("Diese Methode ist veraltet!");
    }

    // Mit Begründung und Ersatz (seit Java 9)
    @Deprecated(since = "2.0", forRemoval = true)
    public String alteBerechnung(int x) {
        return neueBerechnung(x, 1); // intern delegieren
    }

    // Empfohlener Ersatz
    public String neueBerechnung(int x, int faktor) {
        return String.valueOf(x * faktor);
    }
}

// Verwendung erzeugt Warnung
AltesAPI api = new AltesAPI();
api.alteMethode(); // WARNUNG: deprecated!

// @Deprecated an Klasse: gesamte Klasse veraltet
@Deprecated(since = "3.0", forRemoval = true)
public class VeralteteKlasse {
    // Alles hier ist veraltet
}
```

### 2.3 @SuppressWarnings

Unterdrückt spezifische Compiler-Warnungen.

```java
public class Suppression {

    // Typ-Sicherheits-Warnung unterdrücken (generics)
    @SuppressWarnings("unchecked")
    public <T> List<T> parseList(Object obj) {
        return (List<T>) obj; // unsicherer Cast
    }

    // Deprecated-Warnung unterdrücken
    @SuppressWarnings("deprecation")
    public void nutzVeraltetes() {
        new VeralteteKlasse().alteMethode();
    }

    // Mehrere Warnungen gleichzeitig
    @SuppressWarnings({"unchecked", "deprecation"})
    public void mehrereWarnungen() {
        List liste = new ArrayList(); // raw type
        new VeralteteKlasse();
    }

    // Alle Warnungen (nicht empfohlen!)
    @SuppressWarnings("all")
    public void alleUnterdrücken() {}
}
```

| Warnungstyp       | Beschreibung                                          |
|-------------------|-------------------------------------------------------|
| `unchecked`       | Unsichere Typ-Konvertierungen (Generics)               |
| `deprecation`     | Nutzung veralteter Elemente                           |
| `rawtypes`        | Verwendung von Raw-Types (ohne Generics-Parameter)    |
| `unused`          | Unbenutzte Variablen, Methoden, Importe               |
| `serial`          | Fehlende `serialVersionUID` in `Serializable`-Klassen |
| `all`             | Alle Warnungen (nur als Notlösung)                    |

### 2.4 @FunctionalInterface

Markiert ein Interface als funktionales Interface (genau eine abstrakte Methode). Der Compiler prüft dies zur Compile-Zeit.

```java
// @FunctionalInterface: Compiler prüft, dass genau 1 abstrakte Methode
@FunctionalInterface
public interface Rechner {
    int berechne(int a, int b); // die eine abstrakte Methode

    // Default-Methoden sind erlaubt
    default Rechner undDann(Rechner nachher) {
        return (a, b) -> nachher.berechne(this.berechne(a, b), 0);
    }

    // Statische Methoden sind erlaubt
    static Rechner addiere() { return (a, b) -> a + b; }

    // Methoden aus Object sind erlaubt
    @Override
    String toString();
}

// Als Lambda verwendbar
Rechner addition   = (a, b) -> a + b;
Rechner multiplik  = (a, b) -> a * b;
Rechner potenz     = (a, b) -> (int) Math.pow(a, b);

System.out.println(addition.berechne(3, 4));     // 7
System.out.println(multiplik.berechne(3, 4));    // 12

// Kompilierfehler: zweite abstrakte Methode verboten
// @FunctionalInterface
// interface Ungültig {
//     void methode1();
//     void methode2(); // Fehler!
// }
```

### 2.5 Weitere eingebaute Annotationen

```java
// @SafeVarargs: Unterdrückt Heap-Pollution-Warnung bei Varargs mit Generics
@SafeVarargs
public final <T> List<T> erstelleListe(T... elemente) {
    return Arrays.asList(elemente);
}

// @Native: Markiert Konstante als Referenz für nativen Code (JNI)
public class NativeKonstanten {
    @Native public static final int MAX_PUFFER = 8192;
}
```

---

## 3. Meta-Annotationen

Meta-Annotationen sind Annotationen **für Annotationen** – sie steuern, wie benutzerdefinierte Annotationen funktionieren.

### 3.1 @Retention

Bestimmt, wie lange die Annotation im kompilierten Code verfügbar bleibt.

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Nur im Source-Code (für IDEs und Dokumentations-Tools)
@Retention(RetentionPolicy.SOURCE)
public @interface NurImSource {}

// Im kompilierten .class-File (Standard wenn nicht angegeben)
@Retention(RetentionPolicy.CLASS)
public @interface ImClassFile {}

// Zur Laufzeit via Reflection lesbar (für Frameworks)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZurLaufzeit {}
```

| RetentionPolicy | .java | .class | Laufzeit (Reflection) | Typischer Einsatz          |
|-----------------|-------|--------|-----------------------|----------------------------|
| `SOURCE`        | Ja    | Nein   | Nein                  | `@Override`, `@SuppressWarnings`, Lombok |
| `CLASS`         | Ja    | Ja     | Nein                  | Bytecode-Analyse-Tools (default) |
| `RUNTIME`       | Ja    | Ja     | Ja                    | Spring, JPA, JUnit, Jackson |

### 3.2 @Target

Bestimmt, an welchen Programmelementen die Annotation verwendet werden darf.

```java
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

// Nur an Klassen
@Target(ElementType.TYPE)
public @interface NurAnKlassen {}

// Nur an Methoden
@Target(ElementType.METHOD)
public @interface NurAnMethoden {}

// An mehreren Elementen
@Target({
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER
})
public @interface AnFeldMethodeParameter {}
```

| ElementType          | Wo verwendbar                                              |
|----------------------|------------------------------------------------------------|
| `TYPE`               | Klassen, Interfaces, Enums, Records, Annotationen          |
| `FIELD`              | Instanz- und Klassenfelder                                 |
| `METHOD`             | Methoden                                                   |
| `CONSTRUCTOR`        | Konstruktoren                                              |
| `PARAMETER`          | Methodenparameter                                          |
| `LOCAL_VARIABLE`     | Lokale Variablen                                           |
| `ANNOTATION_TYPE`    | Andere Annotationen (Meta-Annotationen)                    |
| `PACKAGE`            | Package-Deklarationen (`package-info.java`)                |
| `TYPE_PARAMETER`     | Generics-Typ-Parameter (`<T>`)                             |
| `TYPE_USE`           | Überall wo ein Typ vorkommt (`@NonNull String`)            |
| `MODULE`             | Modul-Deklarationen (`module-info.java`)                   |
| `RECORD_COMPONENT`   | Record-Komponenten (Java 16+)                              |

### 3.3 @Documented

Bewirkt, dass die Annotation in der JavaDoc-Dokumentation erscheint.

```java
import java.lang.annotation.Documented;

// Ohne @Documented: Annotation erscheint nicht in JavaDoc
// Mit @Documented: Annotation wird in JavaDoc angezeigt
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PublicAPI {
    String version() default "1.0";
}

// In JavaDoc-Ausgabe sichtbar:
@PublicAPI(version = "2.5")
public class MeineKlasse {
    // Die @PublicAPI-Annotation erscheint in der JavaDoc
}
```

### 3.4 @Inherited

Ermöglicht Vererbung von Annotationen von Eltern- zu Kindklassen.

```java
import java.lang.annotation.Inherited;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Kategorie {
    String wert();
}

@Kategorie(wert = "Basisklasse")
public class Basis {}

// Erbt @Kategorie von Basis!
public class Kind extends Basis {}

// Überprüfen
System.out.println(Kind.class.isAnnotationPresent(Kategorie.class)); // true
Kategorie k = Kind.class.getAnnotation(Kategorie.class);
System.out.println(k.wert()); // "Basisklasse"

// HINWEIS: @Inherited gilt NUR für Klassen-Annotationen,
// NICHT für Interface-Implementierungen!
```

### 3.5 @Repeatable

Ermöglicht das mehrfache Anbringen derselben Annotation an einem Element.

```java
import java.lang.annotation.*;

// Schritt 1: Container-Annotation definieren
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Rollen {
    Rolle[] value(); // muss value() heißen!
}

// Schritt 2: Wiederholbare Annotation definieren
@Repeatable(Rollen.class) // Container angeben
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Rolle {
    String name();
}

// Schritt 3: Mehrfach verwenden
public class AdminService {

    @Rolle(name = "ADMIN")
    @Rolle(name = "SUPER_USER")
    @Rolle(name = "MANAGER")
    public void deleteUser(int userId) {
        System.out.println("User gelöscht: " + userId);
    }
}

// Lesen via Reflection
Method m = AdminService.class.getMethod("deleteUser", int.class);
Rolle[] rollen = m.getAnnotationsByType(Rolle.class);
for (Rolle r : rollen) {
    System.out.println("Erlaubte Rolle: " + r.name());
}
```

---

## 4. Benutzerdefinierte Annotationen

### 4.1 Syntax einer Annotation

```java
// Annotationen werden wie Interfaces definiert, aber mit @interface
public @interface MeineAnnotation {
    // Elemente (wie Methoden) definieren Attribute
    String wert();              // Pflichtfeld (kein default)
    int maximumWert() default 100; // Optional mit Standardwert
    String[] tags() default {};    // Array-Attribut
    Class<?> zielKlasse() default Void.class; // Class-Attribut
}

// Erlaubte Attributtypen:
// - Primitive: byte, short, int, long, float, double, char, boolean
// - String
// - Class (oder Class<?>)
// - Enum
// - Andere Annotationen
// - Arrays der oben genannten

// NICHT erlaubt: Object, Collections, Maps, etc.
```

### 4.2 Das value()-Konventionsprinzip

```java
// Wenn Annotation nur ein Attribut "value" hat:
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Kategorie {
    String value(); // Heißt "value"
}

// Vereinfachte Verwendung (ohne Attributnamen)
@Kategorie("Datenbank") // statt @Kategorie(value = "Datenbank")
public class DbService {}

// Bei mehreren Attributen: Attributname immer notwendig
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Zeitgesteuert {
    String cron();             // Pflichtfeld
    String beschreibung() default ""; // Optional
}

// Verwendung
@Zeitgesteuert(cron = "0 0 * * *", beschreibung = "Täglich um Mitternacht")
public void taeglicherJob() {}
```

### 4.3 Praxisbeispiel: Validierungs-Annotationen

```java
import java.lang.annotation.*;

// @MinLength: Mindeständige Länge eines Strings
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface MinLength {
    int value() default 1;
    String nachricht() default "Feld ist zu kurz";
}

// @MaxLength: Maximale Länge eines Strings
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface MaxLength {
    int value() default 255;
    String nachricht() default "Feld ist zu lang";
}

// @NotEmpty: Darf nicht leer sein
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface NotEmpty {
    String nachricht() default "Feld darf nicht leer sein";
}

// @Range: Numerischer Bereich
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Range {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
    String nachricht() default "Wert liegt außerhalb des erlaubten Bereichs";
}

// Verwendung in einer Entitätsklasse
public class Benutzer {

    @NotEmpty
    @MinLength(3)
    @MaxLength(50)
    private String name;

    @NotEmpty
    private String email;

    @Range(min = 18, max = 120, nachricht = "Alter muss zwischen 18 und 120 liegen")
    private int alter;

    public Benutzer(String name, String email, int alter) {
        this.name = name;
        this.email = email;
        this.alter = alter;
    }

    // Getter
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public int getAlter()    { return alter; }
}
```

### 4.4 Marker-Annotation (ohne Attribute)

```java
// Marker-Annotationen haben keine Attribute
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Immutable {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Idempotent {}

// Verwendung (kein Klammerpaar nötig)
@Immutable
public final class Punkt {
    private final int x;
    private final int y;

    public Punkt(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public int getY() { return y; }
}
```

---

## 5. Annotationen zur Laufzeit auslesen (Reflection)

### 5.1 Annotationen an Klassen

```java
import java.lang.annotation.*;
import java.lang.reflect.*;

// Prüfen ob Annotation vorhanden
boolean istKategorie = MeineKlasse.class
    .isAnnotationPresent(Kategorie.class);

// Annotation lesen
Kategorie kat = MeineKlasse.class
    .getAnnotation(Kategorie.class);
if (kat != null) {
    System.out.println("Kategorie: " + kat.value());
}

// Alle Annotationen
Annotation[] alleAnnotationen = MeineKlasse.class
    .getAnnotations(); // inkl. geerbte
Annotation[] direkteAnnotationen = MeineKlasse.class
    .getDeclaredAnnotations(); // nur direkte (ohne geerbte)

// Wiederholbare Annotationen
Rolle[] rollen = AdminService.class
    .getAnnotationsByType(Rolle.class);
```

### 5.2 Annotationen an Feldern und Methoden

```java
import java.lang.reflect.*;

Class<?> klasse = Benutzer.class;

// Alle Felder durchlaufen
for (Field feld : klasse.getDeclaredFields()) {
    System.out.println("Feld: " + feld.getName());

    if (feld.isAnnotationPresent(NotEmpty.class)) {
        System.out.println("  -> darf nicht leer sein");
    }

    if (feld.isAnnotationPresent(MinLength.class)) {
        MinLength ml = feld.getAnnotation(MinLength.class);
        System.out.println("  -> Mindestlänge: " + ml.value());
    }

    if (feld.isAnnotationPresent(Range.class)) {
        Range r = feld.getAnnotation(Range.class);
        System.out.printf("  -> Bereich: %d bis %d%n", r.min(), r.max());
    }
}

// Methoden-Annotationen
for (Method methode : klasse.getDeclaredMethods()) {
    if (methode.isAnnotationPresent(Deprecated.class)) {
        System.out.println("Veraltete Methode: " + methode.getName());
    }
}

// Parameter-Annotationen
Method m = klasse.getDeclaredMethod("methode", String.class);
Parameter[] parameter = m.getParameters();
for (Parameter p : parameter) {
    for (Annotation a : p.getAnnotations()) {
        System.out.println("Parameter-Annotation: " + a.annotationType().getSimpleName());
    }
}
```

### 5.3 Vollständiges Validierungs-Framework

```java
import java.lang.reflect.*;
import java.util.*;

public class Validator {

    public static List<String> validiere(Object objekt) {
        List<String> fehler = new ArrayList<>();
        Class<?> klasse = objekt.getClass();

        for (Field feld : klasse.getDeclaredFields()) {
            feld.setAccessible(true); // private Felder zugreifbar machen

            Object wert;
            try {
                wert = feld.get(objekt);
            } catch (IllegalAccessException e) {
                continue;
            }

            String feldName = feld.getName();

            // @NotEmpty prüfen
            if (feld.isAnnotationPresent(NotEmpty.class)) {
                NotEmpty ann = feld.getAnnotation(NotEmpty.class);
                if (wert == null || wert.toString().isEmpty()) {
                    fehler.add(feldName + ": " + ann.nachricht());
                }
            }

            // @MinLength prüfen
            if (feld.isAnnotationPresent(MinLength.class) && wert != null) {
                MinLength ann = feld.getAnnotation(MinLength.class);
                if (wert.toString().length() < ann.value()) {
                    fehler.add(feldName + ": " + ann.nachricht()
                        + " (Minimum: " + ann.value() + ")");
                }
            }

            // @MaxLength prüfen
            if (feld.isAnnotationPresent(MaxLength.class) && wert != null) {
                MaxLength ann = feld.getAnnotation(MaxLength.class);
                if (wert.toString().length() > ann.value()) {
                    fehler.add(feldName + ": " + ann.nachricht()
                        + " (Maximum: " + ann.value() + ")");
                }
            }

            // @Range prüfen
            if (feld.isAnnotationPresent(Range.class) && wert instanceof Number) {
                Range ann = feld.getAnnotation(Range.class);
                int numWert = ((Number) wert).intValue();
                if (numWert < ann.min() || numWert > ann.max()) {
                    fehler.add(feldName + ": " + ann.nachricht()
                        + " (Wert: " + numWert + ")");
                }
            }
        }

        return fehler;
    }

    public static void main(String[] args) {
        // Gültig
        Benutzer gueltig = new Benutzer("Alice", "alice@example.com", 30);
        List<String> fehler1 = validiere(gueltig);
        System.out.println("Fehler (sollte leer sein): " + fehler1);

        // Ungültig
        Benutzer ungueltig = new Benutzer("Al", "", 15);
        List<String> fehler2 = validiere(ungueltig);
        System.out.println("Fehler:");
        fehler2.forEach(f -> System.out.println("  - " + f));
    }
}
```

### 5.4 Ausgabe des Validators

```
Fehler (sollte leer sein): []
Fehler:
  - name: Feld ist zu kurz (Minimum: 3)
  - email: Feld darf nicht leer sein
  - alter: Alter muss zwischen 18 und 120 liegen (Wert: 15)
```

---

## 6. Annotation-Prozessoren

### 6.1 Was sind Annotation-Prozessoren?

Annotation-Prozessoren sind Programme, die **zur Compile-Zeit** Annotationen verarbeiten. Sie können:
- Quelldateien generieren (Code-Generierung wie Lombok, MapStruct)
- Ressourcendateien erstellen (`META-INF/services`)
- Kompilierfehler oder -warnungen ausgeben
- Bytecode modifizieren (nur mit speziellen APIs)

```
javac-Ablauf mit Annotation-Prozessor:
Source → Parse → Annotationen verarbeiten → Code generieren → Kompilieren → .class
```

### 6.2 Ein einfacher Annotation-Prozessor

```java
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;
import java.util.*;

// Definiert welche Annotationen dieser Prozessor verarbeitet
@SupportedAnnotationTypes("com.beispiel.Immutable")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class ImmutableProzessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotationen,
                           RoundEnvironment umgebung) {

        // Alle Elemente mit @Immutable finden
        for (Element element : umgebung.getElementsAnnotatedWith(Immutable.class)) {

            // Nur Klassen prüfen
            if (element.getKind() != ElementKind.CLASS) continue;

            TypeElement klasse = (TypeElement) element;
            boolean hatMutableFeld = false;

            // Alle Felder der Klasse prüfen
            for (Element kind : klasse.getEnclosedElements()) {
                if (kind.getKind() != ElementKind.FIELD) continue;

                VariableElement feld = (VariableElement) kind;
                Set<Modifier> modifier = feld.getModifiers();

                // Prüfen ob Feld final ist
                if (!modifier.contains(Modifier.FINAL)) {
                    hatMutableFeld = true;

                    // Compilerfehler ausgeben
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@Immutable Klasse hat nicht-finales Feld: "
                            + feld.getSimpleName(),
                        feld // Fehlerpositon im Code
                    );
                }
            }

            if (!hatMutableFeld) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Klasse " + klasse.getSimpleName()
                        + " korrekt als @Immutable implementiert"
                );
            }
        }

        return true; // Annotation wurde verarbeitet
    }
}
```

### 6.3 Code generieren mit einem Prozessor

```java
import javax.annotation.processing.*;
import javax.lang.model.element.*;
import java.io.*;

@SupportedAnnotationTypes("com.beispiel.GeneriereSingleton")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class SingletonProzessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotationen,
                           RoundEnvironment umgebung) {

        for (Element element : umgebung.getElementsAnnotatedWith(
                GeneriereSingleton.class)) {

            TypeElement klasse = (TypeElement) element;
            String paketname = processingEnv.getElementUtils()
                .getPackageOf(klasse).getQualifiedName().toString();
            String klassenname = klasse.getSimpleName().toString();
            String singletonName = klassenname + "Singleton";

            try {
                // Neue Java-Quelldatei erstellen
                JavaFileObject datei = processingEnv.getFiler()
                    .createSourceFile(paketname + "." + singletonName);

                try (PrintWriter writer = new PrintWriter(datei.openWriter())) {
                    writer.println("package " + paketname + ";");
                    writer.println();
                    writer.println("public class " + singletonName + " {");
                    writer.println("    private static final " + klassenname
                        + " INSTANZ = new " + klassenname + "();");
                    writer.println();
                    writer.println("    private " + singletonName + "() {}");
                    writer.println();
                    writer.println("    public static " + klassenname
                        + " getInstanz() { return INSTANZ; }");
                    writer.println("}");
                }

            } catch (IOException e) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Konnte Singleton nicht generieren: " + e.getMessage()
                );
            }
        }
        return true;
    }
}
```

### 6.4 Prozessor registrieren und verwenden

```
Projektstruktur für Annotation-Prozessor:
processor-module/
├── src/main/java/
│   ├── com/beispiel/ImmutableProzessor.java
│   └── com/beispiel/Immutable.java
└── src/main/resources/
    └── META-INF/services/
        └── javax.annotation.processing.Processor
            (enthält: com.beispiel.ImmutableProzessor)
```

```bash
# Prozessor kompilieren
javac -proc:none \
      -d processor-classes \
      src/main/java/com/beispiel/*.java

# Prozessor-JAR erstellen
jar --create \
    --file processor.jar \
    -C processor-classes . \
    -C src/main/resources .

# Hauptprojekt mit Prozessor kompilieren
javac -processorpath processor.jar \
      -d classes \
      src/main/java/com/meinprojekt/*.java

# In Maven: maven-compiler-plugin annotationProcessorPaths
```

### 6.5 Bekannte Annotation-Prozessoren

| Framework/Tool | Annotation-Prozessor-Zweck                        |
|----------------|---------------------------------------------------|
| Lombok         | Generiert Getter, Setter, Konstruktoren, Builder  |
| MapStruct      | Generiert typsichere Bean-Mapper                  |
| Dagger         | Dependency-Injection-Code generieren              |
| AutoService    | `META-INF/services`-Dateien generieren            |
| Hibernate      | Metamodel-Klassen für Criteria-API generieren     |
| Immutables     | Unveränderliche Klassen generieren                |

---

## 7. Annotationen mit Enums und verschachtelten Annotationen

### 7.1 Enums in Annotationen

```java
// Enum für HTTP-Methoden
public enum HttpMethode { GET, POST, PUT, DELETE, PATCH }

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Endpunkt {
    String pfad();
    HttpMethode methode() default HttpMethode.GET;
    String[] rollen() default {};
    boolean authentifizierung() default true;
}

// Verwendung
public class UserController {

    @Endpunkt(pfad = "/users", methode = HttpMethode.GET)
    public List<User> alleBenutzer() { return List.of(); }

    @Endpunkt(
        pfad = "/users",
        methode = HttpMethode.POST,
        rollen = {"ADMIN", "MANAGER"},
        authentifizierung = true
    )
    public User benutzerAnlegen(User user) { return user; }
}
```

### 7.2 Verschachtelte Annotationen

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinSpalte {
    String name();
    boolean nullable() default true;
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {
    JoinSpalte[] joinColumns() default {};
    String tabelle() default "";
}

// Verwendung
public class Student {

    @ManyToMany(
        tabelle = "student_kurs",
        joinColumns = {
            @JoinSpalte(name = "student_id", nullable = false),
            @JoinSpalte(name = "semester")
        }
    )
    private List<Kurs> kurse;
}
```

---

## 8. Best Practices und häufige Fehler

### 8.1 Typische Fehler

```java
// FEHLER 1: RetentionPolicy.CLASS statt RUNTIME
// Annotation nicht via Reflection lesbar!
@Retention(RetentionPolicy.CLASS) // FALSCH für Laufzeit-Verarbeitung
public @interface MeinAnnotation {}

// RICHTIG für Laufzeit-Verarbeitung:
@Retention(RetentionPolicy.RUNTIME)
public @interface MeinAnnotation {}

// FEHLER 2: Annotationen ohne @Target überall verwendbar
// (kann zu unerwünschter Nutzung führen)
public @interface OhneTarget {} // kann überall verwendet werden

// BESSER: explizit @Target setzen
@Target(ElementType.METHOD)
public @interface NurAnMethoden {}

// FEHLER 3: null als Annotationswert (nicht erlaubt!)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fehler {
    // String wert() default null; // KOMPILIERFEHLER!
    String wert() default ""; // RICHTIG: leerer String als Default
}

// FEHLER 4: Mutable Typen als Attributtyp
public @interface Ungueltig {
    // List<String> tags(); // KOMPILIERFEHLER: List nicht erlaubt!
    String[] tags(); // RICHTIG: Array verwenden
}
```

### 8.2 Best Practices

| Empfehlung                                     | Begründung                                          |
|------------------------------------------------|-----------------------------------------------------|
| Immer `@Retention` explizit setzen             | Standardwert `CLASS` oft nicht gewünscht            |
| Immer `@Target` einschränken                   | Verhindert fehlerhafte Nutzung                      |
| `@Documented` für öffentliche APIs             | Nutzer sehen Annotationen in JavaDoc                |
| `value()` für einzelne, benannte Attribute     | Kürzere Verwendungssyntax                           |
| Sinnvolle Standardwerte (`default`)            | Macht optionale Attribute optional                  |
| Annotationen nicht überladen                   | Lieber mehrere kleine als eine riesige Annotation   |
| Validierungs-Annotationen kombinierbar gestalten | Mehrere Constraints an einem Feld ermöglichen     |
| Prozessoren mit `@SupportedAnnotationTypes`    | Präzise angeben welche Annotationen verarbeitet werden |

---

## 9. Vollständiges Praxisbeispiel – Mini-DI-Framework

```java
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

// --- Annotationen ---

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Komponente {
    String name() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Injektion {}

// --- Services ---

@Komponente
public class EmailService {
    public void sendeEmail(String empfaenger, String betreff) {
        System.out.printf("Email an %s: %s%n", empfaenger, betreff);
    }
}

@Komponente
public class LogService {
    public void log(String nachricht) {
        System.out.println("[LOG] " + nachricht);
    }
}

@Komponente("benutzerService")
public class BenutzerService {

    @Injektion
    private EmailService emailService;

    @Injektion
    private LogService logService;

    public void benutzerRegistrieren(String email) {
        logService.log("Neuer Benutzer: " + email);
        emailService.sendeEmail(email, "Willkommen!");
    }
}

// --- Mini-DI-Container ---

public class DiContainer {

    private final Map<String, Object> registry = new HashMap<>();

    public void registrieren(Class<?>... klassen) throws Exception {
        // Alle Klassen instanziieren
        for (Class<?> klasse : klassen) {
            if (klasse.isAnnotationPresent(Komponente.class)) {
                Komponente ann = klasse.getAnnotation(Komponente.class);
                String name = ann.name().isEmpty()
                    ? klasse.getSimpleName().substring(0, 1).toLowerCase()
                      + klasse.getSimpleName().substring(1)
                    : ann.name();
                registry.put(name, klasse.getDeclaredConstructor().newInstance());
            }
        }

        // Abhängigkeiten injizieren
        for (Object instanz : registry.values()) {
            for (Field feld : instanz.getClass().getDeclaredFields()) {
                if (feld.isAnnotationPresent(Injektion.class)) {
                    feld.setAccessible(true);
                    String typName = feld.getType().getSimpleName()
                        .substring(0, 1).toLowerCase()
                        + feld.getType().getSimpleName().substring(1);
                    Object abhaengigkeit = registry.get(typName);
                    if (abhaengigkeit != null) {
                        feld.set(instanz, abhaengigkeit);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) registry.get(name);
    }

    public static void main(String[] args) throws Exception {
        DiContainer container = new DiContainer();
        container.registrieren(
            EmailService.class,
            LogService.class,
            BenutzerService.class
        );

        BenutzerService service = container.get("benutzerService");
        service.benutzerRegistrieren("alice@example.com");
        // Ausgabe:
        // [LOG] Neuer Benutzer: alice@example.com
        // Email an alice@example.com: Willkommen!
    }
}
```

---

## 10. Zusammenfassung

| Annotation              | Typ         | Beschreibung                                          |
|-------------------------|-------------|-------------------------------------------------------|
| `@Override`             | Eingebaut   | Überschreibung einer Methode bestätigen               |
| `@Deprecated`           | Eingebaut   | Element als veraltet markieren                        |
| `@SuppressWarnings`     | Eingebaut   | Compiler-Warnungen unterdrücken                       |
| `@FunctionalInterface`  | Eingebaut   | Funktionales Interface (genau 1 abstrakte Methode)    |
| `@SafeVarargs`          | Eingebaut   | Heap-Pollution-Warnung bei Varargs unterdrücken       |
| `@Retention`            | Meta        | Lebensdauer der Annotation steuern                    |
| `@Target`               | Meta        | Erlaubte Verwendungsorte einschränken                 |
| `@Documented`           | Meta        | In JavaDoc einschließen                               |
| `@Inherited`            | Meta        | Auf Unterklassen vererbbar machen                     |
| `@Repeatable`           | Meta        | Mehrfache Verwendung an einem Element erlauben        |

| RetentionPolicy | .class | Reflection | Einsatz                              |
|-----------------|--------|------------|--------------------------------------|
| `SOURCE`        | Nein   | Nein       | IDEs, Lint, Lombok                   |
| `CLASS`         | Ja     | Nein       | Bytecode-Analyse (Standard)          |
| `RUNTIME`       | Ja     | Ja         | Spring, JPA, JUnit, eigene Frameworks|
