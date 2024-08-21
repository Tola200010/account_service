package account.controllers;

import account.config.GeneralApplicationEvent;
import account.dto.GeneralApplicationEventData;
import account.dto.request.ChangePasswordRequestDto;
import account.dto.request.LockUserRequestDto;
import account.dto.request.SignUpRequestDTO;
import account.dto.request.UpdateUserRoleRequestDto;
import account.dto.response.ChangedPasswordResponseDto;
import account.dto.response.DeletedUserResponseDto;
import account.dto.response.SignUpResponseDTO;
import account.dto.response.StatusResponseDto;
import account.entities.AppUser;
import account.entities.Role;
import account.exception.*;
import account.repository.AppUserRepository;
import account.repository.RoleRepository;
import account.utiles.ApplicationEventName;
import account.utiles.LockUserOperation;
import account.utiles.UpdateRoleOperationType;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountController(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<SignUpResponseDTO> signUp(@RequestBody @Valid SignUpRequestDTO requestDTO) {
        if (!requestDTO.getEmail().endsWith("@acme.com")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        passwordValidation(requestDTO.getPassword());
        branchedPasswordValidation(requestDTO.getPassword());
        long count = appUserRepository.count();
        Optional<AppUser> byEmail = appUserRepository.findByEmail(requestDTO.getEmail().toLowerCase());
        if (byEmail.isPresent()) {
            throw new UserExistException();
        }
        AppUser appUser = new AppUser();
        Role role;
        if (count <= 0) {
            role = roleRepository.findByName("ROLE_ADMINISTRATOR").orElse(null);
        } else {
            role = roleRepository.findByName("ROLE_USER").orElse(null);
        }
        assert role != null;
        appUser.addRole(role);
        appUser.setName(requestDTO.getName());
        appUser.setEmail(requestDTO.getEmail().toLowerCase());
        appUser.setLastname(requestDTO.getLastName());
        appUser.setAccountNonLocked(true);
        appUser.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        AppUser createdUser = appUserRepository.save(appUser);
        GeneralApplicationEvent generalApplicationEvent = getGeneralApplicationEvent(GeneralApplicationEventData.sendAnonymousData(ApplicationEventName.CREATE_USER, requestDTO.getEmail().toLowerCase()));
        applicationEventPublisher.publishEvent(generalApplicationEvent);
        List<String> roles = createdUser.getRoles().stream().map(Role::getName).toList();
        return new ResponseEntity<>(new SignUpResponseDTO(createdUser.getId(), requestDTO.getName(), requestDTO.getLastName(), requestDTO.getEmail(), roles), HttpStatus.OK);
    }

    private static void passwordValidation(String password) {
        if (password.length() < 12) {
            throw new PasswordLengthValidationException();
        }
    }

    @PostMapping("/auth/changepass")
    public ResponseEntity<ChangedPasswordResponseDto> changedPassword(@RequestBody ChangePasswordRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        passwordValidation(requestDto.getNewPassword());
        branchedPasswordValidation(requestDto.getNewPassword());
        AppUser appUser = appUserRepository.findByEmail(userDetails.getUsername().toLowerCase()).orElse(null);
        assert appUser != null;
        if (passwordEncoder.matches(requestDto.getNewPassword(), appUser.getPassword())) {
            throw new MatchedPasswordException();
        }
        appUser.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        appUserRepository.save(appUser);
        GeneralApplicationEvent generalApplicationEvent = getGeneralApplicationEvent(new GeneralApplicationEventData(ApplicationEventName.CHANGE_PASSWORD, userDetails.getUsername().toLowerCase(), userDetails.getUsername()));
        applicationEventPublisher.publishEvent(generalApplicationEvent);
        return new ResponseEntity<>(new ChangedPasswordResponseDto(appUser.getEmail().toLowerCase()), HttpStatus.OK);
    }

    @GetMapping("/admin/user/")
    public ResponseEntity<List<SignUpResponseDTO>> getAllUser() {
        List<AppUser> allUsers = appUserRepository.findAll(Sort.by("id"));
        List<SignUpResponseDTO> userResponseList = allUsers.stream().map(
                x -> {
                    List<String> roles = x.getRoles().stream().map(Role::getName).toList();
                    return new SignUpResponseDTO(x.getId(), x.getName(), x.getLastname(), x.getEmail(), roles);
                }
        ).toList();
        allUsers.forEach(x-> System.out.println(x.getFailedAttempt()));
        return new ResponseEntity<>(userResponseList, HttpStatus.OK);
    }

    @DeleteMapping("/admin/user/{email}")
    public ResponseEntity<DeletedUserResponseDto> deleteUser(@PathVariable String email,@AuthenticationPrincipal UserDetails userDetails) {
        AppUser appUser = getUserByEmail(email);
        boolean isAdmin = isAdmin(appUser);
        if (isAdmin) {
            throw new BadRequestException("Can't remove ADMINISTRATOR role!");
        }
        appUserRepository.delete(appUser);
        GeneralApplicationEvent generalApplicationEvent = getGeneralApplicationEvent(new GeneralApplicationEventData(
                ApplicationEventName.DELETE_USER,
                userDetails.getUsername().toLowerCase(),
                email
        ));
        applicationEventPublisher.publishEvent(generalApplicationEvent);
        return new ResponseEntity<>(new DeletedUserResponseDto(email), HttpStatus.OK);
    }

    private static boolean isAdmin(AppUser appUser) {
        return appUser.getRoles().stream().anyMatch(x -> x.getName().endsWith("ROLE_ADMINISTRATOR"));
    }

    private static boolean isBusiness(AppUser appUser) {
        return appUser.getRoles().stream().anyMatch(x -> x.getName().endsWith("ROLE_ACCOUNTANT"));
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found!"));
    }

    @PutMapping("/admin/user/role")
    public ResponseEntity<SignUpResponseDTO> updateUserRole(@RequestBody @Valid UpdateUserRoleRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        // user not found 404
        AppUser user = getUserByEmail(requestDto.getUser());
        // role not found 404
        String roleName = "ROLE_" + requestDto.getRole();
        Role role = roleRepository.findByName(roleName).orElseThrow(() ->
                new NotFoundException("Role not found!"));
        String roleAdministrator = "ROLE_ADMINISTRATOR";
        String roleBusiness = "ROLE_ACCOUNTANT";
        String roleAuditor = "ROLE_AUDITOR";
        // delete role which role that user not having 400
        if (requestDto.getOperation() == UpdateRoleOperationType.REMOVE) {
            boolean existUserRole = user.getRoles().stream().anyMatch(x -> x.getName().equals(roleName));
            if (!existUserRole) {
                throw new BadRequestException("The user does not have a role!");
            }
            if (roleName.equals(roleAdministrator)) {
                throw new BadRequestException("Can't remove ADMINISTRATOR role!");
            }
            // if user having only one role 400
            if (user.getRoles().size() == 1) {
                throw new BadRequestException("The user must have at least one role!");
            }
            user.getRoles().remove(role);
            AppUser updatedUser = appUserRepository.save(user);
            GeneralApplicationEvent generalApplicationEvent = getGeneralApplicationEvent(new GeneralApplicationEventData(
                    ApplicationEventName.REMOVE_ROLE,
                    userDetails.getUsername().toLowerCase(),
                    "Remove role %s from %s".formatted(requestDto.getRole(), user.getEmail())
            ));
            applicationEventPublisher.publishEvent(generalApplicationEvent);
            List<String> roles = updatedUser.getRoles().stream().map(Role::getName).toList();
            return new ResponseEntity<>(new SignUpResponseDTO(user.getId(), user.getName(), user.getLastname(), user.getEmail(), roles), HttpStatus.OK);
        }
        // if remove administrator role 400
        // If an administrative user is granted a business role or vice versa, respond with the HTTP Bad Request status (400)
        if (isAdmin(user) && (roleName.equals(roleBusiness) || roleName.equals("ROLE_USER") || roleName.equals(roleAuditor))) {
            throw new BadRequestException("The user cannot combine administrative and business roles!");
        }
        if (roleName.equals(roleAdministrator) && !isAdmin(user)) {
            throw new BadRequestException("The user cannot combine administrative and business roles!");
        }
        user.addRole(role);
        appUserRepository.save(user);
        GeneralApplicationEvent generalApplicationEvent = getGeneralApplicationEvent(new GeneralApplicationEventData(
                ApplicationEventName.GRANT_ROLE,
                userDetails.getUsername().toLowerCase(),
                "Grant role %s to %s".formatted(requestDto.getRole(), user.getEmail())
        ));
        applicationEventPublisher.publishEvent(generalApplicationEvent);
        List<String> roles = new java.util.ArrayList<>(user.getRoles().stream().map(Role::getName).toList());
        Collections.sort(roles);
        return new ResponseEntity<>(new SignUpResponseDTO(user.getId(), user.getName(), user.getLastname(), user.getEmail(), roles), HttpStatus.OK);
    }

    @PutMapping("/admin/user/access")
    public ResponseEntity<StatusResponseDto> lockUser(@RequestBody @Valid LockUserRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = getUserByEmail(requestDto.getUser());
        if (requestDto.getOperation() == LockUserOperation.LOCK) {
            if (isAdmin(user)) {
                throw new BadRequestException("Can't lock the ADMINISTRATOR!");
            }
            user.setAccountNonLocked(false);
        } else {
            user.setFailedAttempt(0);
            user.setAccountNonLocked(true);
        }
        GeneralApplicationEvent generalApplicationEvent = getGeneralApplicationEvent(new GeneralApplicationEventData(
                requestDto.getOperation() == LockUserOperation.LOCK ? ApplicationEventName.LOCK_USER : ApplicationEventName.UNLOCK_USER,
                userDetails.getUsername().toLowerCase(),
                requestDto.getOperation() == LockUserOperation.LOCK ? "Lock user " + requestDto.getUser().toLowerCase() : "Unlock user " + requestDto.getUser().toLowerCase()
        ));
        applicationEventPublisher.publishEvent(generalApplicationEvent);
        appUserRepository.save(user);
        String status = requestDto.getOperation() == LockUserOperation.UNLOCK ? "unlocked" : "locked";
        return new ResponseEntity<>(new StatusResponseDto("User %s %s!".formatted(requestDto.getUser().toLowerCase(),status)), HttpStatus.OK);
    }

    private GeneralApplicationEvent getGeneralApplicationEvent(GeneralApplicationEventData requestDto) {
        return new GeneralApplicationEvent(this, requestDto);
    }

    private void branchedPasswordValidation(String requestDto) {
        if (breachedPasswords().contains(requestDto)) {
            throw new BranchedPasswordException();
        }
    }

    public List<String> breachedPasswords() {
        return List.of(
                "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"
        );
    }

}
