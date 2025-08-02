
package com.example.user_service.repository;

import com.example.user_service.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PostRepository extends MongoRepository<Post, String> {
    Optional<Post> findBySlug(String slug);
}
