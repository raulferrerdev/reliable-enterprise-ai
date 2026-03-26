package com.phoneix.ragfoundation;

import com.phoneix.ragfoundation.retrieval.NaiveRagService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;  // ← nuevo import

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NaiveRagServiceTest {

    @MockitoBean EmbeddingModel embeddingModel;              // ← @MockBean → @MockitoBean
    @MockitoBean EmbeddingStore<TextSegment> embeddingStore; // ← @MockBean → @MockitoBean
    @MockitoBean ChatModel chatModel;                        // ← @MockBean → @MockitoBean

    @Autowired
    NaiveRagService ragService;

    @Test
    void whenNoContextFound_thenReturnsExplicitNoContextResponse() {
        NaiveRagService.RagResponse response =
                ragService.query("What is the refund policy?");

        assertThat(response.contextFound()).isFalse();
        assertThat(response.chunksUsed()).isEqualTo(0);
        assertThat(response.answer()).contains("No sufficient context");
    }
}