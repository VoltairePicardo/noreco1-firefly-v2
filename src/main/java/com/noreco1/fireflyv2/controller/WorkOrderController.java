package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.model.WorkOrder;
import com.noreco1.fireflyv2.model.WorkOrderDetail;
import com.noreco1.fireflyv2.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @GetMapping("/list")
    public Page<WorkOrder> list(
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return workOrderService.list(statusId, year, month, search, page, size);
    }

    @GetMapping("/{id}")
    public WorkOrder getById(@PathVariable Integer id) {
        return workOrderService.findById(id);
    }

    @GetMapping("/document-statuses")
    public List<Map<String, Object>> documentStatuses() {
        return List.of(
            Map.of("id", 0, "status", "Open"),
            Map.of("id", 1, "status", "Closed")
        );
    }

    @GetMapping("/{id}/posted-vouchers")
    public List<Map<String, Object>> postedVouchers(@PathVariable Integer id) {
        return workOrderService.getPostedVouchersMap(id);
    }

    @GetMapping("/{id}/detail")
    public WorkOrderDetail workOrderDetail(@PathVariable Integer id) {
        return workOrderService.getWorkOrderDetailSummary(id);
    }

    @GetMapping("/{id}/logs")
    public List<Map<String, Object>> logs(@PathVariable Integer id) {
        return workOrderService.getLogs(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Map<String, Object> payload) {
        return workOrderService.createFromPayload(payload);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Map<String, Object> payload) {
        return workOrderService.updateFromPayload(payload);
    }

    @PostMapping("/post")
    public PostResponse post(@RequestBody Map<String, Object> payload) {
        return workOrderService.postWorkOrder(payload);
    }

    @PostMapping("/process")
    public PostResponse process(@RequestBody ProcessDocumentDto dto) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Work Order processing is not supported in this version.");
        return response;
    }

    @PostMapping("/close-out")
    public PostResponse closeOut(@RequestBody Map<String, Object> payload) {
        PostResponse response = new PostResponse();
        response.setFailureMessage("Close-out functionality is handled by the legacy service.");
        return response;
    }
}
