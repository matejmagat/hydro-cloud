package hr.fer.hydro.service.impl;


import hr.fer.hydro.db.RolePrivilegeDao;
import hr.fer.hydro.db.UserDao;
import hr.fer.hydro.db.UserRoleDao;
import hr.fer.hydro.db.entity.RolePrivilegeEntity;
import hr.fer.hydro.db.entity.UserEntity;
import hr.fer.hydro.db.entity.UserRoleEntity;
import lombok.RequiredArgsConstructor;
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
        return userRoleDao.findAllByUser(user)
                .stream()
                .map(UserRoleEntity::getRole)
                .map(rolePrivilegeDao::findByRole)
                .flatMap(Collection::stream)
                .map(RolePrivilegeEntity::getPrivilege)
                .map(privilege -> new SimpleGrantedAuthority(privilege.getPrivilegeName()))
                .toList();
    }

}

