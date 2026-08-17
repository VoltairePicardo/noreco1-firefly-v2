package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.controller.response.AccountDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.repo.AccountRepo;
import com.noreco1.fireflyv2.service.AccountService;
import com.noreco1.fireflyv2.service.DownloadService;
import com.noreco1.fireflyv2.service.JasperDatasourceService;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounting/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private JasperDatasourceService jasperDatasourceService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/search")
    public Page<Map<String, Object>> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return accountRepo.findByQuery(q, PageRequest.of(page, size)).map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",           a.getId());
            m.put("accountCode",  a.getCode());
            m.put("accountTitle", a.getTitle());
            m.put("accountType",  a.getAccountType() != null ? a.getAccountType().getDescription() : null);
            return m;
        });
    }

    @GetMapping("/supplier-account")
    public Map supplierAccount() {
        return accountService.findSupplierAccount();
    }

    @GetMapping("/list")
    public List<AccountDto> getList() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public AccountDto getById(@PathVariable Integer id) {
        return accountService.findById(id);
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Account account) {
        Account saved = accountService.create(account);
        PostResponse response = new PostResponse();
        response.setSuccess(saved != null && saved.getId() != null && saved.getId() > 0);
        response.setModelId(saved != null && saved.getId() != null ? saved.getId() : 0);
        return response;
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Account account) {
        Account saved = accountService.update(account);
        PostResponse response = new PostResponse();
        response.setSuccess(saved != null && saved.getId() != null && saved.getId() > 0);
        response.setModelId(saved != null && saved.getId() != null ? saved.getId() : 0);
        return response;
    }

    @PostMapping("/delete/{id}")
    public Account delete(@PathVariable Integer id) {
        return accountService.delete(id);
    }

    @GetMapping("/export")
    public void exportToPdf(@RequestParam(value = "type") String type,
                            @RequestParam(value = "token") String token,
                            HttpServletResponse response) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("COMP_NAME",    "NEGROS ORIENTAL I ELECTRIC COOPERATIVE, INC");
        params.put("COMP_ADDRESS", "Tinaogan, Bindoy, Negros Oriental, Philippines");
        params.put("SEGMENT",      "");
        params.put("LOGO_PATH",    null);
        JRDataSource dataSource = jasperDatasourceService.getCoaDataSource();
        String template = GlobalConstant.JASPER_BASE_PATH + "/coa/ChartOfAccounts.jrxml";
        downloadService.download(type, token, response, params, template, dataSource);
    }
}
