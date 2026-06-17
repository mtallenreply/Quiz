# Modul 18: Java Security – Sichere Java-Programmierung

## Übersicht

Java bietet eine umfangreiche Sicherheits-API im Paket `java.security` und `javax.crypto`. Dieses Modul behandelt kryptografische Grundlagen, sichere Schlüsselverwaltung und die häufigsten Sicherheitslücken in Java-Anwendungen.

| Abschnitt                  | Dauer |
|----------------------------|-------|
| Security Principles        | 19 m  |
| Hashing and SecureRandom   | 24 m  |
| Symmetric Encryption       | 20 m  |
| Key Management             | 15 m  |
| HTTPS                      | 10 m  |
| Common Vulnerabilities     | 15 m  |
| Practice 18-1              | 20 m  |
| **Gesamt**                 | **123 m** |

> **Skill Check: Security** – mind. 80 % erforderlich, um das Modul abzuschließen.

---

## 1. Sichere Programmierprinzipien

### 1.1 Defense in Depth

Sicherheit wird auf mehreren Ebenen implementiert – eine einzelne Schutzschicht reicht nie aus.

| Prinzip                        | Bedeutung                                                               |
|--------------------------------|-------------------------------------------------------------------------|
| **Least Privilege**            | Komponenten erhalten nur die minimal nötigen Rechte                     |
| **Defense in Depth**           | Mehrere unabhängige Sicherheitsschichten                                |
| **Fail Secure**                | Bei Fehlern in einen sicheren Zustand wechseln                          |
| **Input Validation**           | Alle Eingaben validieren, bevor sie verarbeitet werden                  |
| **Secure by Default**          | Unsichere Features müssen explizit aktiviert werden                     |
| **Open Design**                | Sicherheit darf nicht von der Geheimhaltung des Algorithmus abhängen    |
| **Complete Mediation**         | Zugriffskontrollen bei jedem Zugriff prüfen, nie cachen                 |

### 1.2 Java-Sicherheitsarchitektur

```
┌────────────────────────────────────────────┐
│           Anwendungsschicht                │
│  (Validierung, Authentifizierung, Autoris.)│
├────────────────────────────────────────────┤
│           Java Security API                │
│  java.security / javax.crypto / javax.net  │
├────────────────────────────────────────────┤
│           JCA / JCE Provider               │
│  SunJCE, BouncyCastle, PKCS#11            │
├────────────────────────────────────────────┤
│           JVM / OS                         │
└────────────────────────────────────────────┘
```

### 1.3 Java Cryptography Architecture (JCA)

```java
import java.security.Provider;
import java.security.Security;

// Verfuegbare Security-Provider anzeigen
for (Provider provider : Security.getProviders()) {
    System.out.println(provider.getName() + " v" + provider.getVersionStr());
}

// Alle unterstuetzten Algorithmen eines Typs auflisten
Security.getAlgorithms("MessageDigest").stream()
    .sorted()
    .forEach(alg -> System.out.println("  " + alg));
```

---

## 2. Hashing mit MessageDigest

Ein kryptografischer Hash ist eine Einwegfunktion: Aus dem Hashwert kann die Eingabe nicht rekonstruiert werden.

### 2.1 SHA-256 – Grundlegende Verwendung

```java
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashBeispiel {

    /**
     * Berechnet den SHA-256-Hash eines Strings und gibt ihn als Hex-String zurueck.
     */
    public static String sha256(String eingabe) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(eingabe.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

    /**
     * Vergleicht zwei Hash-Werte sicher (constant-time) gegen Timing-Angriffe.
     */
    public static boolean hashVergleichen(byte[] erwartet, byte[] aktuell) {
        return MessageDigest.isEqual(erwartet, aktuell);
    }

    public static void main(String[] args) throws Exception {
        String passwort = "MeinSicheresPasswort123!";
        String hash1 = sha256(passwort);
        String hash2 = sha256(passwort);
        String hash3 = sha256("AnderePasswort");

        System.out.println("Hash 1: " + hash1);
        System.out.println("Hash 2: " + hash2);
        System.out.println("Gleich: " + hash1.equals(hash2));   // true
        System.out.println("Hash 3: " + hash3);
        System.out.println("Gleich: " + hash1.equals(hash3));   // false
    }
}
```

### 2.2 Algorithmen im Vergleich

| Algorithmus  | Ausgabelaenge | Status         | Empfehlung                           |
|--------------|---------------|----------------|--------------------------------------|
| MD5          | 128 Bit       | Kompromittiert | Nicht mehr fuer Sicherheit verwenden |
| SHA-1        | 160 Bit       | Schwach        | Veraltet, nicht mehr empfohlen       |
| SHA-256      | 256 Bit       | Sicher         | Standard fuer allgemeine Verwendung  |
| SHA-384      | 384 Bit       | Sicher         | Hoehere Sicherheitsanforderungen     |
| SHA-512      | 512 Bit       | Sicher         | Maximale Sicherheit                  |
| SHA3-256     | 256 Bit       | Sicher         | Neuester Standard (Keccak-basiert)   |

### 2.3 Passwort-Hashing mit Salt

Ein einfacher Hash von Passwoertern ist anfaellig fuer Rainbow-Table-Angriffe. Ein Salt verhindert dies.

```java
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswortHasher {

    private static final int SALT_LAENGE = 32; // 256 Bit

    /** Generiert einen neuen Salt und berechnet SHA-256(salt || passwort). */
    public static String passwortHashen(String passwort) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LAENGE];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hash = md.digest(passwort.getBytes("UTF-8"));

        // Salt + Hash zusammen speichern
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        return saltBase64 + ":" + hashBase64;
    }

    /** Prueft ein Passwort gegen einen gespeicherten Hash. */
    public static boolean passwortPruefen(String passwort,
                                          String gespeicherterHash) throws Exception {
        String[] teile = gespeicherterHash.split(":");
        byte[] salt = Base64.getDecoder().decode(teile[0]);
        byte[] erwartetesHash = Base64.getDecoder().decode(teile[1]);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] berechneterHash = md.digest(passwort.getBytes("UTF-8"));

        return MessageDigest.isEqual(erwartetesHash, berechneterHash);
    }

    public static void main(String[] args) throws Exception {
        String passwort = "Sicher123!";
        String gespeichert = passwortHashen(passwort);
        System.out.println("Gespeichert: " + gespeichert);
        System.out.println("Korrekt:     " + passwortPruefen(passwort, gespeichert));
        System.out.println("Falsch:      " + passwortPruefen("FalschPasswort", gespeichert));
    }
}
```

> **Hinweis:** Fuer Passworte in Produktionssystemen sollten dedizierte Algorithmen wie **bcrypt**, **scrypt** oder **Argon2** verwendet werden (z. B. via Spring Security oder BouncyCastle). Diese sind bewusst langsam und widerstehen Brute-Force-Angriffen besser.

---

## 3. SecureRandom – Kryptografisch sichere Zufallszahlen

`java.util.Random` ist fuer Sicherheitszwecke ungeeignet – es ist vorhersagbar. `SecureRandom` nutzt OS-Entropiequellen.

```java
import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomBeispiel {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** Generiert einen kryptografisch sicheren Token. */
    public static String tokenGenerieren(int byteAnzahl) {
        byte[] bytes = new byte[byteAnzahl];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Generiert eine zufaellige PIN mit genau n Stellen. */
    public static String pinGenerieren(int stellen) {
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < stellen; i++) {
            pin.append(SECURE_RANDOM.nextInt(10));
        }
        return pin.toString();
    }

    /** Generiert ein zufaelliges Passwort aus einem Zeichenpool. */
    public static String passwortGenerieren(int laenge) {
        String zeichenPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder(laenge);
        for (int i = 0; i < laenge; i++) {
            int index = SECURE_RANDOM.nextInt(zeichenPool.length());
            sb.append(zeichenPool.charAt(index));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("Session Token:    " + tokenGenerieren(32));
        System.out.println("API Key:          " + tokenGenerieren(24));
        System.out.println("PIN (6-stellig):  " + pinGenerieren(6));
        System.out.println("Passwort:         " + passwortGenerieren(16));
    }
}
```

---

## 4. Symmetrische Verschlüsselung mit AES/GCM

### 4.1 AES-Grundlagen

AES (Advanced Encryption Standard) ist der empfohlene symmetrische Verschlüsselungsalgorithmus.

| Modus       | Bezeichnung                   | Authentifizierung | Empfehlung               |
|-------------|-------------------------------|-------------------|--------------------------|
| ECB         | Electronic Codebook           | Nein              | Nie verwenden            |
| CBC         | Cipher Block Chaining         | Nein              | Nur mit HMAC             |
| CTR         | Counter                       | Nein              | Mit separatem MAC        |
| GCM         | Galois/Counter Mode           | Ja (AEAD)         | Empfohlen                |
| CCM         | Counter with CBC-MAC          | Ja (AEAD)         | IoT-Einsatz              |

### 4.2 AES-GCM Verschlüsselung

```java
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

public class AesGcmVerschluesselung {

    private static final String ALGORITHMUS = "AES/GCM/NoPadding";
    private static final int IV_LAENGE     = 12;  // 96 Bit (GCM-Standard)
    private static final int TAG_LAENGE    = 128; // 128 Bit Authentifizierungs-Tag

    /** Verschluesselt Klartext mit AES-GCM. */
    public static byte[] verschluesseln(byte[] klartext, SecretKey schluessel)
            throws Exception {

        // Zufaelliger Initialisierungsvektor (IV) fuer jede Verschluesselung
        byte[] iv = new byte[IV_LAENGE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHMUS);
        GCMParameterSpec paramSpec = new GCMParameterSpec(TAG_LAENGE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, schluessel, paramSpec);

        byte[] chiffrat = cipher.doFinal(klartext);

        // IV + Chiffrat zusammen zurueckgeben (IV wird zum Entschluesseln benoetigt)
        byte[] ergebnis = new byte[IV_LAENGE + chiffrat.length];
        System.arraycopy(iv,       0, ergebnis, 0,          IV_LAENGE);
        System.arraycopy(chiffrat, 0, ergebnis, IV_LAENGE,  chiffrat.length);
        return ergebnis;
    }

    /** Entschluesselt AES-GCM-Chiffrat (Format: IV || Chiffrat). */
    public static byte[] entschluesseln(byte[] ivUndChiffrat, SecretKey schluessel)
            throws Exception {

        // IV aus dem Anfang extrahieren
        byte[] iv       = new byte[IV_LAENGE];
        byte[] chiffrat = new byte[ivUndChiffrat.length - IV_LAENGE];
        System.arraycopy(ivUndChiffrat, 0,         iv,       0, IV_LAENGE);
        System.arraycopy(ivUndChiffrat, IV_LAENGE, chiffrat, 0, chiffrat.length);

        Cipher cipher = Cipher.getInstance(ALGORITHMUS);
        GCMParameterSpec paramSpec = new GCMParameterSpec(TAG_LAENGE, iv);
        cipher.init(Cipher.DECRYPT_MODE, schluessel, paramSpec);

        return cipher.doFinal(chiffrat); // Wirft AEADBadTagException bei Manipulation
    }

    public static void main(String[] args) throws Exception {
        // Schluessel generieren
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom()); // AES-256
        SecretKey schluessel = keyGen.generateKey();

        String nachricht = "Geheime Bankdaten: IBAN DE12 3456 7890 1234";
        byte[] klartext   = nachricht.getBytes("UTF-8");

        // Verschluesseln
        byte[] chiffrat = verschluesseln(klartext, schluessel);
        System.out.println("Chiffrat (Base64): " +
            Base64.getEncoder().encodeToString(chiffrat));

        // Entschluesseln
        byte[] wiederhergestellt = entschluesseln(chiffrat, schluessel);
        System.out.println("Klartext:          " + new String(wiederhergestellt, "UTF-8"));

        // Manipulationstest
        chiffrat[20] ^= 0xFF; // Bit flippen
        try {
            entschluesseln(chiffrat, schluessel);
        } catch (AEADBadTagException e) {
            System.out.println("Manipulation erkannt: " + e.getMessage());
        }
    }
}
```

### 4.3 KeyGenerator – Schlüssel erzeugen

```java
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public class SchluesselerzeugungBeispiel {

    public static SecretKey aes256Schluessel() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }

    public static SecretKey hmacSha256Schluessel() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }

    public static void main(String[] args) throws Exception {
        SecretKey aesKey = aes256Schluessel();
        System.out.println("AES-Key Algorithmus: " + aesKey.getAlgorithm());
        System.out.println("AES-Key Laenge:      " + aesKey.getEncoded().length * 8 + " Bit");
        System.out.println("AES-Key Format:      " + aesKey.getFormat());
    }
}
```

---

## 5. Schlüsselverwaltung mit KeyStore

Ein `KeyStore` ist ein sicherer Container fuer kryptografische Schluessel und Zertifikate.

### 5.1 KeyStore erstellen und verwenden

```java
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class KeyStoreBeispiel {

    private static final String KEYSTORE_DATEI = "sicherheit.jks";
    private static final char[] KEYSTORE_PASSWORT = "Passwort123!".toCharArray();
    private static final char[] SCHLUESSEL_PASSWORT = "SchluesselPW!".toCharArray();

    /** Erstellt einen neuen PKCS12-KeyStore und speichert einen AES-Schluessel. */
    public static void schluesselspeichernErstellen() throws Exception {
        // PKCS12 ist moderner Standard (statt JKS)
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null); // Neuen, leeren KeyStore erstellen

        // AES-Schluessel generieren
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();

        // Schluessel mit Alias und Passwort sichern
        KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(aesKey);
        KeyStore.ProtectionParameter schutz =
            new KeyStore.PasswordProtection(SCHLUESSEL_PASSWORT);
        ks.setEntry("mein-aes-schluessel", keyEntry, schutz);

        // KeyStore in Datei schreiben
        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_DATEI)) {
            ks.store(fos, KEYSTORE_PASSWORT);
        }
        System.out.println("KeyStore erstellt: " + KEYSTORE_DATEI);
    }

    /** Laedt einen Schluessel aus dem KeyStore. */
    public static SecretKey schluesselladen(String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_DATEI)) {
            ks.load(fis, KEYSTORE_PASSWORT);
        }

        KeyStore.ProtectionParameter schutz =
            new KeyStore.PasswordProtection(SCHLUESSEL_PASSWORT);
        KeyStore.SecretKeyEntry entry =
            (KeyStore.SecretKeyEntry) ks.getEntry(alias, schutz);

        return entry.getSecretKey();
    }

    public static void main(String[] args) throws Exception {
        schluesselspeichernErstellen();
        SecretKey key = schluesselladen("mein-aes-schluessel");
        System.out.println("Geladener Schluessel: " + key.getAlgorithm() +
            " (" + key.getEncoded().length * 8 + " Bit)");
    }
}
```

### 5.2 Asymmetrische Schlüsselpaare (RSA)

```java
import java.security.*;
import javax.crypto.Cipher;
import java.util.Base64;

public class RsaBeispiel {

    /** RSA-Schluessselpaar generieren. */
    public static KeyPair schluesselPaarGenerieren() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom()); // 2048 Bit minimum
        return generator.generateKeyPair();
    }

    /** Verschluesselt mit dem oeffentlichen Schluessel. */
    public static byte[] verschluesselnMitPublicKey(byte[] daten,
                                                     PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(daten);
    }

    /** Entschluesselt mit dem privaten Schluessel. */
    public static byte[] entschluesselnMitPrivateKey(byte[] chiffrat,
                                                       PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(chiffrat);
    }

    public static void main(String[] args) throws Exception {
        KeyPair keypair = schluesselPaarGenerieren();
        String nachricht = "Geheime Nachricht";
        byte[] chiffrat  = verschluesselnMitPublicKey(
            nachricht.getBytes("UTF-8"), keypair.getPublic());
        byte[] klartext  = entschluesselnMitPrivateKey(chiffrat, keypair.getPrivate());
        System.out.println("Original:    " + nachricht);
        System.out.println("Entschlüsselt: " + new String(klartext, "UTF-8"));
    }
}
```

---

## 6. HTTPS in Java

### 6.1 HTTPS-Verbindung mit HttpsURLConnection

```java
import javax.net.ssl.*;
import java.net.*;
import java.io.*;

public class HttpsBeispiel {

    public static String httpsGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(10_000);

        // TLS-Zertifikat pruefen
        System.out.println("Cipher Suite:  " + conn.getCipherSuite());
        System.out.println("Peer Principal:" + conn.getPeerPrincipal());

        int statusCode = conn.getResponseCode();
        System.out.println("Status: " + statusCode);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String zeile;
            while ((zeile = reader.readLine()) != null) {
                sb.append(zeile).append('\n');
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        String antwort = httpsGet("https://api.github.com");
        System.out.println(antwort.substring(0, Math.min(200, antwort.length())));
    }
}
```

### 6.2 TLS-Konfiguration mit SSLContext

```java
import javax.net.ssl.*;
import java.security.KeyStore;
import java.io.FileInputStream;

public class TlsKonfiguration {

    /** SSLContext mit eigenem TrustStore fuer self-signed Zertifikate. */
    public static SSLContext sslContextMitTrustStore(String trustStorePfad,
                                                      char[] passwort) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(trustStorePfad)) {
            trustStore.load(fis, passwort);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }
}
```

---

## 7. Häufige Sicherheitslücken

### 7.1 SQL-Injection

```java
// VERWUNDBAR
public boolean anmeldenUnsicher(Connection conn, String user, String pass)
        throws SQLException {
    String sql = "SELECT * FROM users WHERE name='" + user
               + "' AND pass='" + pass + "'";
    // Eingabe: user = "admin'--"
    // SQL wird: SELECT * FROM users WHERE name='admin'--' AND pass='...'
    // Kommentar (--) deaktiviert die Passwort-Pruefung!
    Statement stmt = conn.createStatement();
    return stmt.executeQuery(sql).next();
}

// SICHER
public boolean anmeldenSicher(Connection conn, String user, String pass)
        throws SQLException {
    String sql = "SELECT * FROM users WHERE name=? AND pass=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, user);
        ps.setString(2, pass);
        return ps.executeQuery().next();
    }
}
```

### 7.2 Path Traversal

```java
import java.io.File;
import java.nio.file.*;

public class PathTraversalSchutz {

    private static final Path ERLAUBTES_VERZEICHNIS =
        Path.of("/var/app/uploads").toAbsolutePath().normalize();

    // VERWUNDBAR
    public String dateiLesenUnsicher(String dateiname) throws Exception {
        // Eingabe: "../../etc/passwd" -> liest /etc/passwd!
        return Files.readString(Path.of("/var/app/uploads/" + dateiname));
    }

    // SICHER
    public String dateiLesenSicher(String dateiname) throws Exception {
        Path angefragt = ERLAUBTES_VERZEICHNIS
            .resolve(dateiname)
            .normalize()
            .toAbsolutePath();

        // Sicherstellen, dass der aufgeloeste Pfad im erlaubten Verzeichnis liegt
        if (!angefragt.startsWith(ERLAUBTES_VERZEICHNIS)) {
            throw new SecurityException("Path traversal erkannt: " + dateiname);
        }

        return Files.readString(angefragt);
    }
}
```

### 7.3 XSS (Cross-Site Scripting) – Ausgabe escapen

```java
public class XssSchutz {

    /** HTML-Sonderzeichen escapen, um XSS zu verhindern. */
    public static String htmlEscapen(String eingabe) {
        if (eingabe == null) return "";
        return eingabe
            .replace("&",  "&amp;")
            .replace("<",  "&lt;")
            .replace(">",  "&gt;")
            .replace("\"", "&quot;")
            .replace("'",  "&#x27;");
    }

    public static void main(String[] args) {
        String benutzereingabe = "<script>alert('XSS')</script>";
        System.out.println("Roh:      " + benutzereingabe);
        System.out.println("Escaped:  " + htmlEscapen(benutzereingabe));
        // Ausgabe: &lt;script&gt;alert(&#x27;XSS&#x27;)&lt;/script&gt;
    }
}
```

### 7.4 Unsichere Deserialisierung

```java
import java.io.*;

public class DeserialisierungsSchutz {

    // GEFAEHRLICH – beliebige Klassen koennen deserialisiert werden
    @SuppressWarnings("unchecked")
    public static <T> T deserialisiernUnsicher(byte[] daten) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(daten))) {
            return (T) ois.readObject(); // Kann Schadcode ausfuehren!
        }
    }

    // SICHER – ObjectInputFilter begrenzt erlaubte Klassen (Java 9+)
    @SuppressWarnings("unchecked")
    public static <T> T deserialisiernSicher(byte[] daten,
                                              Class<T> erlaubteKlasse) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(daten))) {

            // Filter: Nur die explizit erlaubte Klasse akzeptieren
            ois.setObjectInputFilter(info -> {
                Class<?> klasse = info.serialClass();
                if (klasse == null) return ObjectInputFilter.Status.ALLOWED;
                return klasse.equals(erlaubteKlasse)
                    ? ObjectInputFilter.Status.ALLOWED
                    : ObjectInputFilter.Status.REJECTED;
            });

            return (T) ois.readObject();
        }
    }

    // BESTE ALTERNATIVE: JSON statt Java-Serialisierung verwenden
    // (Jackson, Gson, JSON-B)
}
```

### 7.5 Sicherheitslücken-Übersicht

| Schwachstelle              | Angriffsvector                              | Gegenmassnahme                                   |
|----------------------------|---------------------------------------------|--------------------------------------------------|
| SQL-Injection              | Eingabe in SQL-Strings                      | PreparedStatement / parametrisierte Abfragen     |
| XSS                        | Eingabe in HTML-Ausgabe                     | HTML-Escaping (OWASP Java Encoder)               |
| Path Traversal             | `../` in Dateipfaden                        | Pfad normalisieren + Whitelist-Pruefung          |
| Unsichere Deserialisierung | Manipulierte serialisierte Objekte          | ObjectInputFilter / JSON statt Java-Serialisierung |
| Hardcoded Secrets          | Secrets im Quellcode                        | Umgebungsvariablen / Vault / KeyStore            |
| Schwache Kryptografie      | MD5/SHA-1/DES/ECB                           | SHA-256+, AES-GCM, bcrypt fuer Passworte        |
| Vorhersagbare Zufallszahlen| java.util.Random fuer Tokens               | SecureRandom                                     |
| SSRF                       | Server-seitige Anfragen an interne Dienste  | URL-Whitelist, Netzwerksegmentierung            |

---

## 8. HMAC – Nachrichtenintegritaet sichern

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

public class HmacBeispiel {

    /** Berechnet HMAC-SHA256 ueber eine Nachricht. */
    public static String hmacSha256(String nachricht, byte[] geheimKey) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(geheimKey, "HmacSHA256");
        mac.init(keySpec);
        byte[] hmac = mac.doFinal(nachricht.getBytes("UTF-8"));
        return HexFormat.of().formatHex(hmac);
    }

    /** Prueft die Integritaet einer Nachricht. */
    public static boolean integritaetPruefen(String nachricht, String erwartetHmac,
                                              byte[] geheimKey) throws Exception {
        String berechneterHmac = hmacSha256(nachricht, geheimKey);
        // Constant-time Vergleich gegen Timing-Angriffe
        return MessageDigest.isEqual(
            berechneterHmac.getBytes(), erwartetHmac.getBytes());
    }

    public static void main(String[] args) throws Exception {
        byte[] schluessel = "SuperGeheimesHMACPasswort".getBytes("UTF-8");
        String nachricht  = "Bestellnummer: 12345, Betrag: 99.99 EUR";

        String hmac = hmacSha256(nachricht, schluessel);
        System.out.println("HMAC: " + hmac);

        boolean gueltig = integritaetPruefen(nachricht, hmac, schluessel);
        System.out.println("Gueltig:          " + gueltig); // true

        boolean manipuliert = integritaetPruefen(
            nachricht + " MANIPULIERT", hmac, schluessel);
        System.out.println("Nach Manipulation: " + manipuliert); // false
    }
}
```

---

## 9. Vollstaendiges Sicherheits-Utility

```java
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

/**
 * Utility-Klasse fuer haeufig benoetigte Sicherheitsoperationen.
 * Alle Methoden sind thread-sicher.
 */
public final class SecurityUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String AES_ALGO    = "AES/GCM/NoPadding";
    private static final int IV_BYTES       = 12;
    private static final int TAG_BITS       = 128;

    private SecurityUtils() {} // Utility-Klasse, nicht instanziierbar

    /** Generiert n kryptografisch sichere Zufallsbytes, kodiert als Base64-URL. */
    public static String sicherenToken(int bytes) {
        byte[] buf = new byte[bytes];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** Erzeugt einen AES-256-Schluessel. */
    public static SecretKey aesSchluessel() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, RANDOM);
        return kg.generateKey();
    }

    /** AES-GCM-Verschluesselung; gibt Base64(IV||Chiffrat) zurueck. */
    public static String verschluesseln(String klartext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        RANDOM.nextBytes(iv);
        Cipher c = Cipher.getInstance(AES_ALGO);
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] ct = c.doFinal(klartext.getBytes("UTF-8"));
        byte[] out = new byte[IV_BYTES + ct.length];
        System.arraycopy(iv, 0, out, 0,        IV_BYTES);
        System.arraycopy(ct, 0, out, IV_BYTES, ct.length);
        return Base64.getEncoder().encodeToString(out);
    }

    /** AES-GCM-Entschluesselung; erwartet Base64(IV||Chiffrat). */
    public static String entschluesseln(String base64Chiffrat, SecretKey key) throws Exception {
        byte[] ivCt = Base64.getDecoder().decode(base64Chiffrat);
        byte[] iv   = new byte[IV_BYTES];
        byte[] ct   = new byte[ivCt.length - IV_BYTES];
        System.arraycopy(ivCt, 0,        iv, 0, IV_BYTES);
        System.arraycopy(ivCt, IV_BYTES, ct, 0, ct.length);
        Cipher c = Cipher.getInstance(AES_ALGO);
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        return new String(c.doFinal(ct), "UTF-8");
    }

    /** SHA-256-Hash als Hex-String. */
    public static String sha256(String eingabe) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256")
                         .digest(eingabe.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Token:      " + sicherenToken(32));
        System.out.println("SHA-256:    " + sha256("Hallo Welt"));

        SecretKey key   = aesSchluessel();
        String chiffrat = verschluesseln("Geheimnis", key);
        String klartext = entschluesseln(chiffrat, key);
        System.out.println("Chiffrat:   " + chiffrat);
        System.out.println("Klartext:   " + klartext);
    }
}
```

---

## Zusammenfassung

- **MessageDigest**: SHA-256 / SHA-512 fuer kryptografische Hashes; MD5/SHA-1 veraltet.
- **Salt**: Verhindert Rainbow-Table-Angriffe; fuer Passworte besser bcrypt/Argon2.
- **SecureRandom**: Fuer alle sicherheitsrelevanten Zufallszahlen statt `java.util.Random`.
- **AES-GCM**: Authentifizierte Verschluesselung; GCM erkennt Manipulation automatisch.
- **KeyStore**: Sicherer Container fuer Schluessel und Zertifikate (PKCS12-Format bevorzugen).
- **HTTPS**: `HttpsURLConnection` verifiziert TLS-Zertifikate automatisch.
- **SQL-Injection**: Immer `PreparedStatement` statt String-Konkatenation.
- **Path Traversal**: Pfade normalisieren und gegen erlaubtes Verzeichnis pruefen.
- **Deserialisierung**: `ObjectInputFilter` oder JSON statt Java-Serialisierung verwenden.
