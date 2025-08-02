
package com.example.user_service.repository;

import com.example.user_service.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface CommentRepository extends MongoRepository<Comment, String> {
    // Add custom query methods if needed
}