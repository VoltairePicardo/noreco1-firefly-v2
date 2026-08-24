package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.ItemTestingDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.ItemTesting;
import com.noreco1.fireflyv2.model.PoDetail;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.ItemTestingService;
import com.noreco1.fireflyv2.service.PrintableVoucher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/item-testing")
public class ItemTestingController {

    @Autowired
    @Qualifier("itemTestingServiceImpl")
    private ItemTestingService itemTestingService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    @Qualifier("itemTestingServiceImpl")
    private PrintableVoucher printableVoucher;

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/list")
    public List<ItemTesting> list() {
        Page<ItemTesting> page = itemTestingService.findAll("2000-01-01",
                GlobalConstant.YYYY_DATE_FORMAT.format(new Date()) + "-12-31",
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/list/{from}/{to}")
    public List<ItemTesting> listByDateRange(@PathVariable String from, @PathVariable String to) {
        Page<ItemTesting> page = itemTestingService.findAll(from, to,
                PageRequest.of(0, 1000, Sort.by("date").descending()));
        return page.getContent();
    }

    @GetMapping("/inventory-locations")
    public List<InventoryLocation> inventoryLocations() {
        return itemTestingService.getInventoryLocations();
    }

    @GetMapping("/po-details/{poId}")
    public List<PoDetail> poDetails(@PathVariable Integer poId) {
        return itemTestingService.getPurchaseOrderDetailsForItemTesting(poId);
    }

    @GetMapping("/details/{id}")
    public List<Map> getDetails(@PathVariable Integer id) {
        return itemTestingService.getItemTestingDetails(id);
    }

    @GetMapping("/{id}")
    public ItemTestingDto getById(@PathVariable Integer id) {
        return itemTestingService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody ItemTesting itemTesting) {
        BindingResult br = new BeanPropertyBindingResult(itemTesting, "itemTesting");
        return itemTestingService.create(itemTesting, br, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody ItemTesting itemTesting) {
        BindingResult br = new BeanPropertyBindingResult(itemTesting, "itemTesting");
        return itemTestingService.update(itemTesting, br, messageSource);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Process not supported for Item Testing.");
        return response;
    }

    @PostMapping("/delete/{id}")
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
