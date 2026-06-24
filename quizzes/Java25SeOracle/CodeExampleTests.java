import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Testet alle evaluierbaren Code-Beispiele aus den Modul-MD-Dateien.
 * Ausführen: java CodeExampleTests.java
 */
class CodeExampleTests {

    static int passed = 0, failed = 0;

    static void ok(String label, Object actual, Object expected) {
        if (expected.equals(actual)) {
            System.out.printf("  ✓ %s%n", label);
            passed++;
        } else {
            System.out.printf("  ✗ %s — erwartet: %s, erhalten: %s%n", label, expected, actual);
            failed++;
        }
    }

    static void ok(String label, double actual, double expected) {
        if (Math.abs(actual - expected) < 1e-9) {
            System.out.printf("  ✓ %s%n", label);
            passed++;
        } else {
            System.out.printf("  ✗ %s — erwartet: %s, erhalten: %s%n", label, expected, actual);
            failed++;
        }
    }

    void main() {
        testModul02_Math();
        testModul02_Operatoren();
        testModul02_Casting();
        testModul02_Bitwise();
        testModul03_String();
        testModul03_DateTime();
        testModul03_DST();

        System.out.printf("%n═══ Ergebnis: %d/%d bestanden ═══%n", passed, passed + failed);
        if (failed > 0) System.exit(1);
    }

    // ── Modul 02: Math API ───────────────────────────────────────────────────
    static void testModul02_Math() {
        System.out.println("\n── Modul 02: Math API ──");
        ok("Math.abs(-5)",        Math.abs(-5),        5);
        ok("Math.abs(-3.7)",      Math.abs(-3.7),      3.7);
        ok("Math.min(3,7)",       Math.min(3, 7),      3);
        ok("Math.max(3,7)",       Math.max(3, 7),      7);
        ok("Math.ceil(2.1)",      Math.ceil(2.1),      3.0);
        ok("Math.floor(2.9)",     Math.floor(2.9),     2.0);
        ok("Math.round(2.5)",     (long) Math.round(2.5), 3L);
        ok("Math.round(2.4)",     (long) Math.round(2.4), 2L);
        ok("Math.pow(2,10)",      Math.pow(2, 10),     1024.0);
        ok("Math.sqrt(16)",       Math.sqrt(16),       4.0);
        ok("Math.cbrt(27)",       Math.cbrt(27),       3.0);
        ok("Math.log10(1000)",    Math.log10(1000),    3.0);
        ok("Math.log(Math.E)",    Math.log(Math.E),    1.0);
        ok("Math.floor(-2.1)",    Math.floor(-2.1),    -3.0);
        ok("Math.PI prefix",      (int)(Math.PI * 100), 314);
    }

    // ── Modul 02: Operatoren & Präzedenz ────────────────────────────────────
    static void testModul02_Operatoren() {
        System.out.println("\n── Modul 02: Operatoren & Präzedenz ──");
        ok("10 / 3 (int)",        10 / 3,              3);
        ok("10 % 3",              10 % 3,              1);
        ok("2 + 3 * 4",           2 + 3 * 4,           14);
        ok("(2 + 3) * 4",         (2 + 3) * 4,         20);
        ok("10 - 3 - 2",          10 - 3 - 2,          5);
        ok("true || false && false", true || false && false, true);  // && vor ||

        // Post/Prä Inkrement
        int a = 5;
        int b = a++;  // b=5, a=6
        ok("a++ post: b",         b, 5);
        ok("a++ post: a",         a, 6);
        int c = ++a;  // c=7, a=7
        ok("++a pre: c",          c, 7);
        ok("++a pre: a",          a, 7);

        // Compound assignment mit byte (implizites Narrowing)
        byte by = 10;
        by += 5;
        ok("byte += 5",           (int) by, 15);
    }

    // ── Modul 02: TypeCasting ────────────────────────────────────────────────
    static void testModul02_Casting() {
        System.out.println("\n── Modul 02: TypeCasting ──");
        ok("(byte)128 overflow",  (int)(byte)128,      -128);
        ok("(byte)300 overflow",  (int)(byte)300,      44);   // 300 - 256 = 44
        ok("(int)3.14 truncate",  (int)3.14,           3);
        ok("(int)3.9 truncate",   (int)3.9,            3);    // NICHT gerundet
        ok("char 'A' → int",      (int)'A',            65);
        ok("(char)66 → char",     (char)66,            'B');
        ok("10.0/3 double div",   10.0 / 3,            3.3333333333333335);
        ok("(double)10/3",        (double)10 / 3,      3.3333333333333335);
    }

    // ── Modul 02: Bitwise ────────────────────────────────────────────────────
    static void testModul02_Bitwise() {
        System.out.println("\n── Modul 02: Bitwise ──");
        int x = 5, y = 3;  // 5=0101, 3=0011
        ok("5 & 3",               x & y,  1);    // 0001
        ok("5 | 3",               x | y,  7);    // 0111
        ok("5 ^ 3",               x ^ y,  6);    // 0110
        ok("~5",                  ~x,      -6);
        ok("5 << 2",              x << 2,  20);   // × 4
        ok("5 >> 1",              x >> 1,  2);    // ÷ 2
        ok("5 & 3 (Modul2)",      5 & 3,   1);
    }

    // ── Modul 03: String ────────────────────────────────────────────────────
    static void testModul03_String() {
        System.out.println("\n── Modul 03: String ──");
        String a = "Hallo";
        String b = new String("Hallo");
        ok("== (Referenz)",           a == b,               false);
        ok(".equals (Inhalt)",        a.equals(b),          true);
        ok(".length()",               a.length(),           5);
        ok(".toUpperCase()",          a.toUpperCase(),      "HALLO");
        ok(".toLowerCase()",          a.toLowerCase(),      "hallo");
        ok(".trim()",                 "  hi  ".trim(),      "hi");
        ok(".strip() [Java 11]",      "  hi  ".strip(),     "hi");
        ok(".contains()",             a.contains("all"),    true);
        ok(".startsWith()",           a.startsWith("Ha"),   true);
        ok(".endsWith()",             a.endsWith("lo"),     true);
        ok(".replace()",              a.replace("l", "L"),  "HaLLo");
        ok(".substring(1,3)",         a.substring(1, 3),    "al");
        ok(".charAt(0)",              a.charAt(0),          'H');
        ok(".indexOf('l')",           a.indexOf('l'),       2);
        ok(".isBlank() [Java 11]",    "  ".isBlank(),       true);
        ok(".repeat(3) [Java 11]",    "ab".repeat(3),       "ababab");
        ok("String.valueOf(42)",      String.valueOf(42),   "42");
        ok("Integer.parseInt",        Integer.parseInt("42"), 42);

        // StringBuilder
        StringBuilder sb = new StringBuilder("Hallo");
        sb.append(" Welt").insert(0, ">>> ");
        ok("StringBuilder.append+insert", sb.toString(), ">>> Hallo Welt");
        ok("StringBuilder.length()",      sb.length(),   14);
        sb.reverse();
        ok("StringBuilder.reverse()",     sb.toString(), "tleW ollaH >>>");
    }

    // ── Modul 03: Date/Time ──────────────────────────────────────────────────
    static void testModul03_DateTime() {
        System.out.println("\n── Modul 03: Date/Time ──");
        LocalDate d = LocalDate.of(2025, 6, 1);
        ok("LocalDate.getYear()",        d.getYear(),        2025);
        ok("LocalDate.getMonthValue()",  d.getMonthValue(),  6);
        ok("LocalDate.getDayOfMonth()",  d.getDayOfMonth(),  1);
        ok("LocalDate.isLeapYear(2024)", LocalDate.of(2024, 1, 1).isLeapYear(), true);
        ok("LocalDate.isLeapYear(2025)", d.isLeapYear(),     false);
        ok("LocalDate.plusDays(5)",      d.plusDays(5),      LocalDate.of(2025, 6, 6));
        ok("LocalDate.minusMonths(1)",   d.minusMonths(1),   LocalDate.of(2025, 5, 1));
        ok("isBefore",                   d.isBefore(d.plusDays(1)), true);

        // Period
        LocalDate birth = LocalDate.of(1990, 5, 15);
        LocalDate ref   = LocalDate.of(2025, 5, 15);
        Period p = Period.between(birth, ref);
        ok("Period.getYears()",          p.getYears(),  35);

        // Duration
        LocalTime t1 = LocalTime.of(9, 0);
        LocalTime t2 = LocalTime.of(17, 30);
        Duration dur = Duration.between(t1, t2);
        ok("Duration.toHours()",         dur.toHours(), 8L);

        // Formatter
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        ok("format(dd.MM.yyyy)",         d.format(fmt),          "01.06.2025");
        ok("parse(dd.MM.yyyy)",          LocalDate.parse("01.06.2025", fmt), d);

        // Instant
        Instant epoch = Instant.EPOCH;
        ok("Instant.EPOCH epochSecond",  epoch.getEpochSecond(), 0L);
    }

    // ── Modul 03: DST / ZonedDateTime ────────────────────────────────────────
    static void testModul03_DST() {
        System.out.println("\n── Modul 03: DST / ZonedDateTime ──");
        ZoneId berlin = ZoneId.of("Europe/Berlin");

        // Vor Zeitumstellung (Winter): Offset +01:00
        ZonedDateTime vorher = ZonedDateTime.of(2023, 3, 26, 1, 30, 0, 0, berlin);
        ok("Winter-Offset +01:00",       vorher.getOffset().toString(), "+01:00");

        // Nach +1h: Lücke übersprungen → 3:30 Sommerzeit
        ZonedDateTime nachher = vorher.plusHours(1);
        ok("DST gap → 3:30",             nachher.getHour(), 3);
        ok("DST gap → Offset +02:00",    nachher.getOffset().toString(), "+02:00");

        // Überlappung (Sommerzeit → Winterzeit): 29.10.2023, 2:30
        ZonedDateTime ambig = ZonedDateTime.of(2023, 10, 29, 2, 30, 0, 0, berlin);
        ok("Overlap: earlier=Sommer",    ambig.getOffset().toString(), "+02:00");
        ZonedDateTime winter = ambig.withLaterOffsetAtOverlap();
        ok("Overlap: later=Winter",      winter.getOffset().toString(), "+01:00");

        // Selber Instant nach Konvertierung
        ZoneId ny = ZoneId.of("America/New_York");
        ZonedDateTime nyTime = ZonedDateTime.of(2025, 6, 1, 12, 0, 0, 0, ny);
        ZonedDateTime berlinTime = nyTime.withZoneSameInstant(berlin);
        ok("withZoneSameInstant: gleicher Instant",
                nyTime.toInstant().equals(berlinTime.toInstant()), true);
        ok("Berlin = NY + 6h im Sommer", berlinTime.getHour(), 18);
    }
}
