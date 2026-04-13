package org.mave.rag_langchain4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@EnableAspectJAutoProxy
@SpringBootApplication
public class RagLangchain4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagLangchain4jApplication.class, args);
    }

}
