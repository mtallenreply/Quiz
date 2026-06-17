# Modul 17: JDBC – Java Database Connectivity

## Übersicht

JDBC (Java Database Connectivity) ist die Standard-API in Java, um relationale Datenbanken anzubinden. Mit JDBC können Programme SQL-Anweisungen absetzen, Ergebnismengen verarbeiten und Transaktionen steuern – unabhängig vom konkreten Datenbankprodukt.

| Abschnitt                        | Dauer |
|----------------------------------|-------|
| JDBC Architecture                | 36 m  |
| Statement and PreparedStatement  | 19 m  |
| ResultSet                        | 15 m  |
| Transactions                     | 14 m  |
| Batch Operations                 | 10 m  |
| Practice 17-1                    | 25 m  |
| **Gesamt**                       | **119 m** |

> **Skill Check: JDBC** – mind. 80 % erforderlich, um das Modul abzuschließen.

---

## 1. JDBC-Architektur

### 1.1 Schichtenmodell

JDBC trennt die Anwendungslogik von der datenbankspezifischen Implementierung durch ein zweischichtiges Treibermodell:

```
┌─────────────────────────────────┐
│       Java-Anwendung            │
│   (java.sql / javax.sql)        │
├─────────────────────────────────┤
│       JDBC Driver Manager       │
│   (java.sql.DriverManager)      │
├─────────────────────────────────┤
│       JDBC-Treiber (JAR)        │
│  (herstellerspezifisch)         │
├─────────────────────────────────┤
│       Datenbank                 │
└─────────────────────────────────┘
```

| JDBC-Klasse / Interface    | Verantwortlichkeit                                        |
|----------------------------|-----------------------------------------------------------|
| `DriverManager`            | Treiber registrieren, Verbindung herstellen               |
| `Connection`               | Repräsentiert eine aktive DB-Verbindung                   |
| `Statement`                | Einfache SQL-Anweisung ohne Parameter                     |
| `PreparedStatement`        | Vorkompilierte SQL-Anweisung mit Platzhaltern             |
| `CallableStatement`        | Aufruf von gespeicherten Prozeduren                       |
| `ResultSet`                | Ergebnismenge einer SELECT-Abfrage                        |
| `ResultSetMetaData`        | Metadaten über Spalten einer Ergebnismenge                |
| `DatabaseMetaData`         | Metadaten über die Datenbankverbindung                    |
| `SQLException`             | Datenbankfehler-Ausnahme (geprüfte Exception)             |

### 1.2 Treibertypen

| Typ | Bezeichnung                  | Beschreibung                                     |
|-----|------------------------------|--------------------------------------------------|
| 1   | JDBC-ODBC Bridge             | Veraltet, über Windows-ODBC-Schicht              |
| 2   | Native-API                   | Teilweise nativ, plattformabhängig               |
| 3   | Network Protocol             | Reines Java, Middleware-basiert                  |
| 4   | Thin Driver (Pure Java)      | Direkte TCP-Verbindung, empfohlen                |

Moderne Projekte verwenden ausschließlich Typ-4-Treiber (z. B. `postgresql-42.x.jar`, `ojdbc11.jar`).

### 1.3 Treiber laden und registrieren

Seit Java 6 nutzt JDBC den **ServiceLoader**-Mechanismus: Ein im JAR enthaltenes `META-INF/services/java.sql.Driver`-File registriert den Treiber automatisch. Ein expliziter `Class.forName()`-Aufruf ist nicht mehr nötig.

```java
// Automatische Registrierung (Java 6+) – kein Class.forName() erforderlich
// Der Treiber wird beim ersten DriverManager.getConnection()-Aufruf geladen.

// Explizite Registrierung (Legacy-Code)
Class.forName("org.postgresql.Driver");
```

---

## 2. JDBC-URL-Formate

Die JDBC-URL identifiziert den Treiber, den Host, den Port und die Datenbank.

```
jdbc:<subprotocol>://<host>:<port>/<database>[?parameter=wert&...]
```

| Datenbank          | Beispiel-URL                                                        |
|--------------------|---------------------------------------------------------------------|
| Oracle (SID)       | `jdbc:oracle:thin:@localhost:1521:ORCL`                             |
| Oracle (Service)   | `jdbc:oracle:thin:@//localhost:1521/orclpdb1`                       |
| PostgreSQL         | `jdbc:postgresql://localhost:5432/mydb`                             |
| MySQL / MariaDB    | `jdbc:mysql://localhost:3306/mydb?useSSL=true`                      |
| H2 (In-Memory)     | `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`                              |
| H2 (Datei)         | `jdbc:h2:~/data/testdb`                                             |
| SQLite             | `jdbc:sqlite:/home/user/data.db`                                    |
| SQL Server         | `jdbc:sqlserver://localhost:1433;databaseName=mydb`                 |

### 2.1 Verbindung herstellen

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class VerbindungsBeispiel {

    private static final String URL      = "jdbc:postgresql://localhost:5432/schulung";
    private static final String BENUTZER = "java_user";
    private static final String PASSWORT = "geheim";

    public static void main(String[] args) {
        // try-with-resources schliesst die Verbindung automatisch
        try (Connection conn = DriverManager.getConnection(URL, BENUTZER, PASSWORT)) {
            System.out.println("Verbunden mit: " + conn.getMetaData().getURL());
            System.out.println("Datenbank:     " + conn.getCatalog());
            System.out.println("Treiber:       " + conn.getMetaData().getDriverName());
        } catch (SQLException e) {
            System.err.println("Verbindung fehlgeschlagen: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
        }
    }
}
```

### 2.2 Connection-Properties

```java
import java.util.Properties;

Properties props = new Properties();
props.setProperty("user",     "java_user");
props.setProperty("password", "geheim");
props.setProperty("ssl",      "true");
props.setProperty("loginTimeout", "10");

try (Connection conn = DriverManager.getConnection(
        "jdbc:postgresql://localhost:5432/schulung", props)) {
    // ...
}
```

---

## 3. Statement – einfache SQL-Anweisungen

`Statement` wird für statische SQL-Anweisungen ohne Parameter verwendet. Es eignet sich vor allem für DDL-Befehle und einfache, parameterfreie Abfragen.

```java
import java.sql.*;

try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
     Statement stmt = conn.createStatement()) {

    // DDL – Tabelle anlegen
    stmt.executeUpdate("""
        CREATE TABLE IF NOT EXISTS produkt (
            id      SERIAL PRIMARY KEY,
            name    VARCHAR(100) NOT NULL,
            preis   NUMERIC(10,2),
            aktiv   BOOLEAN DEFAULT TRUE
        )
        """);

    // DML – Datensatz einfuegen
    int betroffeneZeilen = stmt.executeUpdate(
        "INSERT INTO produkt (name, preis) VALUES ('Kaffee', 3.99)");
    System.out.println("Eingefuegt: " + betroffeneZeilen);

    // DQL – Abfrage ausfuehren
    ResultSet rs = stmt.executeQuery("SELECT id, name, preis FROM produkt");
    while (rs.next()) {
        System.out.printf("%-5d %-20s %.2f%n",
            rs.getInt("id"), rs.getString("name"), rs.getDouble("preis"));
    }
}
```

> **Warnung:** `Statement` niemals mit Benutzereingaben konkatenieren! Das oeffnet SQL-Injection-Angriffe. Stattdessen immer `PreparedStatement` verwenden.

### 3.1 execute() vs. executeQuery() vs. executeUpdate()

| Methode             | Rueckgabetyp | Verwendung                                   |
|---------------------|-------------|----------------------------------------------|
| `executeQuery()`    | `ResultSet` | SELECT-Anweisungen                           |
| `executeUpdate()`   | `int`       | INSERT / UPDATE / DELETE / DDL               |
| `execute()`         | `boolean`   | Unbekannter Typ; `true` = ResultSet vorhanden |

---

## 4. PreparedStatement – sichere parametrisierte Abfragen

`PreparedStatement` sendet die SQL-Vorlage einmal an die Datenbank, die sie vorkompiliert. Wiederholte Aufrufe mit unterschiedlichen Parametern sind effizienter und vor SQL-Injection geschützt.

### 4.1 Platzhalter und Parameter-Binding

```java
import java.sql.*;
import java.math.BigDecimal;

public class PreparedStatementBeispiel {

    public static void produktEinfuegen(Connection conn,
                                        String name,
                                        BigDecimal preis) throws SQLException {

        String sql = "INSERT INTO produkt (name, preis, aktiv) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);         // Position 1: name
            pstmt.setBigDecimal(2, preis);    // Position 2: preis
            pstmt.setBoolean(3, true);        // Position 3: aktiv

            int zeilen = pstmt.executeUpdate();
            System.out.println("Eingefuegt: " + zeilen + " Zeile(n)");

            // Generierte Primaerschluessel abfragen
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Neue ID: " + keys.getLong(1));
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            produktEinfuegen(conn, "Espresso",  new BigDecimal("2.50"));
            produktEinfuegen(conn, "Cappuccino", new BigDecimal("3.80"));
        }
    }
}
```

### 4.2 Setter-Methoden im Überblick

| SQL-Typ          | Java-Typ            | PreparedStatement-Methode               |
|------------------|---------------------|-----------------------------------------|
| `VARCHAR`        | `String`            | `setString(int, String)`               |
| `INTEGER`        | `int`               | `setInt(int, int)`                     |
| `BIGINT`         | `long`              | `setLong(int, long)`                   |
| `NUMERIC`        | `BigDecimal`        | `setBigDecimal(int, BigDecimal)`        |
| `BOOLEAN`        | `boolean`           | `setBoolean(int, boolean)`             |
| `DATE`           | `java.sql.Date`     | `setDate(int, Date)`                   |
| `TIMESTAMP`      | `java.sql.Timestamp`| `setTimestamp(int, Timestamp)`         |
| `BLOB`           | `InputStream`       | `setBinaryStream(int, InputStream)`    |
| `CLOB`           | `Reader`            | `setCharacterStream(int, Reader)`      |
| `NULL`           | –                   | `setNull(int, Types.VARCHAR)`          |

### 4.3 SQL-Injection verhindern

```java
// GEFAEHRLICH – SQL-Injection moeglich
String unsicher = "SELECT * FROM benutzer WHERE name = '" + eingabe + "'";
// Eingabe: admin' OR '1'='1  -> gibt alle Zeilen zurueck!

// SICHER – PreparedStatement
String sicher = "SELECT * FROM benutzer WHERE name = ?";
try (PreparedStatement ps = conn.prepareStatement(sicher)) {
    ps.setString(1, eingabe); // Eingabe wird als Literal behandelt, nicht als SQL
    ResultSet rs = ps.executeQuery();
}
```

### 4.4 Abfrage mit PreparedStatement

```java
public List<Produkt> produkteSuchen(Connection conn, String namensFilter,
                                    BigDecimal maxPreis) throws SQLException {

    String sql = """
        SELECT id, name, preis, aktiv
        FROM produkt
        WHERE name ILIKE ?
          AND preis <= ?
          AND aktiv = TRUE
        ORDER BY preis
        """;

    List<Produkt> ergebnis = new ArrayList<>();

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, "%" + namensFilter + "%");
        pstmt.setBigDecimal(2, maxPreis);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ergebnis.add(new Produkt(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("preis"),
                    rs.getBoolean("aktiv")
                ));
            }
        }
    }
    return ergebnis;
}
```

---

## 5. ResultSet – Ergebnismengen navigieren

Ein `ResultSet` zeigt auf einen Cursor, der initial **vor** der ersten Zeile steht. Mit `next()` wird die Zeile um eins vorgerückt.

### 5.1 Vorwärtsnavigation (Standard)

```java
try (PreparedStatement pstmt = conn.prepareStatement(
        "SELECT * FROM produkt ORDER BY id");
     ResultSet rs = pstmt.executeQuery()) {

    // Spaltennummer (1-basiert) oder Spaltenname
    while (rs.next()) {
        int     id    = rs.getInt(1);
        String  name  = rs.getString("name");
        double  preis = rs.getDouble("preis");
        boolean aktiv = rs.getBoolean("aktiv");

        // NULL-Pruefung – wasNull() nach dem Getter aufrufen
        if (rs.wasNull()) {
            System.out.println(name + ": kein Preis angegeben");
        } else {
            System.out.printf("[%d] %-20s %.2f EUR %s%n",
                id, name, preis, aktiv ? "aktiv" : "inaktiv");
        }
    }
}
```

### 5.2 Scrollbares ResultSet

```java
// TYPE_SCROLL_INSENSITIVE: Scrollbar, keine Live-Updates
// CONCUR_READ_ONLY: Nur lesend
try (Statement stmt = conn.createStatement(
        ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
     ResultSet rs = stmt.executeQuery("SELECT * FROM produkt ORDER BY id")) {

    // Zur letzten Zeile springen
    if (rs.last()) {
        System.out.println("Letzte ID: " + rs.getInt("id"));
        System.out.println("Anzahl Zeilen: " + rs.getRow());
    }

    // Zurueck zur ersten Zeile
    rs.beforeFirst();
    while (rs.next()) {
        System.out.println(rs.getString("name"));
    }

    // Direkt zu Zeile 3 springen
    if (rs.absolute(3)) {
        System.out.println("Zeile 3: " + rs.getString("name"));
    }
}
```

### 5.3 ResultSet-Typen im Überblick

| Konstante                        | Navigation  | Sichtbarkeit von Aenderungen |
|----------------------------------|-------------|------------------------------|
| `TYPE_FORWARD_ONLY` (Standard)   | Nur vorwärts | –                           |
| `TYPE_SCROLL_INSENSITIVE`        | Beliebig    | Nein (Snapshot)              |
| `TYPE_SCROLL_SENSITIVE`          | Beliebig    | Ja (Live)                    |

| Konstante          | Schreibbarkeit                          |
|--------------------|-----------------------------------------|
| `CONCUR_READ_ONLY` | Nur lesen                               |
| `CONCUR_UPDATABLE` | Zeilen im ResultSet direkt änderbar     |

### 5.4 ResultSetMetaData

```java
ResultSetMetaData meta = rs.getMetaData();
int spaltenAnzahl = meta.getColumnCount();

System.out.printf("%-5s %-20s %-15s %-10s%n",
    "#", "Spalte", "Typ", "Nullable");
System.out.println("-".repeat(55));

for (int i = 1; i <= spaltenAnzahl; i++) {
    System.out.printf("%-5d %-20s %-15s %-10s%n",
        i,
        meta.getColumnName(i),
        meta.getColumnTypeName(i),
        meta.isNullable(i) == ResultSetMetaData.columnNullable ? "JA" : "NEIN");
}
```

---

## 6. Transaktionen

Standardmäßig arbeitet JDBC im **Auto-Commit-Modus**: Jede SQL-Anweisung wird sofort als eigene Transaktion commitet. Für mehrstufige Operationen muss Auto-Commit deaktiviert werden.

### 6.1 Manuelles Commit und Rollback

```java
public void geldTransfer(Connection conn,
                          int vonKontoId, int aufKontoId,
                          BigDecimal betrag) throws SQLException {

    conn.setAutoCommit(false); // Transaktion beginnen

    try {
        // Schritt 1: Betrag abbuchen
        try (PreparedStatement abbuchung = conn.prepareStatement(
                "UPDATE konto SET saldo = saldo - ? WHERE id = ?")) {
            abbuchung.setBigDecimal(1, betrag);
            abbuchung.setInt(2, vonKontoId);
            int zeilen = abbuchung.executeUpdate();
            if (zeilen != 1) throw new SQLException("Konto " + vonKontoId + " nicht gefunden");
        }

        // Schritt 2: Saldo pruefen (Deckung sicherstellen)
        try (PreparedStatement saldoPruefung = conn.prepareStatement(
                "SELECT saldo FROM konto WHERE id = ?")) {
            saldoPruefung.setInt(1, vonKontoId);
            try (ResultSet rs = saldoPruefung.executeQuery()) {
                if (rs.next() && rs.getBigDecimal("saldo").compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("Unzureichende Deckung auf Konto " + vonKontoId);
                }
            }
        }

        // Schritt 3: Betrag gutschreiben
        try (PreparedStatement gutschrift = conn.prepareStatement(
                "UPDATE konto SET saldo = saldo + ? WHERE id = ?")) {
            gutschrift.setBigDecimal(1, betrag);
            gutschrift.setInt(2, aufKontoId);
            int zeilen = gutschrift.executeUpdate();
            if (zeilen != 1) throw new SQLException("Konto " + aufKontoId + " nicht gefunden");
        }

        conn.commit(); // Alle Schritte erfolgreich
        System.out.println("Transfer erfolgreich: " + betrag + " EUR");

    } catch (SQLException e) {
        conn.rollback(); // Alle Aenderungen rueckgaengig machen
        System.err.println("Transfer fehlgeschlagen, Rollback durchgefuehrt: " + e.getMessage());
        throw e;
    } finally {
        conn.setAutoCommit(true); // Standard-Modus wiederherstellen
    }
}
```

### 6.2 Savepoints

Savepoints erlauben partielles Rollback innerhalb einer Transaktion.

```java
conn.setAutoCommit(false);
Savepoint sp1 = null;

try {
    // Phase 1
    stmt.executeUpdate("INSERT INTO log (msg) VALUES ('Phase 1')");
    sp1 = conn.setSavepoint("nach_phase1");

    // Phase 2
    stmt.executeUpdate("INSERT INTO log (msg) VALUES ('Phase 2')");
    Savepoint sp2 = conn.setSavepoint("nach_phase2");

    // Phase 3 – schlaegt fehl
    stmt.executeUpdate("UPDATE nicht_vorhandene_tabelle SET x = 1");

    conn.commit();

} catch (SQLException e) {
    if (sp1 != null) {
        // Nur Phase 3 rueckgaengig machen, Phase 1 und 2 behalten
        conn.rollback(sp1);
        conn.commit(); // Phase 1 commiten
        System.out.println("Partielles Commit bis Savepoint sp1");
    } else {
        conn.rollback();
    }
}
```

### 6.3 Transaktions-Isolationslevels

```java
// Isolationslevel setzen (vor dem ersten Statement)
conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
```

| Konstante                        | Wert | Dirty Read | Non-Repeatable | Phantom |
|----------------------------------|------|------------|----------------|---------|
| `TRANSACTION_READ_UNCOMMITTED`   | 1    | Ja         | Ja             | Ja      |
| `TRANSACTION_READ_COMMITTED`     | 2    | Nein       | Ja             | Ja      |
| `TRANSACTION_REPEATABLE_READ`    | 4    | Nein       | Nein           | Ja      |
| `TRANSACTION_SERIALIZABLE`       | 8    | Nein       | Nein           | Nein    |

---

## 7. Batch-Updates

Batch-Updates bündeln mehrere DML-Anweisungen in einem einzigen Netzwerk-Round-Trip zur Datenbank.

### 7.1 Statement-Batch

```java
try (Statement stmt = conn.createStatement()) {
    conn.setAutoCommit(false);

    stmt.addBatch("INSERT INTO log (msg) VALUES ('Eintrag 1')");
    stmt.addBatch("INSERT INTO log (msg) VALUES ('Eintrag 2')");
    stmt.addBatch("INSERT INTO log (msg) VALUES ('Eintrag 3')");
    stmt.addBatch("UPDATE konfiguration SET wert = 'neu' WHERE schluessel = 'version'");

    int[] ergebnisse = stmt.executeBatch();
    conn.commit();

    for (int i = 0; i < ergebnisse.length; i++) {
        System.out.println("Statement " + (i + 1) + ": " + ergebnisse[i] + " Zeile(n)");
    }
}
```

### 7.2 PreparedStatement-Batch (empfohlen)

```java
String sql = "INSERT INTO produkt (name, preis) VALUES (?, ?)";

try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    conn.setAutoCommit(false);

    String[][] produkte = {
        {"Tee",       "1.99"},
        {"Wasser",    "0.99"},
        {"Saft",      "2.49"},
        {"Limo",      "1.79"}
    };

    for (String[] p : produkte) {
        pstmt.setString(1, p[0]);
        pstmt.setBigDecimal(2, new BigDecimal(p[1]));
        pstmt.addBatch();           // Zum Batch hinzufuegen
    }

    int[] ergebnisse = pstmt.executeBatch();
    conn.commit();

    long gesamt = Arrays.stream(ergebnisse).sum();
    System.out.println("Gesamt eingefuegt: " + gesamt);
}
```

### 7.3 Batch mit Fehlerbehandlung

```java
try {
    int[] results = pstmt.executeBatch();
    conn.commit();
} catch (BatchUpdateException bue) {
    conn.rollback();
    System.err.println("Batch-Fehler bei Index: ");
    int[] updateCounts = bue.getUpdateCounts();
    for (int i = 0; i < updateCounts.length; i++) {
        if (updateCounts[i] == Statement.EXECUTE_FAILED) {
            System.err.println("  Statement " + i + " fehlgeschlagen");
        }
    }
}
```

---

## 8. try-with-resources für JDBC-Ressourcen

Alle JDBC-Objekte (`Connection`, `Statement`, `ResultSet`) implementieren `AutoCloseable`. try-with-resources schließt sie in umgekehrter Reihenfolge automatisch.

### 8.1 Korrekte Ressourcenverwaltung

```java
public List<String> alleProduktNamen(String url, String user, String pass)
        throws SQLException {

    List<String> namen = new ArrayList<>();

    // Aeussere try-with-resources: Connection
    try (Connection conn = DriverManager.getConnection(url, user, pass);
         // Innere Ressourcen: Statement und ResultSet
         PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT name FROM produkt WHERE aktiv = TRUE ORDER BY name");
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            namen.add(rs.getString("name"));
        }
    }
    // conn, pstmt, rs werden automatisch und sicher geschlossen

    return namen;
}
```

### 8.2 Probleme mit verschachtelten try-with-resources bei Transaktionen

```java
// Muster: Transaktion + try-with-resources korrekt kombinieren
public void transaktionMitCleanup(String url, String user, String pass)
        throws SQLException {

    try (Connection conn = DriverManager.getConnection(url, user, pass)) {
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps1 = conn.prepareStatement(
                    "UPDATE konto SET saldo = saldo - 100 WHERE id = 1");
                 PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE konto SET saldo = saldo + 100 WHERE id = 2")) {

                ps1.executeUpdate();
                ps2.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }
}
```

---

## 9. DataSource und Connection Pooling

Für Produktionsanwendungen ist `DriverManager` ungeeignet – jede Verbindung kostet Zeit. Stattdessen verwendet man einen **Connection Pool** über das `DataSource`-Interface.

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatenbankPool {

    private static final HikariDataSource POOL;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/schulung");
        config.setUsername("java_user");
        config.setPassword("geheim");
        config.setMaximumPoolSize(10);        // Max. 10 gleichzeitige Verbindungen
        config.setMinimumIdle(2);             // Min. 2 idle Verbindungen halten
        config.setConnectionTimeout(30_000);  // 30 Sekunden Timeout
        config.setIdleTimeout(600_000);       // 10 Minuten idle Timeout
        POOL = new HikariDataSource(config);
    }

    public static Connection verbindungHolen() throws SQLException {
        return POOL.getConnection(); // Aus dem Pool leihen
        // Beim Schliessen wird die Verbindung zurueck in den Pool gegeben
    }

    public static void poolSchliessen() {
        POOL.close();
    }
}
```

---

## 10. Vollstaendiges Praxisbeispiel

```java
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public record Produkt(int id, String name, BigDecimal preis, boolean aktiv) {}

public class ProduktRepository {

    private final String jdbcUrl;
    private final String benutzer;
    private final String passwort;

    public ProduktRepository(String jdbcUrl, String benutzer, String passwort) {
        this.jdbcUrl   = jdbcUrl;
        this.benutzer  = benutzer;
        this.passwort  = passwort;
    }

    /** Tabelle anlegen, falls nicht vorhanden */
    public void schemaErstellen() throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, benutzer, passwort);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS produkt (
                    id     SERIAL PRIMARY KEY,
                    name   VARCHAR(100) NOT NULL UNIQUE,
                    preis  NUMERIC(10,2),
                    aktiv  BOOLEAN DEFAULT TRUE
                )
                """);
        }
    }

    /** Neues Produkt speichern */
    public int speichern(String name, BigDecimal preis) throws SQLException {
        String sql = "INSERT INTO produkt (name, preis) VALUES (?, ?) RETURNING id";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, benutzer, passwort);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setBigDecimal(2, preis);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /** Alle aktiven Produkte laden */
    public List<Produkt> alleAktiven() throws SQLException {
        String sql = "SELECT id, name, preis, aktiv FROM produkt WHERE aktiv = TRUE";
        List<Produkt> liste = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, benutzer, passwort);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                liste.add(new Produkt(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("preis"),
                    rs.getBoolean("aktiv")
                ));
            }
        }
        return liste;
    }

    /** Preis aktualisieren */
    public boolean preisAktualisieren(int id, BigDecimal neuerPreis) throws SQLException {
        String sql = "UPDATE produkt SET preis = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, benutzer, passwort);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, neuerPreis);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() == 1;
        }
    }

    /** Produkt deaktivieren (soft delete) */
    public boolean deaktivieren(int id) throws SQLException {
        String sql = "UPDATE produkt SET aktiv = FALSE WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, benutzer, passwort);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() == 1;
        }
    }

    /** Massenimport per Batch */
    public int[] massenimport(List<String[]> produkte) throws SQLException {
        String sql = "INSERT INTO produkt (name, preis) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, benutzer, passwort);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (String[] p : produkte) {
                pstmt.setString(1, p[0]);
                pstmt.setBigDecimal(2, new BigDecimal(p[1]));
                pstmt.addBatch();
            }
            try {
                int[] ergebnisse = pstmt.executeBatch();
                conn.commit();
                return ergebnisse;
            } catch (BatchUpdateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        var repo = new ProduktRepository(
            "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1", "sa", "");

        repo.schemaErstellen();

        int id1 = repo.speichern("Arabica Bohnen", new BigDecimal("12.99"));
        int id2 = repo.speichern("Robusta Blend",  new BigDecimal("9.49"));
        System.out.println("Neue IDs: " + id1 + ", " + id2);

        repo.preisAktualisieren(id1, new BigDecimal("13.49"));

        System.out.println("\nAlle aktiven Produkte:");
        repo.alleAktiven().forEach(p ->
            System.out.printf("  [%d] %-25s %.2f EUR%n", p.id(), p.name(), p.preis()));

        var batch = List.of(
            new String[]{"Darjeeling", "8.99"},
            new String[]{"Oolong",     "11.50"}
        );
        repo.massenimport(batch);
        System.out.println("\nNach Batch-Import: " + repo.alleAktiven().size() + " Produkte");
    }
}
```

---

## 11. Häufige Fehler und Best Practices

| Problem                          | Ursache                                   | Loesung                                      |
|----------------------------------|-------------------------------------------|----------------------------------------------|
| `Connection leak`                | Verbindung nicht geschlossen              | try-with-resources verwenden                  |
| SQL-Injection                    | String-Konkatenation mit Benutzereingabe  | Immer `PreparedStatement` nutzen             |
| `ResultSet closed`               | Statement geschlossen vor ResultSet       | ResultSet vor Statement schliessen           |
| Langsame Massenoperationen       | Einzelne INSERT ohne Batch                | `addBatch()` / `executeBatch()` verwenden    |
| Dirty Reads                      | Falsches Isolationslevel                  | `TRANSACTION_READ_COMMITTED` oder hoeher     |
| `Too many connections`           | Kein Connection Pool                      | HikariCP, c3p0 oder DBCP2 einsetzen          |
| `NullPointerException` bei NULL  | DB-NULL ohne `wasNull()`-Pruefung         | `rs.wasNull()` nach primitiven Gettern pruefen |

---

## Zusammenfassung

- **JDBC-Architektur**: `DriverManager` stellt Verbindungen via JDBC-URL her; Typ-4-Treiber sind Standard.
- **Statement**: Fuer statische SQL ohne Parameter; niemals mit Benutzereingaben konkatenieren.
- **PreparedStatement**: Vorkompiliert, effizient, sicher gegen SQL-Injection durch Platzhalter (`?`).
- **ResultSet**: Cursor-basiert; `next()` bewegt vorwarts; scrollbare Typen ermoeglichen beliebige Navigation.
- **Transaktionen**: `setAutoCommit(false)` + `commit()` / `rollback()`; Savepoints fuer partielles Rollback.
- **Batch-Updates**: `addBatch()` + `executeBatch()` fuer effizienten Massenimport.
- **try-with-resources**: Alle JDBC-Ressourcen immer in try-with-resources verwenden.
- **Connection Pool**: In Produktion `DataSource` mit HikariCP statt `DriverManager` nutzen.
