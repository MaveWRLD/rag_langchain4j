package org.mave.rag_langchain4j.dto.response;

public record StreamEvent(
        String type,
        Object data
) {}
