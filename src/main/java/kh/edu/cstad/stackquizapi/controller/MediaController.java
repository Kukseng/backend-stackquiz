package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.response.MediaResponse;
import kh.edu.cstad.stackquizapi.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/medias")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("upload-single")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse upload(@RequestPart MultipartFile file) {
        return mediaService.upload(file);
    }

}
