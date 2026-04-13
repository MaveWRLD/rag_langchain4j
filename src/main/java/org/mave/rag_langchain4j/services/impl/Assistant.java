package org.mave.rag_langchain4j.services.impl;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;


public interface Assistant {

    @SystemMessage("""
    You are a helpful assistant. Answer questions ONLY using the context provided to you.
    If the answer is not found in the context, say "I don't have enough information to answer that."
    Do not use any prior knowledge or make assumptions beyond what the context states.
    """)
    TokenStream chat(String message);
}
