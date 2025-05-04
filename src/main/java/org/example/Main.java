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
    private static final List<Future<List<Endpoint>>> futures = new ArrayList<>();

    public static void main(final String[] args) {
        crawlDirectoryAndProcessFiles(new File(args[0]));

        final List<Endpoint> results = new ArrayList<>();

        for (final Future<List<Endpoint>> future : futures) {
            try {
                results.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        for (Endpoint endpoint : results) {
            System.out.println("File: " + endpoint.getFileName() +", path: " + endpoint.getPath() + ", methods: " + endpoint.getMethods() + ", session needed: " + endpoint.getAuthenticated());
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