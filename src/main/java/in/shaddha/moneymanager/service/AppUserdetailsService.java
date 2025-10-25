package in.shaddha.moneymanager.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import in.shaddha.moneymanager.entity.ProfileEntity;
import in.shaddha.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AppUserdetailsService implements UserDetailsService {
   
    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {


       ProfileEntity existingProfile= profileRepository.findByEmail(email)
         .orElseThrow(()->new UsernameNotFoundException("Profile not found with email"+email));
        return User.builder()
            .username(existingProfile.getEmail())
            .password(existingProfile.getPassword())
            .authorities(Collections.emptyList())
            .build();

    }
    

}
