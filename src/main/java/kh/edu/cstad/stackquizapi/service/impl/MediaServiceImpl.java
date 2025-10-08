package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Media;
import kh.edu.cstad.stackquizapi.dto.response.MediaResponse;
import kh.edu.cstad.stackquizapi.repository.MediaRepository;
import kh.edu.cstad.stackquizapi.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    @Value("${media.server-path}")
    private String serverPath;

    @Value("${media.base-uri}")
    private String baseUri;

    @Override
    public MediaResponse upload(MultipartFile file) {

        String name = UUID.randomUUID().toString();
        int lastIndex = Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf(".");
        String extension = file.getOriginalFilename().substring(lastIndex + 1);

        Path path = Paths.get(serverPath + String.format("%s.%s", name, extension));

        try {
            Files.copy(file.getInputStream(), path);
        } catch (IOException exception) {
            log.error(exception.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "File upload failed");
        }

        Media media = new Media();
        media.setName(name+"."+extension);
        media.setExtension(extension);
        media.setMimeTypeFile(file.getContentType());
        media.setIsDeleted(false);

        media = mediaRepository.save(media);

        return MediaResponse.builder()
                .name(media.getName())
                .extension(media.getExtension())
                .mimeTypeFile(media.getMimeTypeFile())
                .uri(baseUri + String.format("%s.%s", name, extension))
                .size(file.getSize())
                .build();
    }

    @Override
    public List<MediaResponse> uploadMultipleFiles(List<MultipartFile> files) {
        return files.stream()
                .map(this::upload)
                .toList();
    }

    @Override
    public ResponseEntity<Resource> download(String fileName) {
        if (fileName == null || fileName.isBlank() ||
                fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        Path filePath = Paths.get(serverPath).resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Invalid file URL");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not determine file type");
        }
    }

    @Override
    public void deleteByName(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file name");
        }

        Path filePath = Paths.get(serverPath).resolve(fileName);

        try {
            if (!Files.exists(filePath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "File not found");
            }

            Files.delete(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to deleteByName file");
        }

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            String name = fileName.substring(0, dotIndex);
            mediaRepository.findMediaByName(name)
                    .ifPresent(mediaRepository::delete);
        }
    }

}
