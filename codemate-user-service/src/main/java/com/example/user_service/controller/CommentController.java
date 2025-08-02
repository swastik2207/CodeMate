

package com.example.user_service.controller;
import com.example.user_service.model.Comment;
import com.example.user_service.model.Post;
import com.example.user_service.model.User;
import com.example.user_service.repository.CommentRepository;
import com.example.user_service.repository.PostRepository;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate; // for direct updates

    @PostMapping("/create")
    public ResponseEntity<?> createComment(@RequestParam String postId,
                                           @RequestParam String userId,
                                           @RequestParam String text) {
        // Validate user
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate post exists
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Create and save comment
        Comment comment = Comment.builder()
                .author(author)
                .post(Post.builder().id(postId).build()) // lightweight reference
                .text(text)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Update only commentIds in the Post
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update().push("commentIds", savedComment.getId());
        mongoTemplate.updateFirst(query, update, Post.class);

        return ResponseEntity.ok(
                Map.of("message", "Comment created successfully")
        );
    }
}
