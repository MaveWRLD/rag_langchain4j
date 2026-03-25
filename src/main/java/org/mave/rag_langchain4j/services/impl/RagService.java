package org.mave.rag_langchain4j.services.impl;


import lombok.RequiredArgsConstructor;
import org.mave.rag_langchain4j.dto.request.RagRequest;
import org.mave.rag_langchain4j.dto.response.RagResponse;
import org.mave.rag_langchain4j.mapper.RagResponseMapper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RagService {

    private final DocumentIngestionService ingestionService;
    private final RagResponseMapper responseMapper;
    private final Assistant assistant;


    public void ingest(String filePath) {
        ingestionService.ingest(filePath);
    }

    public RagResponse askQuestion(RagRequest request) {
        return responseMapper.toResponse(
                assistant.chat(request.getMessage()));
    }
}
