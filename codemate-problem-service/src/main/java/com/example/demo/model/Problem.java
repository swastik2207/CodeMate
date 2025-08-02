package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;



@Document("problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Problem {

    @Id
    private String id;

    @Indexed(unique = true)
    private String title;

    private String[] constraints;
    private String description;
    private String[] tags;
    private String[] inputFormat;
    private String[] outputFormat; 
    private String sampleInputPath;
    private String sampleOutputPath;
}
