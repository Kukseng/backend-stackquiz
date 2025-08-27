package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final Keycloak keycloak;

    @Override
    public void assignRole(String userId, String roleName) {

        log.info("assignRole userId={} roleName={}", userId, roleName);

        UserResource userResource = keycloak.realm("sq-api")
                .users().get(userId);

        log.info("assignRole userResource={}", userResource);

        RoleRepresentation role = keycloak.realm("sq-api")
                .roles()
                .get(roleName)
                .toRepresentation();

        userResource.roles().realmLevel()
                .add(List.of(role));
    }

}
