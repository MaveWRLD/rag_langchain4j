package org.mave.rag_langchain4j.services.impl;


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

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLoaderService {

    private final DoclingConverterService doclingConverter;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;


    public void loadUrl(URI uri, String originalFileName, String docId, SseEmitter emitter) {
        var fullyQualifiedUri = (uri.getScheme() == null) ?
                Path.of(".", uri.getPath()).normalize().toAbsolutePath().toUri() :
                uri;

        log.info("Loading document from {}", fullyQualifiedUri);


        this.doclingConverter.extract(fullyQualifiedUri, originalFileName, docId, emitter)
                .forEach(this::embedSegment);
    }

    private void embedSegment(TextSegment segment) {
//        var embedding = this.embeddingModel.embed(segment).content();
//        this.embeddingStore.add(embedding, segment);
    }
}