package arbeit.quiz;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Quiz {

    public static void main(String[] args) {
        // Defaults
        int count = 10;
        int timeSecs = 60;
        int[] modules = new int[0];
        String difficulty = "";
        boolean randomOrder = true;
        boolean showExplanation = true;
        boolean showScore = true;

        // Parse args
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--count", "-n" -> {
                    if (i + 1 < args.length) {
                        try { count = Integer.parseInt(args[++i]); }
                        catch (NumberFormatException e) { System.out.println("Warning: invalid value for --count, using default 10."); }
                    }
                }
                case "--time", "-t" -> {
                    if (i + 1 < args.length) {
                        try { timeSecs = Integer.parseInt(args[++i]); }
                        catch (NumberFormatException e) { System.out.println("Warning: invalid value for --time, using default 60."); }
                    }
                }
                case "--module", "-m" -> {
                    if (i + 1 < args.length) {
                        String csv = args[++i];
                        String[] parts = csv.split(",");
                        List<Integer> mods = new ArrayList<>();
                        for (String p : parts) {
                            try { mods.add(Integer.parseInt(p.trim())); }
                            catch (NumberFormatException e) { System.out.println("Warning: invalid module number '" + p.trim() + "', skipped."); }
                        }
                        modules = mods.stream().mapToInt(Integer::intValue).toArray();
                    }
                }
                case "--difficulty", "-d" -> {
                    if (i + 1 < args.length) difficulty = args[++i];
                }
                case "--order", "-o" -> {
                    if (i + 1 < args.length) {
                        String order = args[++i];
                        if ("sequential".equalsIgnoreCase(order)) randomOrder = false;
                        else if ("random".equalsIgnoreCase(order)) randomOrder = true;
                        else System.out.println("Warning: unknown --order value '" + order + "', using random.");
                    }
                }
                case "--show-explanation" -> {} // default: always on; kept for backwards compat
                case "--no-explanation" -> showExplanation = false;
                case "--no-score" -> showScore = false;
                default -> System.out.println("Warning: unknown argument '" + args[i] + "', skipped.");
            }
        }

        // Load questions
        InputStream is = Quiz.class.getResourceAsStream("/questions.json");
        if (is == null) {
            System.err.println("Error: /questions.json not found on classpath.");
            System.exit(1);
        }

        List<Question> all;
        try {
            all = QuizEngine.loadQuestions(is);
        } catch (Exception e) {
            System.err.println("Error reading questions.json: " + e.getMessage());
            System.exit(1);
            return; // unreachable, satisfies compiler
        }

        // Filter
        List<Question> pool = QuizEngine.filterQuestions(all, modules, difficulty);

        if (pool.isEmpty()) {
            System.err.println("Error: no questions match the given filter criteria.");
            System.exit(1);
        }

        if (pool.size() < count) {
            System.out.printf("Warning: only %d question(s) available (requested %d). Using all.%n",
                    pool.size(), count);
        }

        // Select
        List<Question> selected = QuizEngine.selectQuestions(pool, count, randomOrder);

        // Header
        System.out.println("=== Java SE 25 Quiz ===");
        System.out.printf("Questions : %d%n", selected.size());
        System.out.printf("Time limit: %s%n", timeSecs > 0 ? timeSecs + "s per question" : "none");
        System.out.printf("Modules   : %s%n",
                modules.length == 0 ? "all" : Arrays.toString(modules).replaceAll("[\\[\\]]", ""));
        System.out.printf("Difficulty: %s%n", difficulty.isBlank() ? "all" : difficulty);
        System.out.printf("Order     : %s%n", randomOrder ? "random" : "sequential");
        System.out.println("=======================");

        // Run
        QuizResult result = QuizEngine.runQuiz(selected, timeSecs, showExplanation);

        // Score
        if (showScore) {
            System.out.println();
            System.out.println("=======================");
            System.out.printf("Result: %d / %d (%.1f%%)%n",
                    result.correct(), result.total(), result.percentage());

            if (!result.wrongQuestions().isEmpty()) {
                System.out.println();
                System.out.println("Questions answered incorrectly:");
                for (Question q : result.wrongQuestions()) {
                    System.out.printf("  [ID %d | Module %d | %s] %s%n",
                            q.id(), q.module(), q.difficulty(),
                            q.question().length() > 80 ? q.question().substring(0, 80) + "..." : q.question());
                }
            }

            System.out.println();
            double pct = result.percentage();
            if (pct >= 80) {
                System.out.println("Excellent! Ready for the exam!");
            } else if (pct >= 60) {
                System.out.println("Good work! Keep practicing.");
            } else {
                System.out.println("Keep studying!");
            }
        }
    }
}
