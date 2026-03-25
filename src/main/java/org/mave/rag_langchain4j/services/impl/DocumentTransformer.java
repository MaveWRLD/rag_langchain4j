package org.mave.rag_langchain4j.services.impl;

import dev.langchain4j.data.document.Document;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentTransformer {

    public Document transform(Document document) {

        if (document == null) {
            log.error("Attempted to transform a null document");
            throw new IllegalArgumentException("Document cannot be null");
        }

        String originalText = document.text();

        if (originalText == null || originalText.isBlank()) {
            log.warn("Document text is empty before transformation");
        }

        log.info("Starting document transformation...");

        String cleaned = originalText
                .replaceAll("(?m)^Page \\d+", "")
                .replaceAll("(?i)confidential", "")
                .trim();

        log.info("Document transformation completed");

        log.debug("Original length: {}", originalText.length());
        log.debug("Cleaned length: {}", cleaned.length());

        if (log.isDebugEnabled()) {
            log.debug("Original preview: {}",
                    originalText.substring(0, Math.min(100, originalText.length())));

            log.debug("Cleaned preview: {}",
                    cleaned.substring(0, Math.min(100, cleaned.length())));
        }

        return Document.from(cleaned, document.metadata());
    }
}
