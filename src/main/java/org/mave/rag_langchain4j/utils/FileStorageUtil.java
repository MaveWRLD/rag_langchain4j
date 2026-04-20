package org.mave.rag_langchain4j.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private Path tempFileDirectory;

    public FileStorageUtil(@Value("${app.upload.temp-dir:/tmp/rag-uploads}") String tempDir) {
        tempFileDirectory = Paths.get(tempDir);
        initializeDirectory();
    }

    private void initializeDirectory() {
        try {
            Files.createDirectories(tempFileDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp directory: " + tempFileDirectory, e);
        }
    }

    public Path saveToTempFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path filePath = tempFileDirectory.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}
