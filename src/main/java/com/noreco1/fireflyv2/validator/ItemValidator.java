package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.model.Role;
import com.noreco1.fireflyv2.service.implementation.ItemServiceImpl;
import com.noreco1.fireflyv2.service.implementation.RoleServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    private ItemServiceImpl service;

    @Override
    public boolean supports(Class<?> aClass) {
        return Item.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Item item = (Item) o;

        Item i = this.service.findByDescription(item.getDescription());
        // insert mode
        if (item.getId() == null || item.getId() == 0) {
            if (i != null) {
                errors.rejectValue("description", "item.description.taken");
            }
        } else {
            // update role
            if (i != null && !i.getId().equals(item.getId())) { // diff item
                errors.rejectValue("description", "item.description.taken");
            }
        }
    }

    public void setService(ItemServiceImpl service) {
        this.service = service;
    }
}
