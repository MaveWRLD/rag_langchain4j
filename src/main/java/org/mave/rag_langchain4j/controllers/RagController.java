package org.mave.rag_langchain4j.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mave.rag_langchain4j.dto.request.RagRequest;
import org.mave.rag_langchain4j.services.impl.RagService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;


import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000")
public class RagController {

    private final RagService ragService;

    @PostMapping(value = "/ingest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ingest(@RequestParam MultipartFile file) {

        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout

        String originalFilename = file.getOriginalFilename();

        CompletableFuture.runAsync(() -> {
            try {
                String docId = ragService.ingest(file, originalFilename, emitter);
                sendUpdate(emitter, "done", "✅ Ready! You can now ask questions.");
                sendUpdate(emitter, "docId", docId);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @DeleteMapping("/document/{docId}")
    public void deleteDocument(@PathVariable String docId) {
        ragService.deleteDocument(docId);
    }

    @PostMapping("/chat")
    public Flux<String> askQuestion(@RequestBody RagRequest request){
        return ragService.askQuestion(request);
    }

    private void sendUpdate(SseEmitter emitter, String eventType, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventType)
                    .data(message));
            log.info("SSE update sent: {}", message);
        } catch (IOException e) {
            log.error("Failed to send SSE update", e);
        }
    }
}
