package com.phoneix.ragfoundation.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;

import java.time.Duration;

@Configuration
public class RagConfig {

    @Value("${weaviate.host}")
    private String weaviateHost;

    @Value("${weaviate.scheme}")
    private String weaviateScheme;

    @Value("${langchain4j.ollama.chat-model.base-url}")
    private String ollamaBaseUrl;

    @Bean
    @Primary // Tells Spring to prefer this over the AutoConfig one
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName("nomic-embed-text")
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    @Primary // Tells Spring to prefer this over the AutoConfig one
    public OllamaChatModel chatLanguageModel() { // Using concrete type or ChatLanguageModel interface
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName("llama3.2")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return WeaviateEmbeddingStore.builder()
                .scheme(weaviateScheme)
                .host(weaviateHost)
                .objectClass("EnterpriseDocument")
                .build();
    }
}