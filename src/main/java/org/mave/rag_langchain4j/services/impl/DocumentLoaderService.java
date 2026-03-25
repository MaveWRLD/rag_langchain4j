package org.mave.rag_langchain4j.services.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentLoaderService {

    public Document loadDocument(String path) {

        Path filePath = Path.of(path);

        if (!Files.exists(filePath)) {
            log.error("File not found at path: {}", path);
            throw new RuntimeException("File not found");
        }

        log.info("Loading document from path: {}", path);

        Document document = FileSystemDocumentLoader
                .loadDocument(filePath, new ApacheTikaDocumentParser());

        log.info("Document successfully loaded: {}", filePath.getFileName());

        return document;
    }
}
