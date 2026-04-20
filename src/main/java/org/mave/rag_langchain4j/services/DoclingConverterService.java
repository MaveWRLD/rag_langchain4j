package org.mave.rag_langchain4j.services;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


import ai.docling.serve.api.DoclingServeApi;
import ai.docling.serve.api.chunk.request.HierarchicalChunkDocumentRequest;
import ai.docling.serve.api.chunk.request.HybridChunkDocumentRequest;
import ai.docling.serve.api.chunk.response.Chunk;
import ai.docling.serve.api.chunk.response.ChunkDocumentResponse;
import ai.docling.serve.api.convert.request.options.ConvertDocumentOptions;
import ai.docling.serve.api.convert.request.options.OutputFormat;
import ai.docling.serve.api.convert.request.source.HttpSource;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoclingConverterService {

    private static final ConvertDocumentOptions DEFAULT_OPTIONS = ConvertDocumentOptions.builder()
            .toFormat(OutputFormat.MARKDOWN)
            .doOcr(false)
            .includeImages(false)
            .doTableStructure(true)
            .tableCellMatching(true)
            .abortOnError(false)
            .build();

    private final DoclingServeApi docling;


    public CompletionStage<List<TextSegment>> extract(
            URI uri,
            String originalFileName,
            String docId,
            SseEmitter emitter
    ) {

        ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger seconds = new AtomicInteger(0);

        heartbeat.scheduleAtFixedRate(() -> {
            seconds.addAndGet(5);
            sendUpdate(emitter, "⏳ Docling is processing your document... (" + seconds.get() + "s)");
        }, 5, 5, TimeUnit.SECONDS);

        return chunkFile(Paths.get(uri))
                .thenApply(response -> response.getChunks().stream()
                        .map(Chunk::getText)
                        .map(chunk -> TextSegment.from(
                                chunk,
                                Metadata.from(Map.of(
                                        "uri", uri.toString(),
                                        "filename", originalFileName,
                                        "docId", docId
                                ))
                        ))
                        .toList()
                )
                .whenComplete((result, ex) -> {
                    sendUpdate(emitter, "✂️ Chunking complete");
                    heartbeat.shutdown();

                    if (ex != null) {
                        sendUpdate(emitter, "❌ Error during chunking");
                    }
                });
    }

    private boolean isLocalFile(URI uri) {
        return "file".equals(uri.getScheme());
    }

    private CompletionStage<ChunkDocumentResponse> chunkFile(Path file) {

        var chunkRequest = HierarchicalChunkDocumentRequest.builder()
                .options(DEFAULT_OPTIONS)
                .build();

        return docling.chunkFilesWithHierarchicalChunkerAsync(chunkRequest, file);
    }

    private CompletionStage<ChunkDocumentResponse> chunkUri(URI uri) {

        var chunkRequest = HybridChunkDocumentRequest.builder()
                .source(HttpSource.builder()
                        .url(uri)
                        .build())
                .options(DEFAULT_OPTIONS)
                .build();

        return docling.chunkSourceWithHybridChunkerAsync(chunkRequest);
    }

    private void sendUpdate(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("step")
                    .data(message));
        } catch (IOException e) {
            log.error("Failed to send SSE update", e);
        }
    }
}
