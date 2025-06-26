package com.example.demo.controller;

import com.example.demo.model.Problem;
import com.example.demo.repository.ProblemRepository;
import com.example.demo.feign.OnlineJudgeServiceClient;
import com.example.demo.dto.CodeSubmissionRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


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
        String absoluteUploadDir = new File(uploadDir).getAbsolutePath();

        File dir = new File(absoluteUploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Created upload directory: " + created + " at " + dir.getAbsolutePath());
            if (!created) {
                throw new RuntimeException("Failed to create upload directory");
            }
        }

        String inputFileName = System.currentTimeMillis() + "_input_file.txt";
        String outputFileName = System.currentTimeMillis() + "_output_file.txt";

        File inputFile = Paths.get(absoluteUploadDir, inputFileName).toFile();
        File outputFile = Paths.get(absoluteUploadDir, outputFileName).toFile();

        sampleInput.transferTo(inputFile);
        sampleOutput.transferTo(outputFile);

      Problem problem = new Problem();
problem.setTitle(title);
problem.setConstraints(constraints);
problem.setDescription(description);
problem.setSampleInputPath(inputFile.getAbsolutePath());
problem.setSampleOutputPath(outputFile.getAbsolutePath());



        problemRepository.save(problem);

        return "Problem Uploaded Successfully";
    }
@PostMapping("/solution/verify")
public String verifySolution(
        @RequestParam String userId,
        @RequestParam String problemId,
        @RequestParam String code,
        @RequestParam String language
) throws Exception{                    

    // Resolve file paths
    String absoluteUploadDir = new File(uploadDir).getAbsolutePath();
    File inputFile = Paths.get(absoluteUploadDir, problemId + "_input_file.txt").toFile();
    File outputFile = Paths.get(absoluteUploadDir, problemId + "_output_file.txt").toFile();

    if (!inputFile.exists() || !outputFile.exists()) {
        return "Input or Output file not found for problemId: " + problemId;
    }

    
List<List<String>> testInputs = Files.readAllLines(inputFile.toPath())
                                        .stream()
                                        .map(line -> List.of(line))
                                        .toList();


    List<String> expectedOutput = Files.readAllLines(outputFile.toPath());


    CodeSubmissionRequest request = new CodeSubmissionRequest();
    request.setCode(code);
    request.setLanguage(language);
    request.setInputs(testInputs);
    request.setUserId(userId);




    List<String>result = onlineJudgeServiceClient.executeForProblem(request);

    
    if(result.size()>0){
        return "ALL TESTCASES PASSED";
    }

    return "INCORRECT ANSWER";
}
}
