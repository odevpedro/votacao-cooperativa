package com.example.cooperativevoting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CooperativeVotingApplication {

  public static void main(String[] args) {
    SpringApplication.run(CooperativeVotingApplication.class, args);
  }
}
