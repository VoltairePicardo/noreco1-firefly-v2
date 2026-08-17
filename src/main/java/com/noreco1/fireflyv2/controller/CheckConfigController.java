package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.CheckConfig;
import com.noreco1.fireflyv2.service.CheckConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/check-config")
public class CheckConfigController {

    @Autowired
    private CheckConfigService checkConfigService;

    @Autowired
    MessageSource messageSource;

    @GetMapping("/list")
    public List<CheckConfig> list() {
        return checkConfigService.findAll();
    }

    @GetMapping("/{id}")
    public CheckConfig getById(@PathVariable Integer id) {
        return checkConfigService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody CheckConfig config) {
        BindingResult bindingResult = new BeanPropertyBindingResult(config, "config");
        return checkConfigService.processCreate(config, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody CheckConfig config) {
        BindingResult bindingResult = new BeanPropertyBindingResult(config, "config");
        return checkConfigService.processUpdate(config, bindingResult, messageSource);
    }

    @GetMapping(value = "/{id}/test-print", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> testPrint(@PathVariable Integer id) {
        CheckConfig c = checkConfigService.findById(id);
        if (c == null) return ResponseEntity.notFound().build();

        String prefix     = c.getCheckNoPrefix() != null ? c.getCheckNoPrefix() : "";
        String dateFormat = c.getDateFormat()    != null ? c.getDateFormat()    : "MM/dd/yyyy";

        String checkNoStyle      = pos(c.getCheckNoX(),      c.getCheckNoY(),      null,           null);
        String dateStyle         = pos(c.getDateX(),         c.getDateY(),         null,           null);
        String payeeStyle        = pos(c.getPayeeX(),        c.getPayeeY(),        c.getPayeeW(),  null);
        String numericAmtStyle   = pos(c.getNumericAmountX(),c.getNumericAmountY(),null,           null);
        String alphaAmtStyle     = pos(c.getAlphaAmountX(),  c.getAlphaAmountY(),  c.getAlphaAmountW(), null);
        String sig1Style         = pos(c.getSig1X(),         c.getSig1Y(),         null,           null);
        String sig2Style         = pos(c.getSig2X(),         c.getSig2Y(),         null,           null);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
            .append("<title>Test Print — ").append(c.getCode()).append("</title>")
            .append("<style>")
            .append("body{margin:0;padding:0;font-family:Arial,sans-serif;font-size:12px;}")
            .append(".check{position:relative;width:816px;height:340px;border:1px dashed #ccc;background:#fffef8;margin:20px auto;}")
            .append(".check span{position:absolute;white-space:nowrap;}")
            .append(".label{font-size:10px;color:#999;position:fixed;top:5px;left:10px;}")
            .append("</style></head><body>")
            .append("<div class='label'>Test Print &mdash; Check Config: <strong>").append(c.getCode()).append("</strong></div>")
            .append("<div class='check'>")
            .append("<span style='").append(checkNoStyle).append("'>").append(prefix).append("0001234</span>")
            .append("<span style='").append(dateStyle).append("'>").append(dateFormat.replace("yyyy","2025").replace("MM","01").replace("dd","15").replace("YYYY","2025").replace("DD","15")).append("</span>")
            .append("<span style='").append(payeeStyle).append("'>SAMPLE PAYEE NAME</span>")
            .append("<span style='").append(numericAmtStyle).append("'>12,345.67</span>")
            .append("<span style='").append(alphaAmtStyle).append("'>TWELVE THOUSAND THREE HUNDRED FORTY FIVE AND 67/100</span>");

        if (Boolean.TRUE.equals(c.getWithSigner())) {
            html.append("<span style='").append(sig1Style).append("'>Authorized Signatory 1</span>")
                .append("<span style='").append(sig2Style).append("'>Authorized Signatory 2</span>");
        }

        html.append("</div></body></html>");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html.toString());
    }

    private String pos(Integer x, Integer y, Integer w, Integer lineH) {
        StringBuilder s = new StringBuilder();
        if (x    != null) s.append("margin-left:").append(x).append("px;");
        if (y    != null) s.append("margin-top:").append(y).append("px;");
        if (w    != null) s.append("width:").append(w).append("px;overflow:hidden;");
        if (lineH!= null) s.append("line-height:").append(lineH).append("px;");
        return s.toString();
    }
}
