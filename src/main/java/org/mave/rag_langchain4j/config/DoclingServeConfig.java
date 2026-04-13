package org.mave.rag_langchain4j.config;

import java.net.URI;
import java.time.Duration;

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
                .logRequests()
                .logResponses()
                .prettyPrint()
                .build();
    }


    @Bean
    public ConvertDocumentRequest convertDocumentRequest(){
        return ConvertDocumentRequest.builder()
                .source(HttpSource.builder().url(URI.create("https://arxiv.org/pdf/2408.09869"))
                        .build())
                .options(ConvertDocumentOptions.builder()
                        .toFormat(OutputFormat.MARKDOWN)
                        .includeImages(true)
                        .build())
                .target(InBodyTarget.builder().build())
                .build();
    }
}
