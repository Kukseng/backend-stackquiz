package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Avatar;
import kh.edu.cstad.stackquizapi.dto.request.CreateAvatarRequest;
import kh.edu.cstad.stackquizapi.dto.response.AvatarResponse;
import kh.edu.cstad.stackquizapi.repository.AvatarRepository;
import kh.edu.cstad.stackquizapi.service.AvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private final AvatarRepository avatarRepository;

    @Override
    @Transactional
    public AvatarResponse createNewAvatar(CreateAvatarRequest createAvatarRequest) {
        log.info("Creating new avatar: {}", createAvatarRequest.name());
        
        Avatar avatar = new Avatar();
        avatar.setAvatarNo(createAvatarRequest.avatarNo());
        avatar.setName(createAvatarRequest.name());
        
        Avatar savedAvatar = avatarRepository.save(avatar);
        log.info("✅ Avatar created successfully with ID: {}", savedAvatar.getId());
        
        return mapToResponse(savedAvatar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvatarResponse> getAllAvatars() {
        log.info("Fetching all avatars");
        List<Avatar> avatars = avatarRepository.findAll();
        log.info("Found {} avatars", avatars.size());
        return avatars.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarResponse getAvatarById(Long id) {
        log.info("Fetching avatar with ID: {}", id);
        Avatar avatar = avatarRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Avatar not found with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Avatar not found with ID: " + id
                    );
                });
        return mapToResponse(avatar);
    }

    @Override
    @Transactional
    public AvatarResponse updateAvatar(Long id, CreateAvatarRequest updateRequest) {
        log.info("Updating avatar with ID: {}", id);
        
        Avatar avatar = avatarRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Avatar not found with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Avatar not found with ID: " + id
                    );
                });
        
        avatar.setAvatarNo(updateRequest.avatarNo());
        avatar.setName(updateRequest.name());
        
        Avatar updatedAvatar = avatarRepository.save(avatar);
        log.info("✅ Avatar updated successfully: {}", updatedAvatar.getName());
        
        return mapToResponse(updatedAvatar);
    }

    @Override
    @Transactional
    public void deleteAvatar(Long id) {
        log.info("Deleting avatar with ID: {}", id);
        
        if (!avatarRepository.existsById(id)) {
            log.error("❌ Avatar not found with ID: {}", id);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Avatar not found with ID: " + id
            );
        }
        
        avatarRepository.deleteById(id);
        log.info("✅ Avatar deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public List<AvatarResponse> createMultipleAvatars(List<CreateAvatarRequest> requests) {
        log.info("Creating {} avatars in batch", requests.size());
        
        List<Avatar> avatars = requests.stream()
                .map(request -> {
                    Avatar avatar = new Avatar();
                    avatar.setAvatarNo(request.avatarNo());
                    avatar.setName(request.name());
                    return avatar;
                })
                .collect(Collectors.toList());
        
        List<Avatar> savedAvatars = avatarRepository.saveAll(avatars);
        log.info("✅ Successfully created {} avatars", savedAvatars.size());
        
        return savedAvatars.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AvatarResponse> initializeDefaultAvatars() {
        log.info("Initializing default avatars");
        
        // Check if avatars already exist
        long existingCount = avatarRepository.count();
        if (existingCount > 0) {
            log.warn("⚠️ Avatars already exist (count: {}). Skipping initialization.", existingCount);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Avatars already exist. Delete existing avatars first or use batch endpoint."
            );
        }
        
        // Create 20 default avatars
        List<CreateAvatarRequest> defaultAvatars = List.of(
                new CreateAvatarRequest(1, "Robot"),
                new CreateAvatarRequest(2, "Alien"),
                new CreateAvatarRequest(3, "Cat"),
                new CreateAvatarRequest(4, "Dog"),
                new CreateAvatarRequest(5, "Panda"),
                new CreateAvatarRequest(6, "Fox"),
                new CreateAvatarRequest(7, "Bear"),
                new CreateAvatarRequest(8, "Rabbit"),
                new CreateAvatarRequest(9, "Lion"),
                new CreateAvatarRequest(10, "Tiger"),
                new CreateAvatarRequest(11, "Monkey"),
                new CreateAvatarRequest(12, "Elephant"),
                new CreateAvatarRequest(13, "Giraffe"),
                new CreateAvatarRequest(14, "Penguin"),
                new CreateAvatarRequest(15, "Owl"),
                new CreateAvatarRequest(16, "Dragon"),
                new CreateAvatarRequest(17, "Unicorn"),
                new CreateAvatarRequest(18, "Phoenix"),
                new CreateAvatarRequest(19, "Ninja"),
                new CreateAvatarRequest(20, "Pirate")
        );
        
        List<AvatarResponse> createdAvatars = createMultipleAvatars(defaultAvatars);
        log.info("✅ Successfully initialized {} default avatars", createdAvatars.size());
        
        return createdAvatars;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean avatarExists(Long id) {
        boolean exists = avatarRepository.existsById(id);
        log.debug("Avatar with ID {} exists: {}", id, exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAvatars() {
        long count = avatarRepository.count();
        log.debug("Total avatars count: {}", count);
        return count;
    }

    /**
     * Map Avatar entity to AvatarResponse DTO
     */
    private AvatarResponse mapToResponse(Avatar avatar) {
        return new AvatarResponse(
                avatar.getId(),
                avatar.getAvatarNo(),
                avatar.getName()
        );
    }
}

