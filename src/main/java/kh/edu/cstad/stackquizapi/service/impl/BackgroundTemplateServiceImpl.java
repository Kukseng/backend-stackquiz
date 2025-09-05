package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.BackgroundTemplate;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.dto.request.CreateBgTemplateRequest;
import kh.edu.cstad.stackquizapi.dto.response.BackgroundTemplateResponse;
import kh.edu.cstad.stackquizapi.repository.BackgroundTemplateRepository;
import kh.edu.cstad.stackquizapi.repository.QuestionRepository;
import kh.edu.cstad.stackquizapi.service.BackgroundTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackgroundTemplateServiceImpl implements BackgroundTemplateService {

    private BackgroundTemplateRepository backgroundTemplateRepository;
    private QuestionRepository questionRepository;

    @Override
    public BackgroundTemplateResponse createBackgroundTemplate(CreateBgTemplateRequest createBgTemplateRequest) {

        BackgroundTemplate template = new BackgroundTemplate();
        template.setTemplateImage(createBgTemplateRequest.templateImage());

        backgroundTemplateRepository.save(template);

        return BackgroundTemplateResponse.builder()
                .id(template.getId())
                .templateImage(template.getTemplateImage())
                .build();
    }
}
