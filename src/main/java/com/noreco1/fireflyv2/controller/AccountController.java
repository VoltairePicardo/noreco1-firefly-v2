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
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounting/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountRepo accountRepo;
    private final DownloadService downloadService;
    private final JasperDatasourceService jasperDatasourceService;
    private final MessageSource messageSource;

    public AccountController(AccountService accountService, AccountRepo accountRepo, DownloadService downloadService, JasperDatasourceService jasperDatasourceService, MessageSource messageSource) {
        this.accountService = accountService;
        this.accountRepo = accountRepo;
        this.downloadService = downloadService;
        this.jasperDatasourceService = jasperDatasourceService;
        this.messageSource = messageSource;
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
            m.put("hasSL",        a.getHasSL() == 1);
            return m;
        });
    }

    @GetMapping("/supplier-account")
    public Map supplierAccount() {
        return accountService.findSupplierAccount();
    }

    @GetMapping("/vat")
    public Map vatAccount() {
        return accountService.findVat();
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
    public PostResponse create(@Valid @RequestBody Account account, BindingResult bindingResult) {
        return accountService.processCreate(account, bindingResult, messageSource);
    }

    @PostMapping("/update")
    public PostResponse update(@Valid @RequestBody Account account, BindingResult bindingResult) {
        return accountService.processUpdate(account, bindingResult, messageSource);
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
