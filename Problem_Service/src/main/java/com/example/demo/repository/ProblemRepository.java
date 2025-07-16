package com.example.demo.repository;

import com.example.demo.model.Problem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProblemRepository extends MongoRepository<Problem, String> {
    Problem findByTitle(String title);
    
    

}
