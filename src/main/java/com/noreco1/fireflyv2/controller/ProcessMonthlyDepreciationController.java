package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.AssetDepreciation;
import com.noreco1.fireflyv2.model.AssetDepreciationDetail;
import com.noreco1.fireflyv2.model.form.YearMonth;
import com.noreco1.fireflyv2.service.AssetDepreciationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/process-monthly-depreciation")
public class ProcessMonthlyDepreciationController {

    @Autowired
    private AssetDepreciationService assetDepreciationService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list/{year}/{month}")
    public List<AssetDepreciation> list(@PathVariable Integer year, @PathVariable Integer month) {
        return assetDepreciationService.findAllByYearAndMonth(year, month);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDepreciation> getById(@PathVariable Integer id) {
        AssetDepreciation data = assetDepreciationService.findById(id);
        return data != null ? ResponseEntity.ok(data) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/detail")
    public List<Map> getDetails(@PathVariable Integer id) {
        return assetDepreciationService.findAllDetailsById(id);
    }

    @GetMapping("/{id}/detail/footer")
    public Map getFooter(@PathVariable Integer id) {
        return assetDepreciationService.getTotals(id);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody YearMonth yearMonth) {
        BindingResult bindingResult = new BeanPropertyBindingResult(yearMonth, "yearMonth");
        return assetDepreciationService.process(yearMonth, bindingResult, messageSource);
    }
}
