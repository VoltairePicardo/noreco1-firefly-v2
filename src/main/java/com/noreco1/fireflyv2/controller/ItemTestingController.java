package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.ItemTestingDto;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.ItemTesting;
import com.noreco1.fireflyv2.repo.ItemTestingDetailRepo;
import com.noreco1.fireflyv2.service.ItemTestingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/item-testing")
public class ItemTestingController {

    private final ItemTestingService itemTestingService;
    private final MessageSource messageSource;
    private final ItemTestingDetailRepo itemTestingDetailRepo;

    public ItemTestingController(ItemTestingService itemTestingService, MessageSource messageSource, ItemTestingDetailRepo itemTestingDetailRepo) {
        this.itemTestingService = itemTestingService;
        this.messageSource = messageSource;
        this.itemTestingDetailRepo = itemTestingDetailRepo;
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

}
