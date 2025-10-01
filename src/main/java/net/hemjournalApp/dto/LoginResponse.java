package net.hemjournalApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String id;
    private String userName;
    private String email;
    private String mobile;
    private List<String> permissions;
}
