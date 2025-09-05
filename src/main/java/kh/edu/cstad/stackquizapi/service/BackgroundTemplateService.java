package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateBgTemplateRequest;
import kh.edu.cstad.stackquizapi.dto.response.BackgroundTemplateResponse;

public interface BackgroundTemplateService {

    BackgroundTemplateResponse createBackgroundTemplate(CreateBgTemplateRequest createBgTemplateRequest);



}
