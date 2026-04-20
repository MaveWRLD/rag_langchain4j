package org.mave.rag_langchain4j.services;

import lombok.RequiredArgsConstructor;
import org.mave.rag_langchain4j.repository.EmbeddingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {
    private final EmbeddingRepository embeddingRepository;

    public List<String> getAllFilenames() {

        return embeddingRepository.findDistinctFilenames();
    }

}
