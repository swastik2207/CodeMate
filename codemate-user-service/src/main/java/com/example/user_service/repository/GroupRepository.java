package com.example.user_service.repository;

import com.example.user_service.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group, String> {
   Group findByGroupCode(String groupCode);
}
