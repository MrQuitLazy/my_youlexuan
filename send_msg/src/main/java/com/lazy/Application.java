package com.lazy;

import com.aliyuncs.exceptions.ClientException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws ClientException {
        SpringApplication.run(Application.class,args);
    }
}
