package com.example.demo.controller;

import com.example.demo.model.Problem;
import com.example.demo.repository.ProblemRepository;
import com.example.demo.feign.OnlineJudgeServiceClient;
import com.example.demo.dto.CodeSubmissionRequest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Arrays;


@RestController
@RequestMapping("/problems")
public class ProblemController {
       
    private final OnlineJudgeServiceClient onlineJudgeServiceClient;

    private final ProblemRepository problemRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ProblemController(ProblemRepository problemRepository,
                             OnlineJudgeServiceClient onlineJudgeServiceClient) {
        this.problemRepository = problemRepository;
        this.onlineJudgeServiceClient=onlineJudgeServiceClient;
    
    }

    @PostMapping("/upload")
    public String uploadProblem(
            @RequestParam("constraints") String[] constraints,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("sampleInput") MultipartFile sampleInput,
            @RequestParam("sampleOutput") MultipartFile sampleOutput
    ) throws IOException {

        // Convert to absolute path

        if (uploadDir == null || uploadDir.isEmpty()) {
            throw new RuntimeException("Upload directory is not configured");
        }
        if (!new File(uploadDir).isAbsolute()) {
            throw new RuntimeException("Upload directory must be an absolute path");
        }

        String absoluteUploadDir = new File(uploadDir).getAbsolutePath();

        File dir = new File(absoluteUploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Created upload directory: " + created + " at " + dir.getAbsolutePath());
            if (!created) {
                throw new RuntimeException("Failed to create upload directory");
            }
        }
   String problemId = UUID.randomUUID().toString();
        String inputFileName = problemId + "_input_file.txt";
        String outputFileName = problemId + "_output_file.txt";

        File inputFile = Paths.get(absoluteUploadDir, inputFileName).toFile();
        File outputFile = Paths.get(absoluteUploadDir, outputFileName).toFile();

        sampleInput.transferTo(inputFile);
        sampleOutput.transferTo(outputFile);

 Problem problem = new Problem();
problem.setId(problemId);
problem.setTitle(title);
problem.setConstraints(constraints);
problem.setDescription(description);
problem.setSampleInputPath(inputFile.getAbsolutePath());
problem.setSampleOutputPath(outputFile.getAbsolutePath());



        problemRepository.save(problem);

        return "Problem Uploaded Successfully";
    }


@PostMapping("/solution/verify")
public ResponseEntity<String> verifySolution(
  @RequestBody CodeSubmissionRequest request
) {
    try {
        // Resolve file paths
        String problemId = request.getProblemId();
        String absoluteUploadDir = new File(uploadDir).getAbsolutePath();
        File inputFile = Paths.get(absoluteUploadDir, problemId + "_input_file.txt").toFile();
        File outputFile = Paths.get(absoluteUploadDir, problemId + "_output_file.txt").toFile();

        if (!inputFile.exists() || !outputFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Input or output file not found for problemId: " + problemId);
        }

        // Read input and output files
        List<List<String>> testInputs;
        List<String> expectedOutput;
      

        try {
    testInputs = Files.readAllLines(inputFile.toPath())  // Step 1
    .stream()
    .map(String::trim)                                                  // Step 2: Trim whitespace
    .map(line -> Arrays.asList(line.split(" ")))                        // Step 3: Split by space
    .collect(Collectors.toList());      
                  

            expectedOutput = Files.readAllLines(outputFile.toPath()).stream()
    .map(String::trim)
    .collect(Collectors.toList());
              
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read input/output files: " + e.getMessage());
        }

        request.setInputs(testInputs);
        // Execute and compare results
         ResponseEntity<List<String>> result = onlineJudgeServiceClient.executeForProblem(request);

        if (result == null || result.getBody() == null || result.getBody().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Received no output from execution service.");
        }



         

        if (result.getBody().equals(expectedOutput)) {

            

            return ResponseEntity.ok("ALL TEST CASES PASSED");
        } else {
            return ResponseEntity.ok("INCORRECT ANSWER");
        }

    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }
}

    @GetMapping
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @GetMapping("/{id}")
   public ResponseEntity<Problem> getProblemById(@PathVariable String id) {
    return problemRepository.findById(id)
            .map(ResponseEntity::ok)                        // if present, return 200 with body
            .orElse(ResponseEntity.notFound().build());     // if not, return 404
}

}
