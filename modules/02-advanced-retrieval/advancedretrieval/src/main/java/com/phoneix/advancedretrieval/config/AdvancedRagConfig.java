package com.phoneix.advancedretrieval.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
public class AdvancedRagConfig {

    @Value("${weaviate.host}")
    private String weaviateHost;

    @Value("${weaviate.scheme}")
    private String weaviateScheme;

    @Value("${weaviate.object-class}")
    private String weaviateObjectClass;

    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String ollamaBaseUrl;

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName("nomic-embed-text")
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }

    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName("llama3.2")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(120))
                .httpClientBuilder(new SpringRestClientBuilder())
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return WeaviateEmbeddingStore.builder()
                .scheme(weaviateScheme)
                .host(weaviateHost)
                .objectClass(weaviateObjectClass)
                .build();
    }
}