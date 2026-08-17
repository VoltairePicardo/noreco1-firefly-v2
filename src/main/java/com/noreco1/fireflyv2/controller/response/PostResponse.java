package com.noreco1.fireflyv2.controller.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PostResponse implements Serializable {

    private int modelId = 0;
    private List<Integer> modelIds = new ArrayList<>();
    private HttpStatus status = HttpStatus.NO_CONTENT;
    private boolean success;
    private ArrayList<String> fields = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private String successMessage;
    private String failureMessage = "Something went wrong.";
    private int logId = 0;
    private List<Map<String, Object>> duplicates = new ArrayList<>();

    private boolean isNotAuthorized = false;

    public void setSuccessMessage(String successMessage) {
        this.success = true;
        this.status = HttpStatus.OK;
        this.failureMessage = "";
        this.successMessage = successMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.success = false;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.failureMessage = failureMessage;
    }
}
