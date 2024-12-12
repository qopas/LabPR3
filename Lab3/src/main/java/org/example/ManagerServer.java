package org.example;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/update-leader")
public class ManagerServer {

    private String currentLeader;
    @PostMapping
    public String updateLeader(@RequestParam("leader") String leaderId) {
        currentLeader = leaderId;
        // Logic to update redirect or routing to the new leader
        System.out.println("Leader updated to: " + currentLeader);
        return "Leader updated to: " + currentLeader;
    }
    @GetMapping("/current-leader")
    public String getCurrentLeader() {
        return "Current Leader: " + currentLeader;
    }
}
