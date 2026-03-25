package org.mave.rag_langchain4j.services;

import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

public interface EmbeddingInterface {

  void store(List<TextSegment> segments);
}
