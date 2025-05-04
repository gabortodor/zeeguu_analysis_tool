package org.example;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor implements Callable<List<Endpoint>> {

    private final File file;
    private final Pattern variablePattern = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*\"([^\"]*)\"\\s*$");
    private final Pattern endpointPattern = Pattern.compile("@api\\.route\\((?:[frbu]*\")([^\"]+)\",?(?:\\s*methods=[\\(\\[]([^\\)\\]]+)[\\)\\]])?,?\\)");


    public Processor(final File file) {
        this.file = file;
    }

    @Override
    public List<Endpoint> call() {
        final List<Endpoint> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            final Map<String, String> context = new HashMap<>();
            String line;
            String apiBlock = "";
            boolean isAuthenticated = false;
            boolean inProgress = false;

            while ((line = br.readLine()) != null) {



                final Matcher variableMatcher = variablePattern.matcher(line);
                if (variableMatcher.matches()) {
                    context.put(variableMatcher.group(1), variableMatcher.group(2));
                }

                if (line.startsWith("@requires_session")) {
                    isAuthenticated = true;
                }
                if (line.startsWith("@api.route") || inProgress) {
                    inProgress = true;
                    apiBlock += line.trim();
                    if (StringUtils.countMatches(apiBlock, "(") == StringUtils.countMatches(apiBlock, ")")) {
                        inProgress = false;
                    }
                }
                if (line.startsWith("def ") && !apiBlock.isEmpty()) {
                    result.add(this.parse(file.getName(), apiBlock, context, isAuthenticated));
                    isAuthenticated = false;
                    inProgress = false;
                    apiBlock = "";
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Endpoint parse(final String fileName, final String line, final Map<String, String> context, final boolean isAuthenticated) {
        final Matcher matcher = endpointPattern.matcher(line);
        if (matcher.find()) {
            String path = matcher.group(1);
            while (path.contains("{") && path.contains("}")) {
                String variableName = path.substring(path.indexOf("{"), path.indexOf("}") + 1);
                path = path.replace(variableName, context.get(variableName.substring(1, variableName.length() - 1)));
            }

            final List<Method> methods = new ArrayList<>();
            final String methodString = matcher.group(2);
            if (methodString != null) {
                final String[] methodStrings = matcher.group(2).split(",");
                for (String methodStr : methodStrings) {
                    String clean = methodStr.replaceAll("[\"'\\s]", "");
                    if (!clean.isEmpty()) {
                        methods.add(Method.valueOf(clean));
                    }
                }
            } else {
                methods.add(Method.GET);
            }
            return new Endpoint(fileName, path, methods, isAuthenticated);
        }
        System.out.println("Invalid line: " + line +", file: " + fileName);
        throw new IllegalArgumentException("Input string is not a valid route annotation");
    }
}
