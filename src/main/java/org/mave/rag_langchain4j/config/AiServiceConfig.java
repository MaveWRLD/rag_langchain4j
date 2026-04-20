package org.mave.rag_langchain4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.mave.rag_langchain4j.services.Assistant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
public class AiServiceConfig {


    @Bean
    public ChatMemoryProvider chatMemoryProvider() {

        Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

        return memoryId ->
                memories.computeIfAbsent(
                        (String) memoryId,
                        id -> MessageWindowChatMemory.withMaxMessages(20)
                );
    }

    @Bean
    public Assistant assistant(StreamingChatModel streamingChatModel, EmbeddingStore<TextSegment> vectorStore, EmbeddingModel embeddingModel) {

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(vectorStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.75)
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .executor(executor)
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(chatMemory)
                .build();
    }
}
