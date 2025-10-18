package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.CreateAvatarRequest;
import kh.edu.cstad.stackquizapi.dto.response.AvatarResponse;
import kh.edu.cstad.stackquizapi.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
@Tag(name = "Avatar", description = "Avatar management endpoints")
public class AvatarController {

    private final AvatarService avatarService;

    /**
     * Get all avatars (public endpoint)
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all avatars", description = "Retrieve all available avatars for participants")
    public List<AvatarResponse> getAllAvatars() {
        return avatarService.getAllAvatars();
    }

    /**
     * Get avatar by ID (public endpoint)
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get avatar by ID", description = "Retrieve a specific avatar by its ID")
    public AvatarResponse getAvatarById(@PathVariable Long id) {
        return avatarService.getAvatarById(id);
    }

    /**
     * Create new avatar (public endpoint for easy setup)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new avatar", description = "Create a new avatar")
    public AvatarResponse createAvatar(@Valid @RequestBody CreateAvatarRequest request) {
        return avatarService.createNewAvatar(request);
    }

    /**
     * Create multiple avatars at once (public endpoint for easy setup)
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create multiple avatars", description = "Create multiple avatars in a single request")
    public List<AvatarResponse> createAvatars(@Valid @RequestBody List<CreateAvatarRequest> requests) {
        return avatarService.createMultipleAvatars(requests);
    }

    /**
     * Update avatar
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update avatar", description = "Update an existing avatar")
    public AvatarResponse updateAvatar(
            @PathVariable Long id,
            @Valid @RequestBody CreateAvatarRequest request) {
        return avatarService.updateAvatar(id, request);
    }

    /**
     * Delete avatar
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete avatar", description = "Delete an avatar by ID")
    public void deleteAvatar(@PathVariable Long id) {
        avatarService.deleteAvatar(id);
    }

    /**
     * Initialize default avatars (convenience endpoint)
     */
    @PostMapping("/init-defaults")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initialize default avatars", description = "Create 20 default avatars for the quiz app")
    public List<AvatarResponse> initializeDefaultAvatars() {
        return avatarService.initializeDefaultAvatars();
    }

    /**
     * Check if avatar exists
     */
    @GetMapping("/{id}/exists")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check if avatar exists", description = "Check if an avatar exists by ID")
    public boolean avatarExists(@PathVariable Long id) {
        return avatarService.avatarExists(id);
    }

    /**
     * Get total avatar count
     */
    @GetMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get avatar count", description = "Get total number of avatars")
    public long countAvatars() {
        return avatarService.countAvatars();
    }
}

