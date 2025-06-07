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

        // Step 1: Create a temporary working directory
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("codeExec");
        } catch (IOException e) {
            result.put("error", "Failed to create temporary directory: " + e.getMessage());
            return result;
        }

        // Step 2: Prepare filenames and commands
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

        // Step 3: Write code to file
        Path codeFile = tempDir.resolve(fileName);
        try {
            Files.write(codeFile, code.getBytes());
        } catch (IOException e) {
            result.put("error", "Failed to write code to file: " + e.getMessage());
            return result;
        }

        // Step 4: Write Dockerfile
        String dockerfileContent = generateDockerfile(language, fileName, compileCommand, runCommand);
        Path dockerfile = tempDir.resolve("Dockerfile");
        try {
            Files.write(dockerfile, dockerfileContent.getBytes());
        } catch (IOException e) {
            result.put("error", "Failed to write Dockerfile: " + e.getMessage());
            return result;
        }

        // Step 5: Build Docker image
        String imageTag = "code-executor-" + userId;
        ProcessBuilder buildProcessBuilder = new ProcessBuilder("docker", "build", "-t", imageTag, ".");
        buildProcessBuilder.directory(tempDir.toFile());
        buildProcessBuilder.redirectErrorStream(true);
        try {
            Process buildProcess = buildProcessBuilder.start();
            streamProcessOutput(buildProcess);
            int buildExitCode = buildProcess.waitFor();
            if (buildExitCode != 0) {
                result.put("error", "Docker build failed. Check logs for details.");
                return result;
            }
        } catch (IOException | InterruptedException e) {
            result.put("error", "Docker build error: " + e.getMessage());
            return result;
        }

        // Step 6: Manage user-specific Docker container
        String containerName = "code-executor-" + "12345" ;
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
                createContainerBuilder.directory(tempDir.toFile());
                createContainerBuilder.redirectErrorStream(true);
                Process createProcess = createContainerBuilder.start();
                streamProcessOutput(createProcess);
                int exitCode = createProcess.waitFor();
                if (exitCode != 0) {
                    result.put("error", "Failed to create Docker container for user.");
                    return result;
                }
            } catch (IOException | InterruptedException e) {
                result.put("error", "Error creating Docker container: " + e.getMessage());
                return result;
            }
        }

        // Step 6.2: Copy source file into container
        try {
            ProcessBuilder copyToContainer = new ProcessBuilder(
                    "docker", "cp", codeFile.toString(), containerName + ":/app/" + fileName
            );
            copyToContainer.redirectErrorStream(true);
            Process copyProcess = copyToContainer.start();
            streamProcessOutput(copyProcess);
            int copyExit = copyProcess.waitFor();
            if (copyExit != 0) {
                result.put("error", "Failed to copy source file into container.");
                return result;
            }
        } catch (IOException | InterruptedException e) {
            result.put("error", "Error copying file to container: " + e.getMessage());
            return result;
        }

        // Step 6.3: Compile if necessary
        if (compileCommand != null) {
            try {
                ProcessBuilder compileBuilder = new ProcessBuilder(
                        "docker", "exec", containerName, "sh", "-c", compileCommand
                );
                compileBuilder.redirectErrorStream(true);
                Process compileProcess = compileBuilder.start();
                streamProcessOutput(compileProcess);
                int compileExit = compileProcess.waitFor();
                if (compileExit != 0) {
                    result.put("error", "Compilation failed.");
                    return result;
                }
            } catch (IOException | InterruptedException e) {
                result.put("error", "Error compiling code: " + e.getMessage());
                return result;
            }
        }

        // Step 6.4: Run the code with input
        try {
            ProcessBuilder runBuilder = new ProcessBuilder(
                    "docker", "exec", "-i", containerName, "sh", "-c", runCommand
            );
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                writer.write(inputData);
                writer.flush();
                writer.close();
            }

            String runOutput = readProcessOutput(runProcess.getInputStream());
            int runExitCode = runProcess.waitFor();

            if (runExitCode != 0) {
                result.put("error", "Execution failed:\n" + runOutput);
            } else {
                result.put("output", runOutput);
            }
        } catch (IOException | InterruptedException e) {
            result.put("error", "Error executing code in container: " + e.getMessage());
        }

        return result;
    }

    private boolean containerExists(String containerName) {
        try {
            ProcessBuilder checkBuilder = new ProcessBuilder(
                    "docker", "ps", "-a", "--filter", "name=" + containerName, "--format", "{{.Names}}"
            );
            Process checkProcess = checkBuilder.start();
            String output = readProcessOutput(checkProcess.getInputStream());
            checkProcess.waitFor();
            return output.trim().equals(containerName);
        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Failed to check container existence: " + e.getMessage());
            return false;
        }
    }

    private String generateDockerfile(String language, String fileName, String compileCommand, String runCommand) {
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

        dockerfile.append("CMD tail -f /dev/null\n"); // Keep container running

        return dockerfile.toString();
    }

    private void streamProcessOutput(Process process) {
        Thread stdout = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[DOCKER] " + line);
                }
            } catch (IOException e) {
                System.err.println("[DOCKER ERROR] Failed to read stdout: " + e.getMessage());
            }
        });

        Thread stderr = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("[DOCKER ERROR] " + line);
                }
            } catch (IOException e) {
                System.err.println("[DOCKER ERROR] Failed to read stderr: " + e.getMessage());
            }
        });

        stdout.start();
        stderr.start();
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
