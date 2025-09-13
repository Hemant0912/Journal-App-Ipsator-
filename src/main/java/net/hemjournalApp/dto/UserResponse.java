package net.hemjournalApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String userName;
    private List<String> permissions;
}