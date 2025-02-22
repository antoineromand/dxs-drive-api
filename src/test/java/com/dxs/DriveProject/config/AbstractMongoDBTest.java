package com.dxs.DriveProject.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public abstract class AbstractMongoDBTest {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest");

    @BeforeAll
    public static void setUp() {
        if (!mongoContainer.isRunning()) {
            mongoContainer.start();
        }
        System.setProperty("spring.data.mongodb.uri", mongoContainer.getReplicaSetUrl());
    }

    @AfterAll
    public static void tearDown() {
        mongoContainer.stop();
    }
}