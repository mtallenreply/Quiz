package arbeit.quiz;

import java.util.List;

public record QuizResult(int correct, int total, List<Question> wrongQuestions) {
    public double percentage() {
        return total == 0 ? 0 : (correct * 100.0) / total;
    }
}
