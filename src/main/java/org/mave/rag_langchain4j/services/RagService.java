package org.mave.rag_langchain4j.services;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mave.rag_langchain4j.dto.request.RagRequest;
import org.mave.rag_langchain4j.dto.response.StreamEvent;
import org.mave.rag_langchain4j.utils.FileStorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final FileStorageUtil fileStorageUtil;
    private final DocumentLoaderService documentLoaderService;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final Assistant assistant;

    public CompletionStage<String> ingest(MultipartFile file, String originalFileName, SseEmitter emitter) {

        Path filePath;
        try {
            sendUpdate(emitter, "step", "📄 Document received, saving...");
            filePath = fileStorageUtil.saveToTempFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String docId = UUID.randomUUID() + "_" + originalFileName;

        return documentLoaderService
                .loadUrl(filePath.toUri(), originalFileName, docId, emitter)
                .thenApply(v -> docId);
    }

    public void deleteDocument(String docId) {
        embeddingStore.removeAll(
                MetadataFilterBuilder.metadataKey("docId").isEqualTo(docId)
        );
        log.info("Deleted all segments for docId: {}", docId);
    }

    public Flux<StreamEvent> askQuestion(RagRequest request) {
        Sinks.Many<StreamEvent> sink = Sinks.many().unicast().onBackpressureBuffer();



        TokenStream tokenStream = assistant.chat(request.getMessage());

        tokenStream
                .onRetrieved(contents -> {
                    List<Map<String, Object>> sources = contents.stream()
                            .map(content -> {
                                TextSegment segment = content.textSegment();
                                Map<String, Object> source = new HashMap<>();
                                source.put("text",     segment.text());
                                source.put("filename", segment.metadata().getString("filename"));
                                source.put("score",    content.metadata().get(ContentMetadata.SCORE));
                                return source;
                            })
                            .collect(Collectors.toList());

                    sink.tryEmitNext(new StreamEvent("sources", sources));
                })
                .onPartialResponse(token -> sink.tryEmitNext(new StreamEvent("token", token)))
                .onCompleteResponse(response -> sink.tryEmitNext(new StreamEvent("done", response)))
                .onError(sink::tryEmitError)
                .start();


        return sink.asFlux();
    }



    private void sendUpdate(SseEmitter emitter, String eventType, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventType)
                    .data(message));
        } catch (IOException e) {
            log.error("Failed to send SSE update", e);
        }
    }
}
