package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.repo.RouteRepo;
import com.noreco1.fireflyv2.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public class ControllerHelper {

    @Autowired
    static RouteRepo routeRepo;

    public static ModelAndView getModelAndView(String viewName, RoleService roleService, Integer userId, String route) {
        ModelAndView modelAndView = new ModelAndView("403");

        Boolean authorized = roleService.isRouteAuthorized(userId, route);
        if (authorized) {
            Map<String, String> pageComponents = roleService.findPageComponentByRoute(userId, route);

            if (pageComponents != null && pageComponents.size() > 0) {
                modelAndView.addAllObjects(pageComponents);
            }
            modelAndView.setViewName(viewName);
        }

        return modelAndView;
    }
}
