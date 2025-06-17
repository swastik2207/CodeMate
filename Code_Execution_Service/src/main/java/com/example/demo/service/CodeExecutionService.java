package com.example.demo.service;

import com.example.demo.dto.CodeSubmissionRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class CodeExecutionService {

    public Map<String, String> executeCode(CodeSubmissionRequest request) {
        Map<String, String> result = new HashMap<>();
        String language = request.getLanguage().toLowerCase();
        String code = request.getCode();
        List<String> inputs = request.getInputs();
        String userId = request.getUserId();

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("codeExec");
        } catch (IOException e) {
            result.put("error", "Failed to create temp dir: " + e.getMessage());
            return result;
        }

        String fileName;
        String compileCommand = null;
        String runCommand;

        switch (language) {
            case "java":
                fileName = "Main.java";
                compileCommand = "javac Main.java";
                runCommand = "java Main";
                break;
            case "python":
                fileName = "script.py";
                runCommand = "python3 script.py";
                break;
            case "c":
                fileName = "program.c";
                compileCommand = "gcc program.c -o program";
                runCommand = "./program";
                break;
            default:
                result.put("error", "Unsupported language: " + language);
                return result;
        }

        Path codeFile = tempDir.resolve(fileName);
        try {
            Files.write(codeFile, code.getBytes());
        } catch (IOException e) {
            result.put("error", "Failed to write code file: " + e.getMessage());
            return result;
        }

        Path dockerfile = tempDir.resolve("Dockerfile");
        String dockerfileContent = generateDockerfile(language, fileName, compileCommand);
        try {
            Files.write(dockerfile, dockerfileContent.getBytes());
        } catch (IOException e) {
            result.put("error", "Failed to write Dockerfile: " + e.getMessage());
            return result;
        }

        String imageTag = "code-executor-" + userId;
        ProcessBuilder buildProcessBuilder = new ProcessBuilder("docker", "build", "-t", imageTag, ".");
        buildProcessBuilder.directory(tempDir.toFile());
        buildProcessBuilder.redirectErrorStream(true);

        try {
            Process buildProcess = buildProcessBuilder.start();
            streamProcessOutput(buildProcess);
            int exit = buildProcess.waitFor();
            if (exit != 0) {
                result.put("error", "Docker build failed.");
                return result;
            }
        } catch (IOException | InterruptedException e) {
            result.put("error", "Docker build error: " + e.getMessage());
            return result;
        }

        String containerName = "code-executor-" + userId;
        String inputData = String.join("\n", inputs != null ? inputs : Collections.emptyList());

        if (!containerExists(containerName)) {
            try {
                ProcessBuilder createContainerBuilder = new ProcessBuilder(
                        "docker", "run", "-dit",
                        "--memory=512m",
                        "--cpus=1",
                        "--name", containerName,
                        imageTag, "sh"
                );
                createContainerBuilder.redirectErrorStream(true);
                createContainerBuilder.directory(tempDir.toFile());
                Process createProcess = createContainerBuilder.start();
                streamProcessOutput(createProcess);
                if (createProcess.waitFor() != 0) {
                    result.put("error", "Failed to create Docker container.");
                    return result;
                }
            } catch (IOException | InterruptedException e) {
                result.put("error", "Error creating Docker container: " + e.getMessage());
                return result;
            }
        }

        try {
            ProcessBuilder copyBuilder = new ProcessBuilder("docker", "cp", codeFile.toString(), containerName + ":/app/" + fileName);
            copyBuilder.redirectErrorStream(true);
            Process copyProcess = copyBuilder.start();
            streamProcessOutput(copyProcess);
            if (copyProcess.waitFor() != 0) {
                result.put("error", "Failed to copy file to container.");
                return result;
            }
        } catch (IOException | InterruptedException e) {
            result.put("error", "Error copying to container: " + e.getMessage());
            return result;
        }

        if (compileCommand != null) {
            try {
                ProcessBuilder compileBuilder = new ProcessBuilder(
                        "docker", "exec", containerName, "sh", "-c", compileCommand
                );
                compileBuilder.redirectErrorStream(true);
                Process compileProcess = compileBuilder.start();
                streamProcessOutput(compileProcess);
                if (compileProcess.waitFor() != 0) {
                    result.put("error", "Compilation failed.");
                    return result;
                }
            } catch (IOException | InterruptedException e) {
                result.put("error", "Error compiling code: " + e.getMessage());
                return result;
            }
        }

        try {
            ProcessBuilder runBuilder = new ProcessBuilder("docker", "exec", "-i", containerName, "sh", "-c", runCommand);
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                writer.write(inputData);
                writer.flush();
            }

            String output = readProcessOutput(runProcess.getInputStream());
            int exit = runProcess.waitFor();

            if (exit != 0) {
                result.put("error", "Execution failed.\n" + output);
            } else {
                System.out.println("✅ Code output:\n" + output); // log the output
                result.put("output", output);
            }
        } catch (IOException | InterruptedException e) {
            result.put("error", "Execution error: " + e.getMessage());
        }

        System.out.println("📦 Final Result Map: " + result); // full map log
        return result;
    }

    private String generateDockerfile(String language, String fileName, String compileCommand) {
        StringBuilder dockerfile = new StringBuilder();
        dockerfile.append("FROM ");
        switch (language) {
            case "java":
                dockerfile.append("openjdk:17-jdk-alpine\n");
                break;
            case "python":
                dockerfile.append("python:3.9-alpine\n");
                break;
            case "c":
                dockerfile.append("gcc:latest\n");
                break;
        }
        dockerfile.append("WORKDIR /app\n");
        dockerfile.append("COPY ").append(fileName).append(" .\n");
        if (compileCommand != null) {
            dockerfile.append("RUN ").append(compileCommand).append("\n");
        }
        dockerfile.append("CMD tail -f /dev/null\n"); // keep container alive
        return dockerfile.toString();
    }

    private boolean containerExists(String name) {
        try {
            ProcessBuilder builder = new ProcessBuilder("docker", "ps", "-a", "--filter", "name=" + name, "--format", "{{.Names}}");
            Process process = builder.start();
            String output = readProcessOutput(process.getInputStream());
            process.waitFor();
            return output.trim().equals(name);
        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Container check failed: " + e.getMessage());
            return false;
        }
    }

    private void streamProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[DOCKER] " + line);
                }
            } catch (IOException e) {
                System.err.println("[DOCKER ERROR] Stdout error: " + e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("[DOCKER ERROR] " + line);
                }
            } catch (IOException e) {
                System.err.println("[DOCKER ERROR] Stderr error: " + e.getMessage());
            }
        }).start();
    }

    private String readProcessOutput(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }
}
