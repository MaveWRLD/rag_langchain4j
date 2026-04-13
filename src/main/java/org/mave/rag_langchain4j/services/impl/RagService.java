package org.mave.rag_langchain4j.services.impl;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mave.rag_langchain4j.dto.request.RagRequest;
import org.mave.rag_langchain4j.dto.response.RagResponse;
import org.mave.rag_langchain4j.mapper.RagResponseMapper;
import org.mave.rag_langchain4j.utils.FileStorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final FileStorageUtil fileStorageUtil;
    private final DocumentLoaderService documentLoaderService;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final Assistant assistant;

    public String ingest(MultipartFile file, String originalFileName, SseEmitter emitter) {

        Path filePath;
        try {
            sendUpdate(emitter, "step", "📄 Document received, saving...");
            filePath = fileStorageUtil.saveToTempFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String docId = UUID.randomUUID() + "_" + originalFileName;

        documentLoaderService.loadUrl(filePath.toUri(), originalFileName, docId, emitter);

        return docId;
    }

    public void deleteDocument(String docId) {
        embeddingStore.removeAll(
                MetadataFilterBuilder.metadataKey("docId").isEqualTo(docId)
        );
        log.info("Deleted all segments for docId: {}", docId);
    }

    public Flux<String> askQuestion(RagRequest request) {
        String userMessage = request.getMessage();

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        TokenStream tokenStream = assistant.chat(userMessage);

        tokenStream
                .onPartialResponse(sink::tryEmitNext)
                .onCompleteResponse(response -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
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
