package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class MessageFormatter {

    private MessageSource messageSource;
    private BindingResult bindingResult;
    private PostResponse response;

    public MessageFormatter(PostResponse response) {
        this.response = response;
    }
    public MessageFormatter(BindingResult bindingResult, MessageSource messageSource, PostResponse response) {
        this.bindingResult = bindingResult;
        this.messageSource = messageSource;
        this.response = response;
    }

    public void buildErrorMessages() {
        // create service for this later
        for (Object object : bindingResult.getAllErrors()) {
            if (object instanceof FieldError) {
                FieldError fieldError = (FieldError) object;

                /**
                 * Use null as second parameter if you do not use i18n
                 * (internationalization)
                 */
                String message = messageSource.getMessage(fieldError, null);
                response.getFields().add(fieldError.getField());
                response.getMessages().add(message);
            }
        }
    }

    public PostResponse getResponse() {
        return this.response;
    }
}
