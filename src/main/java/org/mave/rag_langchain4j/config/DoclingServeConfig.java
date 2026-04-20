package org.mave.rag_langchain4j.config;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.docling.serve.api.DoclingServeApi;
import ai.docling.serve.api.convert.request.ConvertDocumentRequest;
import ai.docling.serve.api.convert.request.options.ConvertDocumentOptions;
import ai.docling.serve.api.convert.request.options.OutputFormat;
import ai.docling.serve.api.convert.request.source.HttpSource;
import ai.docling.serve.api.convert.request.target.InBodyTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DoclingServeConfig {

    @Value("${docling.based-url}")
    private String baseUrl;

    @Bean
    public DoclingServeApi doclingServeApi(){
        return DoclingServeApi.builder()
                .baseUrl(baseUrl)
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofMinutes(5))
                .build();
    }

    @Bean
    public ExecutorService embeddingExecutor(
            @Value("${embedding.thread-pool-size:8}") int poolSize) {
        return Executors.newFixedThreadPool(poolSize);
    }
}
