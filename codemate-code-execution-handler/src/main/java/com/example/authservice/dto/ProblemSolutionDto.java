package com.example.authservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemSolutionDto {


    private String problemId;
    private String code;
    private String language;
    private String inputFile;
    private String outputFile;
}
