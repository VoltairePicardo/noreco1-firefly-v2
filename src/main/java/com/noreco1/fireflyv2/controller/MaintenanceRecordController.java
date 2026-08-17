package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.MaintenanceRecordDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.MaintenanceRecord;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.MaintenanceRecordService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/api/maintenance-record")
public class MaintenanceRecordController {

    @Autowired
    @Qualifier("maintenanceRecordServiceImpl")
    private MaintenanceRecordService maintenanceRecordService;

    @Autowired
    @Qualifier("maintenanceRecordServiceImpl")
    PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/list")
    public Page<MaintenanceRecord> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(name = "s", defaultValue = "") String startDate,
            @RequestParam(name = "e", defaultValue = "") String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maintenanceDate"));

        if (q != null && !q.isBlank()) {
            return maintenanceRecordService.findAllByQuery(q, startDate, endDate, pageable);
        }
        return maintenanceRecordService.findAll(startDate, endDate, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRecordDto> getById(@PathVariable Integer id) {
        MaintenanceRecordDto dto = maintenanceRecordService.findOne(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody MaintenanceRecord maintenanceRecord) {
        BindingResult bindingResult = new BeanPropertyBindingResult(maintenanceRecord, "maintenanceRecord");
        return maintenanceRecordService.create(maintenanceRecord, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody MaintenanceRecord maintenanceRecord) {
        BindingResult bindingResult = new BeanPropertyBindingResult(maintenanceRecord, "maintenanceRecord");
        return maintenanceRecordService.update(maintenanceRecord, bindingResult, messageSource);
    }

    @RequestMapping(value = "/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {

        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);

        String template = GlobalConstant.JASPER_BASE_PATH + "/support-modules/MaintenanceRecord.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
