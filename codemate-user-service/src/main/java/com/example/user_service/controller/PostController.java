package com.example.user_service.controller;

import com.example.user_service.model.Comment;
import com.example.user_service.model.Post;
import com.example.user_service.repository.PostRepository;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    // EXISTING: Create post
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestParam String userId,
                                           @RequestParam String title,
                                           @RequestParam String body) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String slug = title.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");

        Post post = Post.builder()
                .title(title)
                .body(body)
                .slug(slug)
                .status(Post.Status.DRAFT)
                .build();

        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }

    // EXISTING: Get post by ID
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return ResponseEntity.ok(post);
    }

    // EXISTING: Get post by slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Post> getPostBySlug(@PathVariable String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));
        return ResponseEntity.ok(post);
    }

    // NEW: Get all comments for a specific post
    @GetMapping("/comments")
    public ResponseEntity<List<Comment>> getComments(@RequestParam String postId) {
   // 1. Match the post by ID
    AggregationOperation match = Aggregation.match(Criteria.where("_id").is(postId));

    // 2. Lookup comments using localField / foreignField
    LookupOperation lookup = LookupOperation.newLookup()
            .from("comments")
            .localField("commentIds")
            .foreignField("_id")
            .as("comments");

    // 3. Unwind comments
    AggregationOperation unwind = Aggregation.unwind("comments");

    // 4. Sort comments by createdAt descending
    AggregationOperation sort = Aggregation.sort(
            org.springframework.data.domain.Sort.by("comments.createdAt").descending()
    );

    // 5. Project only the comment field
    AggregationOperation project = Aggregation.project()
            .and("comments").as("comments");

    // 6. Build the aggregation pipeline
    Aggregation aggregation = Aggregation.newAggregation(match, lookup, unwind, sort, project);

    // 7. Run the aggregation
    AggregationResults<Document> results =
            mongoTemplate.aggregate(aggregation, "posts", Document.class);

    // 8. Map the "comment" field from each result into a Comment object
    List<Comment> comments = results.getMappedResults().stream()
            .map(doc -> mongoTemplate.getConverter().read(Comment.class, (Document) doc.get("comment")))
            .toList();

    // 9. Return as ResponseEntity
    return ResponseEntity.ok(comments);
    }
}
