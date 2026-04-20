package org.mave.rag_langchain4j.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mave.rag_langchain4j.dto.request.RagRequest;
import org.mave.rag_langchain4j.dto.response.AvailableFiles;
import org.mave.rag_langchain4j.dto.response.StreamEvent;
import org.mave.rag_langchain4j.mapper.AvailableFilesMapper;
import org.mave.rag_langchain4j.services.EmbeddingService;
import org.mave.rag_langchain4j.services.RagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000")
public class RagController {

    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final AvailableFilesMapper fileNameMapper;

    @PostMapping(value = "/ingest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ingest(@RequestParam MultipartFile file) {

        SseEmitter emitter = new SseEmitter(300_000L);

        String originalFilename = file.getOriginalFilename();

        ragService.ingest(file, originalFilename, emitter)
                .thenAccept(docId -> {
                    sendUpdate(emitter, "done", "✅ Ready! You can now ask questions.");
                    sendUpdate(emitter, "docId", docId);
                    emitter.complete();
                })
                .exceptionally(ex -> {
                    emitter.completeWithError(ex);
                    return null;
                });

        return emitter;
    }

    @GetMapping("/filenames")
    public ResponseEntity<AvailableFiles> getFilenames() {
        AvailableFiles availableFiles = fileNameMapper.toDto(
                embeddingService.getAllFilenames()
        );

        return ResponseEntity.ok(availableFiles);
    }

    @DeleteMapping("/document/{docId}")
    public void deleteDocument(@PathVariable String docId) {
        ragService.deleteDocument(docId);
    }

    @PostMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<StreamEvent> askQuestion(@RequestBody RagRequest request){
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
