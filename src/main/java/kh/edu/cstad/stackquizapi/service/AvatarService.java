package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateAvatarRequest;
import kh.edu.cstad.stackquizapi.dto.response.AvatarResponse;

public interface AvatarService {

    AvatarResponse createNewAvatar(CreateAvatarRequest createAvatarRequest);

}
