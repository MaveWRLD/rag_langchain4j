package org.mave.rag_langchain4j.services.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final DocumentLoaderService loader;
    private final DocumentTransformer transform;
    private final DocumentSplitterService splitter;
    private final EmbeddingService embeddingService;



    public void ingest(String filePath) {

        log.info("Starting ingestion for file: {}", filePath);

        try {
            Document raw = loader.loadDocument(filePath);
            log.info("Document loaded successfully");

            Document cleaned = transform.transform(raw);
            log.info("Document transformed successfully");

            List<TextSegment> segments = splitter.split(cleaned);
            log.info("Document split into {} segments", segments.size());

            embeddingService.store(segments);
            log.info("Embeddings stored successfully");

            log.info("Ingestion completed successfully for file: {}", filePath);

        } catch (Exception e) {
            log.error("Ingestion failed for file: {}", filePath, e);
            throw e;
        }
    }
}
