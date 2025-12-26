package in.shaddha.moneymanager.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileDTO {
    
    
    private Long id;
    private String fullName;
    private String email;
    @Column(name = "is_active")
    private Boolean isActive = true;
    private String password;
    @Column(name = "profile_img_url")
    private String profileImgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
