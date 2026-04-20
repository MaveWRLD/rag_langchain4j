package org.mave.rag_langchain4j.services;


import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLoaderService {

    private final DoclingConverterService doclingConverter;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ExecutorService executor;


    public CompletionStage<Void> loadUrl(URI uri, String originalFileName, String docId, SseEmitter emitter) {

        var fullyQualifiedUri = (uri.getScheme() == null)
                ? Path.of(".", uri.getPath()).normalize().toAbsolutePath().toUri()
                : uri;

        return this.doclingConverter
                .extract(fullyQualifiedUri, originalFileName, docId, emitter)
                .thenCompose(segments -> {

                    sendUpdate(emitter, "📦 Embedding " + segments.size() + " segments in batches...");

                    int batchSize = 20;
                    int totalBatches = (segments.size() + batchSize - 1) / batchSize;

                    List<CompletableFuture<Void>> futures = IntStream.range(0, totalBatches)
                            .mapToObj(i -> {
                                int from = i * batchSize;
                                int to   = Math.min(from + batchSize, segments.size());
                                List<TextSegment> batch = segments.subList(from, to);

                                return CompletableFuture.runAsync(() -> embedBatch(batch), executor);
                            })
                            .toList();

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                })
                .thenRun(() -> {
                    sendUpdate(emitter, "✅ All segments embedded and stored");
                })
                .exceptionally(ex -> {
                    sendUpdate(emitter, "❌ Error: " + ex.getMessage());
                    throw new RuntimeException(ex);
                });
    }

    private void embedBatch(List<TextSegment> batch) {
        List<Embedding> embeddings = this.embeddingModel.embedAll(batch).content();
        for (int i = 0; i < embeddings.size(); i++) {
            this.embeddingStore.add(embeddings.get(i), batch.get(i));
        }
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