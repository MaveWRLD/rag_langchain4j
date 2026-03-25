package org.mave.rag_langchain4j.controllers;


import lombok.RequiredArgsConstructor;
import org.mave.rag_langchain4j.dto.request.RagRequest;
import org.mave.rag_langchain4j.dto.response.RagResponse;
import org.mave.rag_langchain4j.services.impl.RagService;
import org.mave.rag_langchain4j.utils.FileStorageUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000")
public class RagController {

    private final RagService ragService;
    private final FileStorageUtil fileStorageUtil;

    @PostMapping("/ingest")
    public void ingest(
            @RequestParam MultipartFile file
    ){
        String filePath;
        try {
            filePath = fileStorageUtil.saveToTempFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        ragService.ingest(filePath);
    }

    @PostMapping("/chat")
    public ResponseEntity<RagResponse> askQuestion(@RequestBody RagRequest request){
        var response = ragService.askQuestion(request);

        return ResponseEntity.ok(response);
    }
}
