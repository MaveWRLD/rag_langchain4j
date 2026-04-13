package org.mave.rag_langchain4j.config;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;


@Component
public class LangChain4jConfig {

    @Value("${spring.datasource.dataBaseName}")
    private String postgresDb;

    @Value("${spring.datasource.username}")
    private String postgresUser;

    @Value("${spring.datasource.password}")
    private String postgresPassword;

    @Value("${spring.pgvector.table:embeddings}")
    private String tableName;

    @Value("${spring.pgvector.dimension:1536}")
    private int dimension;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5438)
                .database(postgresDb)
                .user(postgresUser)
                .password(postgresPassword)
                .table(tableName)
                .dimension(dimension)
                .useIndex(true)
                .indexListSize(100)
                .createTable(true)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel(){
        return OpenAiStreamingChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(GPT_4_O_MINI)
                .build();
    }

    @Bean
    public ChatModel chatModel(){
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(GPT_4_O_MINI)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(){
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

}

