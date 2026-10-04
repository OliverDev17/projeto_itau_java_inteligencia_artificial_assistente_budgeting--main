package dio.budgeting.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpeechRequest(
        String model,
        String input,
        String voice,
        @JsonProperty("response_format") String responseFormat) {

    public static SpeechRequest of(String text) {
        return new SpeechRequest("tts-1", text, "alloy", "mp3");
    }
}
