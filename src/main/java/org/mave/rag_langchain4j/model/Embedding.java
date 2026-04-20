package org.mave.rag_langchain4j.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "embeddings")
public class Embedding {
    @Id
    @Column(name = "embedding_id", nullable = false)
    private UUID id;

    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

}