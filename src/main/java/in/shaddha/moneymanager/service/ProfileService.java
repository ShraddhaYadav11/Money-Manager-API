package in.shaddha.moneymanager.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.shaddha.moneymanager.dto.AuthDTO;
import in.shaddha.moneymanager.dto.ProfileDTO;
import in.shaddha.moneymanager.entity.ProfileEntity;
import in.shaddha.moneymanager.repository.ProfileRepository;
import in.shaddha.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor

public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

     @Value("${app.activation.url}")
        private String activationURL;


    public ProfileDTO registerProfile(ProfileDTO profileDTO){
       
        

        ProfileEntity newprofile=toEntity(profileDTO);
        newprofile.setActivationToken(UUID.randomUUID().toString());
        newprofile=profileRepository.save(newprofile);
        //Send Activation Email
        String activationLink=activationURL+"/api/v1.0/activate?token=" +newprofile.getActivationToken();
        String subject="Acivate your money Manager";
        String body="Click on the following link to activate your account: "+activationLink;
        emailService.sendEmail(newprofile.getEmail(), subject, body);
       return toDTO(newprofile);

       
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
         .id(profileDTO.getId())
         .fullName(profileDTO.getFullName())
         .email(profileDTO.getEmail())
         .password(passwordEncoder.encode(profileDTO.getPassword()))
         .profileImgUrl(profileDTO.getProfileImgUrl())
         .createdAt(profileDTO.getCreatedAt())
         .updatedAt(profileDTO.getUpdatedAt())
         .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
         .id(profileEntity.getId())
         .fullName(profileEntity.getFullName())
         .email(profileEntity.getEmail())
         .profileImgUrl(profileEntity.getProfileImgUrl())
         .createdAt(profileEntity.getCreatedAt())
         .updatedAt(profileEntity.getUpdatedAt())
         .build();
    }

    public boolean activateProfile(String activationToken){
        return profileRepository.findByActivationToken(activationToken)
          .map(profile->{
            profile.setIsActive(true);
            profileRepository.save(profile);
            return true;
          })
          .orElse(false);
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email)
        .map(ProfileEntity::getIsActive)
        .orElse(false);

    }

    public ProfileEntity getCurrentProfile(){
         Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        
        return profileRepository.findByEmail(authentication.getName())
              .orElseThrow(()->new UsernameNotFoundException("Profiles not found with email : " +authentication.getName()));

    }

    public ProfileDTO getPublicProfile(String email){

        ProfileEntity currentUser=null;
        if(email==null){
            getCurrentProfile();

        }else{
           currentUser= profileRepository.findByEmail(email)
              .orElseThrow(()->new UsernameNotFoundException("Profile not found with email :"+ email));
        }

        return ProfileDTO.builder()
        .id(currentUser.getId())
         .fullName(currentUser.getFullName())
         .email(currentUser.getEmail())
         .profileImgUrl(currentUser.getProfileImgUrl())
         .createdAt(currentUser.getCreatedAt())
         .updatedAt(currentUser.getUpdatedAt())
         .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        
        try {
            
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            //Generate JWT token
            String token =jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                "token",token,
                "user", getPublicProfile(authDTO.getEmail())
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid email and Password");
        }
    }



}
