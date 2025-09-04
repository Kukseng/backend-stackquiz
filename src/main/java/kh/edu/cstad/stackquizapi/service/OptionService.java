package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.dto.request.AddOptionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateOptionRequest;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;

import java.util.List;


/**
 * Service interface for managing options related to questions.
 * Provides methods to add, update, and delete options.
 *
 * @author Pech Rattanakmony
 */
public interface OptionService {

    /**
     * Adds a new option to the specified question.
     *
     * @param questionId the ID of the question to which the option will be added
     * @param addOptionRequest the request object containing the details of the option to add
     * @return the response containing the newly added option
     */
    List<OptionResponse> addNewOptions(String questionId, List<AddOptionRequest> addOptionRequests);

    List<OptionResponse> gelAllOptions();

    List<OptionResponse> getOptionsByQuestionId(String questionId);

    /**
     * Updates an existing option by its ID.
     *
     * @param optionId the ID of the option to update
     * @param updateOptionRequest the request object containing updated details
     * @return the response containing the updated option
     */
    OptionResponse updateOptionById(String optionId, UpdateOptionRequest updateOptionRequest);

    /**
     * Deletes an option by its ID.
     *
     * @param optionId the ID of the option to delete
     * @param option the option object to be deleted
     */
    void deleteOptionById(String optionId);
}

