package org.mave.rag_langchain4j.mapper;


import org.mapstruct.Mapper;
import org.mave.rag_langchain4j.dto.response.AvailableFiles;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AvailableFilesMapper {

    default AvailableFiles toDto(List<String> fileNames) {
        return new AvailableFiles(fileNames, fileNames.size());
    }
}
