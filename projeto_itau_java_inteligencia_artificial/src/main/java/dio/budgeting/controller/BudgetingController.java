package dio.budgeting.controller;

import dio.budgeting.assistant.OpenAiAudioClient;
import dio.budgeting.assistant.SpeechRequest;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BudgetingController {

    private final ChatClient chatClient;
    private final OpenAiAudioClient audioClient;
    private final PersistTransactionUseCase persistUseCase;
    private final ListTransactionsByCategoryUseCase listByCategoryUseCase;
    private final ListTransactionsByEstablishmentUseCase listByEstablishmentUseCase;
    private final ListAllTransactionsUseCase listAllUseCase;
    private final DeleteTransactionUseCase deleteUseCase;

    // ── Endpoint de voz ──────────────────────────────────────────────
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> voice(@RequestParam("file") MultipartFile file) throws Exception {
        // 1. Transcrever áudio → texto
        var transcription = audioClient.transcribe(file, "whisper-1");
        String userText = transcription.text();

        // 2. Processar com ChatClient (Tool Calling)
        String responseText = chatClient.prompt()
                .user(userText)
                .call()
                .content();

        // 3. Converter resposta em áudio TTS
        byte[] audio = audioClient.textToSpeech(SpeechRequest.of(responseText));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(audio);
    }

    // ── Endpoint texto (chat sem áudio) ─────────────────────────────
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String userText = body.get("message");
        String response = chatClient.prompt()
                .user(userText)
                .call()
                .content();
        return ResponseEntity.ok(Map.of("response", response));
    }

    // ── CRUD REST ────────────────────────────────────────────────────
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> create(
            @RequestParam String description,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String establishment,
            @RequestParam(required = false) Instant occurredAt,
            @RequestParam(required = false) Category category) {
        Transaction saved = persistUseCase.execute(description, amount, establishment, occurredAt, category);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> listByCategory(
            @RequestParam(required = false) Category category) {
        if (category != null) {
            return ResponseEntity.ok(listByCategoryUseCase.execute(category));
        }
        return ResponseEntity.ok(listAllUseCase.execute());
    }

    @GetMapping("/transactions/by-establishment")
    public ResponseEntity<List<Transaction>> listByEstablishment(@RequestParam String name) {
        return ResponseEntity.ok(listByEstablishmentUseCase.execute(name));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
