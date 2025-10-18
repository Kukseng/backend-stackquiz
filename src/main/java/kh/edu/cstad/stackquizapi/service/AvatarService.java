package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateAvatarRequest;
import kh.edu.cstad.stackquizapi.dto.response.AvatarResponse;

import java.util.List;

public interface AvatarService {

    /**
     * Create a new avatar
     * @param createAvatarRequest Avatar creation request
     * @return Created avatar response
     */
    AvatarResponse createNewAvatar(CreateAvatarRequest createAvatarRequest);

    /**
     * Get all avatars
     * @return List of all avatars
     */
    List<AvatarResponse> getAllAvatars();

    /**
     * Get avatar by ID
     * @param id Avatar ID
     * @return Avatar response
     */
    AvatarResponse getAvatarById(Long id);

    /**
     * Update existing avatar
     * @param id Avatar ID
     * @param updateRequest Update request
     * @return Updated avatar response
     */
    AvatarResponse updateAvatar(Long id, CreateAvatarRequest updateRequest);

    /**
     * Delete avatar by ID
     * @param id Avatar ID
     */
    void deleteAvatar(Long id);

    /**
     * Create multiple avatars at once
     * @param requests List of avatar creation requests
     * @return List of created avatars
     */
    List<AvatarResponse> createMultipleAvatars(List<CreateAvatarRequest> requests);

    /**
     * Initialize 20 default avatars
     * @return List of created default avatars
     */
    List<AvatarResponse> initializeDefaultAvatars();

    /**
     * Check if avatar exists by ID
     * @param id Avatar ID
     * @return true if exists, false otherwise
     */
    boolean avatarExists(Long id);

    /**
     * Count total number of avatars
     * @return Total count
     */
    long countAvatars();
}

