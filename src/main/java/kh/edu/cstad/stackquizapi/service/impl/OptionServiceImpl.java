package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.dto.request.AddOptionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateOptionRequest;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.mapper.OptionMapper;
import kh.edu.cstad.stackquizapi.repository.OptionRepository;
import kh.edu.cstad.stackquizapi.repository.QuestionRepository;
import kh.edu.cstad.stackquizapi.service.OptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;
    private final QuestionRepository questionRepository;
    private final OptionMapper optionMapper;

    @Override
    public List<OptionResponse> addNewOptions(String questionId, List<AddOptionRequest> addOptionRequests) {

        // Validate the list itself
        if (addOptionRequests == null || addOptionRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Option requests list cannot be null or empty");
        }


        for (int i = 0; i < addOptionRequests.size(); i++) {
            AddOptionRequest request = addOptionRequests.get(i);

            if (request.optionText() == null || request.optionText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Option text cannot be null or empty at index %d", i));
            }

            if (request.isCorrected() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("isCorrected cannot be null at index %d", i));
            }
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question ID not found"));

        Integer maxOrder = optionRepository.findMaxOptionOrderByQuestionId(questionId);
        int startingOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        List<Option> options = new ArrayList<>();
        for (int i = 0; i < addOptionRequests.size(); i++) {
            AddOptionRequest request = addOptionRequests.get(i);
            Option option = optionMapper.fromAddOptionRequest(request);
            option.setQuestion(question);
            option.setOptionOrder(startingOrder + i);
            option.setCreatedAt(Timestamp.from(Instant.now()));
            options.add(option);
        }

        List<Option> savedOptions = optionRepository.saveAll(options);

        return savedOptions.stream()
                .map(optionMapper::toOptionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OptionResponse> gelAllOptions() {

        try {

            List<Option> options = optionRepository.findAll();

            if (options.isEmpty()) {
                log.info("No option found");
                return Collections.emptyList();
            }

            log.info("Found {} options", options.size());

            return options.stream()
                    .map(optionMapper::toOptionResponse)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("Unexpected error while fetching all options", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while fetching questions");
        }


    }

    @Override
    public List<OptionResponse> getOptionsByQuestionId(String questionId) {

        try {

            List<Option> options = optionRepository.findByQuestion_Id(questionId);

            if (options.isEmpty()) {
                log.info("No questions found");
                return Collections.emptyList();
            }

            log.info("Found {} questions", options.size());

            return options.stream()
                    .map(optionMapper::toOptionResponse)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("Unexpected error while fetching all options", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while fetching questions");
        }

    }

    @Transactional
    @Override
    public OptionResponse updateOptionById(String optionId, UpdateOptionRequest updateOptionRequest) {

        if (optionId == null || optionId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Option ID cannot be null or empty");
        }

        log.info("Updating option With ID {}", optionId);

        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Option with ID " + optionId + " not found"));

        optionRepository.delete(option);
        log.info("Successfully deleted option with ID: {}", optionId);


        try {
            optionMapper.toQuestionPartially(updateOptionRequest, option);


            Option updatedOption = optionRepository.save(option);

            log.info("Successfully updated option with ID: {}", optionId);

            return optionMapper.toOptionResponse(updatedOption);
        } catch (DataAccessException e) {
            log.error("Database error while updating question with ID: {}", optionId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update question due to database error", e);

        } catch (Exception e) {
            log.error("Unexpected error while updating question with ID: {}", optionId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update question", e);
        }
    }

    @Override
    public void deleteOptionById(String optionId) {

        if (optionId == null || optionId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Option ID cannot be null or empty");
        }

        try {
            Option option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Option with ID " + optionId + " not found"));

            optionRepository.delete(option);
            log.info("Successfully deleted option with ID: {}", optionId);

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete option as it's used in active quizzes");

        } catch (DataAccessException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete option from database");

        } catch (Exception ex) {
            log.error("Unexpected error while deleting option {}", optionId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while deleting option");
        }
    }

}