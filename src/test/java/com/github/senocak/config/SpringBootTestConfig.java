package com.github.senocak.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Tag("integration")
@Target(ElementType.TYPE)
@ExtendWith(SpringExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles(value = { "integration-test" })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public @interface SpringBootTestConfig {}