package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.ItemTestingDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.ItemTesting;
import com.noreco1.fireflyv2.repo.ItemTestingDetailRepo;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.ItemTestingService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/item-testing")
public class ItemTestingController {

    private final ItemTestingService itemTestingService;
    private final MessageSource messageSource;
    private final ItemTestingDetailRepo itemTestingDetailRepo;
    private final PrintableVoucher printableVoucher;
    private final DownloadService downloadService;

    public ItemTestingController(ItemTestingService itemTestingService, MessageSource messageSource, ItemTestingDetailRepo itemTestingDetailRepo, @Qualifier("itemTestingServiceImpl") PrintableVoucher printableVoucher, DownloadService downloadService) {
        this.itemTestingService = itemTestingService;
        this.messageSource = messageSource;
        this.itemTestingDetailRepo = itemTestingDetailRepo;
        this.printableVoucher = printableVoucher;
        this.downloadService = downloadService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ItemTestingDto get(@PathVariable Integer id) {
        return itemTestingService.findById(id);
    }

    @GetMapping(value = "/list-paged")
    @ResponseBody
    public Page<Map<String, Object>> listPaged(@RequestParam String from, @RequestParam String to, Pageable pageable) {
        return itemTestingService.getItemTestingPaged(from, to, pageable);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public PostResponse create(@Valid @RequestBody ItemTesting itemTesting, BindingResult bindingResult) {
        return itemTestingService.create(itemTesting, bindingResult, messageSource);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public PostResponse update(@Valid @RequestBody ItemTesting itemTesting, BindingResult bindingResult, HttpServletRequest request) {
        return itemTestingService.update(itemTesting, bindingResult, messageSource);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public PostResponse delete(@PathVariable Integer id) {
        return itemTestingService.delete(id);
    }

    @RequestMapping(value = "/export/{id}")
    public void exportToPdf(@PathVariable Integer id,
                            @RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response, HttpServletRequest request) {
        HashMap params = printableVoucher.reportParameters(id, request);
        JRDataSource dataSource = printableVoucher.datasource(id);
        String template = GlobalConstant.JASPER_BASE_PATH + "/inventory/ItemTestingAcknowledgement.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
