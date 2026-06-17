package arbeit.quiz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class QuizEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parse JSON of the shape {"questions":[...]} from the given InputStream.
     */
    public static List<Question> loadQuestions(InputStream is) throws IOException {
        JsonNode root = MAPPER.readTree(is);
        JsonNode questionsNode = root.get("questions");
        return MAPPER.convertValue(
                questionsNode,
                new TypeReference<List<Question>>() {}
        );
    }

    /**
     * Filter by module numbers and/or difficulty.
     * Pass null/empty modules array to skip module filter.
     * Pass null/empty difficulty to skip difficulty filter.
     */
    public static List<Question> filterQuestions(List<Question> all, int[] modules, String difficulty) {
        return all.stream()
                .filter(q -> {
                    if (modules != null && modules.length > 0) {
                        boolean moduleMatch = false;
                        for (int m : modules) {
                            if (q.module() == m) {
                                moduleMatch = true;
                                break;
                            }
                        }
                        if (!moduleMatch) return false;
                    }
                    if (difficulty != null && !difficulty.isBlank()) {
                        if (!q.difficulty().equalsIgnoreCase(difficulty)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Select up to count questions from pool, shuffled or sequential.
     */
    public static List<Question> selectQuestions(List<Question> pool, int count, boolean randomOrder) {
        List<Question> copy = new ArrayList<>(pool);
        if (randomOrder) {
            Collections.shuffle(copy);
        }
        int limit = Math.min(count, copy.size());
        return copy.subList(0, limit);
    }

    /**
     * Run the quiz interactively via System.in / System.out.
     * timeSecs <= 0 means no time limit.
     */
    public static QuizResult runQuiz(List<Question> questions, int timeSecs, boolean showExplanation) {
        Scanner scanner = new Scanner(System.in);
        int correct = 0;
        List<Question> wrongList = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);

            // Header
            System.out.println();
            System.out.printf("--- Question %d/%d  |  Module %d  |  %s ---%n",
                    i + 1, questions.size(), q.module(), q.difficulty());
            System.out.println(q.question());
            System.out.println();

            // Options
            char letter = 'A';
            for (String option : q.options()) {
                System.out.printf("  %c) %s%n", letter, option);
                letter++;
            }
            System.out.println();

            // Prompt
            if (q.answerCount() > 1) {
                System.out.printf("Select %d answers (e.g. A,C): ", q.answerCount());
            } else {
                System.out.print("Your answer: ");
            }

            if (timeSecs > 0) {
                System.out.printf("[Time limit: %d seconds] ", timeSecs);
            }
            System.out.flush();

            // Timer setup
            AtomicBoolean timedOut = new AtomicBoolean(false);
            Timer timer = null;
            if (timeSecs > 0) {
                timer = new Timer(true);
                final Timer finalTimer = timer;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timedOut.set(true);
                        System.out.println();
                        System.out.println("Time is up!");
                        System.out.flush();
                        // Interrupt the main thread so scanner.nextLine() unblocks on some JVMs
                        Thread.currentThread().interrupt();
                    }
                }, (long) timeSecs * 1000);
            }

            String line = "";
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                // May be interrupted by timer on some platforms; treat as empty
                line = "";
            }

            if (timer != null) {
                timer.cancel();
            }

            if (timedOut.get()) {
                System.out.println("  -> No answer recorded (timed out).");
                wrongList.add(q);
                printCorrectAnswer(q);
                if (showExplanation) printExplanation(q);
                continue;
            }

            // Parse input
            List<Integer> chosen = parseAnswer(line);

            // Evaluate
            List<Integer> expected = new ArrayList<>(q.correctAnswers());
            Collections.sort(chosen);
            Collections.sort(expected);

            if (chosen.equals(expected)) {
                correct++;
                System.out.println("  Correct!");
            } else {
                wrongList.add(q);
                System.out.println("  Wrong.");
                printCorrectAnswer(q);
            }

            if (showExplanation) printExplanation(q);
        }

        return new QuizResult(correct, questions.size(), wrongList);
    }

    // --- helpers ---

    private static List<Integer> parseAnswer(String line) {
        if (line == null || line.isBlank()) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (String part : line.split(",")) {
            String token = part.trim().toUpperCase();
            if (token.length() == 1) {
                char c = token.charAt(0);
                if (c >= 'A' && c <= 'E') {
                    indices.add(c - 'A');
                }
            }
        }
        return indices;
    }

    private static void printCorrectAnswer(Question q) {
        List<String> letters = q.correctAnswers().stream()
                .map(idx -> String.valueOf((char) ('A' + idx)))
                .collect(Collectors.toList());
        System.out.println("  Correct answer(s): " + String.join(", ", letters));
    }

    private static void printExplanation(Question q) {
        if (q.explanationEn() != null && !q.explanationEn().isBlank()) {
            System.out.println("  [EN] " + q.explanationEn());
        }
        if (q.explanationDe() != null && !q.explanationDe().isBlank()) {
            System.out.println("  [DE] " + q.explanationDe());
        }
    }
}
