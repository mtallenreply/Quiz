package arbeit.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Question(
        int id,
        int module,
        String difficulty,
        String question,
        List<String> options,
        @JsonProperty("correct_answers") List<Integer> correctAnswers,
        @JsonProperty("answer_count") int answerCount,
        @JsonProperty("explanation_en") String explanationEn,
        @JsonProperty("explanation_de") String explanationDe) {}
