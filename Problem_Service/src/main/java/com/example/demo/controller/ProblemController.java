package com.example.demo.controller;

import com.example.demo.model.Problem;
import com.example.demo.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@RestController
@RequestMapping("/problems")
public class ProblemController {

    private final ProblemRepository problemRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ProblemController(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
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

        String inputFileName = System.currentTimeMillis() + "_" + sampleInput.getOriginalFilename();
        String outputFileName = System.currentTimeMillis() + "_" + sampleOutput.getOriginalFilename();

        File inputFile = Paths.get(absoluteUploadDir, inputFileName).toFile();
        File outputFile = Paths.get(absoluteUploadDir, outputFileName).toFile();

        sampleInput.transferTo(inputFile);
        sampleOutput.transferTo(outputFile);

        Problem problem = new Problem(null, title, constraints, description,
                inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
        problemRepository.save(problem);

        return "Problem Uploaded Successfully";
    }
}
