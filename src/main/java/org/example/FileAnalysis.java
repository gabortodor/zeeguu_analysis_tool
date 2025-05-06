package org.example;

import lombok.Data;

import java.util.List;

@Data
public class FileAnalysis {
    private final String fileName;

    private final List<Endpoint> endpointList;

    private final Integer numberOfLines;
}
