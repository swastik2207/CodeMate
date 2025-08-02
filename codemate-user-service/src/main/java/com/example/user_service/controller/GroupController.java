package com.example.user_service.controller;

import com.example.user_service.model.Group;
import com.example.user_service.repository.GroupRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.user_service.dto.CreateGroupRequestDto;
import com.example.user_service.dto.AddUserToGroupRequestDto;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

     @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequestDto request) {
        // Generate a unique group code
        String groupCode = UUID.randomUUID().toString().substring(0, 8);

        // Create and save the group
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creatorId(request.getCreatorId())
                .groupCode(groupCode)
                .memberIds(List.of(request.getCreatorId()))
                .createdAt(LocalDateTime.now())
                .build();

        groupRepository.save(group);

        // Return only the groupCode
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("groupCode", groupCode));
    }





@PostMapping("/add-user")
public ResponseEntity<?> addUserToGroup(@RequestBody AddUserToGroupRequestDto request) {
    Group group = groupRepository.findByGroupCode(request.getGroupCode());

    if (group == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Group not found"));
    }

    List<String> members = group.getMemberIds();
    if (!members.contains(request.getUserId())) {
        members.add(request.getUserId());
        group.setMemberIds(members);
        groupRepository.save(group);
        return ResponseEntity.ok(Map.of("message", "User added to group"));
    } else {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "User already in group"));
    }
    
}









  
}
