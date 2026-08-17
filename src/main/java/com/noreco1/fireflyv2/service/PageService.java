package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Page;
import com.noreco1.fireflyv2.controller.response.PageComponentDto;

import java.util.List;

public interface PageService {
    public List<Page> findAllWithComponents();
    public List<Page> findAllAssigned(Integer roleId);
    public List<PageComponentDto> getPageComponents(Integer pageId);
    public List<PageComponentDto> getPageComponents(Integer roleId, Integer pageId);
}
