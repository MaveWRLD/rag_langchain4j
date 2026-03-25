package org.mave.rag_langchain4j.services.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentSplitterService {

    public List<TextSegment> split(Document document) {

        if (document == null) {
            log.error("Attempted to split a null document");
            throw new IllegalArgumentException("Document cannot be null");
        }

        log.info("Starting document split...");

        List<TextSegment> segments = DocumentSplitters.
                recursive(2000, 300).
                split(document);

        log.info("Document split completed. Total segments created: {}", segments.size());

        if (log.isDebugEnabled()) {
            for (int i = 0; i < Math.min(segments.size(), 3); i++) {
                log.debug("Segment {} preview: {}", i + 1,
                        segments.get(i).text().substring(0,
                                Math.min(100, segments.get(i).text().length())));
            }
        }

        return segments;
    }
}