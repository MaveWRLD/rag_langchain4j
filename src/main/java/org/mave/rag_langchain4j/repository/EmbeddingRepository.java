package org.mave.rag_langchain4j.repository;

import org.mave.rag_langchain4j.model.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {


    @Query(
            value = """
            SELECT DISTINCT
                metadata->>'filename' AS filename
            FROM embeddings
            WHERE metadata->>'filename' IS NOT NULL
            ORDER BY filename
            """,
            nativeQuery = true
    )
    List<String> findDistinctFilenames();

}