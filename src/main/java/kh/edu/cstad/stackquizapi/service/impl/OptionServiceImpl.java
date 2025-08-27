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

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;
    private final QuestionRepository questionRepository;
    private final OptionMapper optionMapper;

    @Override
    public OptionResponse addNewOption(String questionId, AddOptionRequest addOptionRequest) {

        if (!questionRepository.existsById(questionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Question id not found");
        }

        if (addOptionRequest.optionText().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Option text cannot be null or empty");
        }

        if (addOptionRequest.optionOrder() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Option order cannot be null");
        }

        if (addOptionRequest.isCorrected() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "isCorrected cannot be null");
        }

        Question question = questionRepository.findById(addOptionRequest.questionId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question ID not found")
                );

        Option option = new Option();
        optionMapper.fromAddOptionRequest(addOptionRequest);
        option.setQuestion(question);
        option.setCreatedAt(Timestamp.from(Instant.now()));

        option = optionRepository.save(option);

        return optionMapper.toOptionResponse(option);
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
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Option ID not found")
                );

        try {
            optionMapper.toQuestionPartially(updateOptionRequest, option);

            Option updatedOption = optionRepository.save(option);

            log.info("Successfully updated option with ID: {}", optionId);

            return optionMapper.toOptionResponse(updatedOption);
        }  catch (DataAccessException e) {
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
    public void deletedOptionById(String optionId, Option option) {

        if (optionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Option ID cannot be null");
        }

        try {
            Question question = questionRepository.findById(optionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Option with ID " + optionId + " not found"));

            log.info("Deleting option '{}' with ID: {}", option.getOptionText(), optionId);
            questionRepository.delete(question);
            log.info("Successfully deleted option with ID: {}", optionId);

        } catch (DataIntegrityViolationException exception) {
            log.error("Cannot delete option {} due to data integrity constraints", option, exception);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete option as it's used in active quizzes");

        } catch (DataAccessException exception) {
            log.error("Database error while deleting option {}", optionId, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete option from database");

        } catch (ResponseStatusException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error("Unexpected error while deleting option {}", optionId, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while deleting option");
        }

    }
}
