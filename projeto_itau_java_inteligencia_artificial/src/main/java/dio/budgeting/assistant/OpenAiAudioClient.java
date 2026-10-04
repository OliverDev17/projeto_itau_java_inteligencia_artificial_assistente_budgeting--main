package dio.budgeting.assistant;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "openai-audio", url = "${spring.ai.openai.base-url:https://api.openai.com}")
public interface OpenAiAudioClient {

    @PostMapping(value = "/v1/audio/transcriptions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    TranscriptionResponse transcribe(
            @RequestPart("file") MultipartFile file,
            @RequestParam("model") String model);

    @PostMapping(value = "/v1/audio/speech", consumes = MediaType.APPLICATION_JSON_VALUE)
    byte[] textToSpeech(SpeechRequest request);
}
