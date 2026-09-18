package hr.fer.hydro.auth.auth.service.impl;

import hr.fer.hydro.auth.persistence.entities.RolePrivilegeEntity;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.entities.UserRoleEntity;
import hr.fer.hydro.auth.persistence.repositories.RolePrivilegeDao;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import hr.fer.hydro.auth.persistence.repositories.UserRoleDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final RolePrivilegeDao rolePrivilegeDao;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final UserEntity user = userDao.findByUsername(username).orElseThrow(
                () -> new ResponseStatusException(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()))
        );
        return new User(
                user.getUsername(),
                user.getPassword(),
                !user.getBlocked(),
                true,
                true,
                true,
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(final UserEntity user) {
        return userRoleDao.findByUser(user)
                .stream()
                .map(UserRoleEntity::getRole)
                .map(rolePrivilegeDao::findByRole)
                .flatMap(Collection::stream)
                .map(RolePrivilegeEntity::getPrivilege)
                .map(privilege -> new SimpleGrantedAuthority(privilege.getPrivilegeName()))
                .toList();
    }

}

