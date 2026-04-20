package org.mave.rag_langchain4j.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@AllArgsConstructor
public class AvailableFiles {
    private List<String> fileNames;
    private int fileCount;
}
