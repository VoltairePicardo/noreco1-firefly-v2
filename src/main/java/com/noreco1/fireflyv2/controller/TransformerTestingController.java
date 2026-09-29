package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.TransformerTesting;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.TransformerTestingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("api/transformer-testing")
@RequiredArgsConstructor
public class TransformerTestingController {

    private final TransformerTestingService transformerTestingService;
    private final MessageSource messageSource;
    private final DownloadService downloadService;

    @GetMapping(value = "/{id}")
    public TransformerTesting getById(@PathVariable Integer id) {
        return transformerTestingService.getById(id);
    }

    @GetMapping(value = "/list")
    public Page<TransformerTesting> getAll(@RequestParam(defaultValue = "") String query, Pageable pageable) {
        if(query == null || query.isBlank()) {
            return transformerTestingService.findAll(pageable);
        }

        return transformerTestingService.findAllByQuery(query, pageable);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody TransformerTesting transformerTesting, BindingResult bindingResult) {
        return transformerTestingService.create(transformerTesting, bindingResult, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto postData, BindingResult bindingResult) {
        return transformerTestingService.process(postData, bindingResult, messageSource);
    }


    @RequestMapping(value="/print/export/{id}")
    public void exportList(@PathVariable Integer id,
                           @RequestParam(value = "type") String type,
                           HttpServletResponse response, HttpServletRequest request) {

        HashMap<String, Object> params = transformerTestingService.transformerTestingParameters(request, id);
        JRDataSource dataSource = transformerTestingService.datasourceTransformerTesting(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/special-equipment-testing/TransformerTesting.jrxml";

        downloadService.download(type, response, params, template, dataSource);
    }

}
