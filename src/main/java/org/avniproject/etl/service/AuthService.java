package org.avniproject.etl.service;

import com.auth0.jwk.SigningKeyNotFoundException;
import org.avniproject.etl.domain.User;
import org.avniproject.etl.repository.OrganisationRepository;
import org.avniproject.etl.repository.UserRepository;
import org.avniproject.etl.security.IAMAuthService;
import org.avniproject.etl.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
//    public final static SimpleGrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority(User.USER);
//    public final static SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority(User.ADMIN);
//    public final static SimpleGrantedAuthority ORGANISATION_ADMIN_AUTHORITY = new SimpleGrantedAuthority(User.ORGANISATION_ADMIN);
//    public final static List<SimpleGrantedAuthority> ALL_AUTHORITIES = Arrays.asList(USER_AUTHORITY, ADMIN_AUTHORITY, ORGANISATION_ADMIN_AUTHORITY);
//    private final AccountAdminRepository accountAdminRepository;
    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final IdpServiceFactory idpServiceFactory;

    @Autowired
    public AuthService(UserRepository userRepository, OrganisationRepository organisationRepository,
                       IdpServiceFactory idpServiceFactory) {
        this.idpServiceFactory = idpServiceFactory;
        this.userRepository = userRepository;
        this.organisationRepository = organisationRepository;
//        this.accountAdminRepository = accountAdminRepository;
    }

    public UserContext authenticateByToken(String authToken, String organisationUUID) {
//        becomeSuperUser();
        IAMAuthService iamAuthService = idpServiceFactory.getAuthService();
        UserContext userContext = new UserContext();
        try {
            User user = iamAuthService.getUserFromToken(authToken);
            System.out.println("Token --" + authToken);
            userContext.setUser(iamAuthService.getUserFromToken(authToken));
        } catch (SigningKeyNotFoundException signingKeyNotFoundException) {
            throw new RuntimeException(signingKeyNotFoundException);
        }
        userContext.setAuthToken(authToken);
        return userContext;
    }

//    public UserContext authenticateByUserName(String username, String organisationUUID) {
//        becomeSuperUser();
//        return changeUser(userRepository.findByUsername(username), organisationUUID);
//    }

//    public UserContext authenticateByUserId(Long userId, String organisationUUID) {
//        becomeSuperUser();
//        Optional<User> user = userRepository.findById(userId);
//        if (user.isPresent()) {
//            return changeUser(user.get(), organisationUUID);
//        }
//        throw new RuntimeException(String.format("Not found: User{id='%s'}", userId));
//    }
//
//    private Authentication attemptAuthentication(User user, String organisationUUID) {
//        UserContext userContext = new UserContext();
//        UserContextHolder.create(userContext);
//        if (user == null) {
//            return null;
//        }
//        List<AccountAdmin> accountAdmins = accountAdminRepository.findByUser_Id(user.getId());
//        user.setAdmin(accountAdmins.size() > 0);
//        Organisation organisation = null;
//        if (user.isAdmin() && organisationUUID != null) {
//            user.setOrgAdmin(true);
//            organisation = organisationRepository.findByUuid(organisationUUID);
//        } else if (user.getOrganisationId() != null) {
//            organisation = organisationRepository.findOne(user.getOrganisationId());
//        }
//        userContext.setUser(user);
//        userContext.setOrganisation(organisation);
//        userContext.setOrganisationUUID(organisationUUID);
//
//        List<SimpleGrantedAuthority> authorities = ALL_AUTHORITIES.stream()
//                .filter(authority -> userContext.getRoles().contains(authority.getAuthority()))
//                .collect(Collectors.toList());
//
//        if (authorities.isEmpty())
//            return null;
//        return createTempAuth(authorities);
//    }
//
//    private UserContext changeUser(User user, String organisationUUID) {
//        SecurityContextHolder.getContext().setAuthentication(attemptAuthentication(user, organisationUUID));
//        return UserContextHolder.getUserContext();
//    }
//
//    private void becomeSuperUser() {
//        UserContextHolder.clear();
//        SecurityContextHolder.getContext().setAuthentication(createTempAuth(ALL_AUTHORITIES));
//    }
//
//    private Authentication createTempAuth(List<SimpleGrantedAuthority> authorities) {
//        String token = UUID.randomUUID().toString();
//        return new AnonymousAuthenticationToken(token, token, authorities);
//    }

}