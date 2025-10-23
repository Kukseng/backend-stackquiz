package kh.edu.cstad.stackquizapi.service;

/**
 * Service interface for managing user roles.
 * <p>
 * Provides functionality to assign roles to users for access control and permissions.
 * </p>
 *
 * author Pech Rattanakmony
 * @since 1.0
 */
public interface RoleService {

    /**
     * Assign a role to a specific user.
     *
     * @param userId the unique ID of the user
     * @param roleName the name of the role to assign
     */
    void assignRole(String userId, String roleName);
}

