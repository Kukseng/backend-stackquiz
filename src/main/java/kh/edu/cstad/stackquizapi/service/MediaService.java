package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.MediaResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for handling media operations such as uploading files.
 * @author aintantony
 */
public interface MediaService {

    /**
     * Uploads a single media file.
     *
     * @param file the file to upload
     * @return a response containing the uploaded media's information
     */
    MediaResponse upload(MultipartFile file);

    /**
     * Uploads multiple media files.
     *
     * @param files a list of files to upload
     * @return a list of responses, each containing information about an uploaded media file
     */
    List<MediaResponse> uploadMultipleFiles(List<MultipartFile> files);

    /**
     * Downloads a media file by its name.
     *
     * @param fileName the name of the file to download
     * @return a ResponseEntity containing the media file as a resource, with appropriate headers
     */
    ResponseEntity<Resource> download(String fileName);

    /**
     * Deletes a media file by its name.
     *
     * @param fileName the name of the file to delete
     */
    void deleteByName(String fileName);
}
