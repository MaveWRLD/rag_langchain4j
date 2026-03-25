package org.mave.rag_langchain4j.mapper;

import org.mapstruct.Mapper;
import org.mave.rag_langchain4j.dto.response.RagResponse;

@Mapper(componentModel = "spring")
public interface RagResponseMapper {

    RagResponse toResponse(String response);
}
