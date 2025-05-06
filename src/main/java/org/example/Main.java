package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static final ExecutorService executor = Executors.newFixedThreadPool(32);
    private static final List<Future<FileAnalysis>> futures = new ArrayList<>();

    public static void main(final String[] args) {
        crawlDirectoryAndProcessFiles(new File(args[0]));

        final List<FileAnalysis> results = new ArrayList<>();

        for (final Future<FileAnalysis> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        for (FileAnalysis fileAnalysis : results) {
            System.out.println("File: " + fileAnalysis.getFileName() +", number of endpoints: " + fileAnalysis.getEndpointList().size() + ", number of lines: " + fileAnalysis.getNumberOfLines());
        }
    }

    private static void crawlDirectoryAndProcessFiles(final File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                crawlDirectoryAndProcessFiles(file);
            } else {
                futures.add(executor.submit(new Processor(file)));
            }
        }
    }
}