package org.example;

import lombok.Data;

import java.util.List;

@Data
public class Endpoint {
    private final String fileName;

    private final String path;

    private final List<Method> methods;

    private final Boolean authenticated;
}
