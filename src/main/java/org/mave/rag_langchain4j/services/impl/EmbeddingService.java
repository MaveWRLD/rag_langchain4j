package org.mave.rag_langchain4j.services.impl;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.mave.rag_langchain4j.services.EmbeddingInterface;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService implements EmbeddingInterface {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;


    @Override
    public void store(List<TextSegment> segments) {

        if (segments == null || segments.isEmpty()) {
            log.warn("No segments provided for embedding/storage");
            return;
        }

        log.info("Starting embedding and storage for {} segments", segments.size());

        var embeddings = embeddingModel.embedAll(segments).content();

        if (embeddings.isEmpty()) {
            log.error("Embedding model returned no embeddings");
            throw new IllegalStateException("Failed to generate embeddings");
        }

        log.info("Embeddings generated successfully. Count: {}", embeddings.size());

        embeddingStore.addAll(embeddings, segments);

        log.info("Successfully stored {} embeddings into vector store", embeddings.size());

        if (log.isDebugEnabled()) {
            log.debug("First segment preview: {}",
                    segments.getFirst().text().substring(0,
                            Math.min(100, segments.getFirst().text().length())));

            log.debug("First embedding vector size: {}",
                    embeddings.getFirst().vector().length);
        }
    }
}
