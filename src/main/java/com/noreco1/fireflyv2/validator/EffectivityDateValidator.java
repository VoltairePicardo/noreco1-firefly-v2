package com.noreco1.fireflyv2.validator;

import com.noreco1.fireflyv2.model.DateRange;
import com.noreco1.fireflyv2.service.implementation.EffectivityDateServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.sql.Date;
import java.util.List;

@Component
public class EffectivityDateValidator implements Validator {

    private EffectivityDateServiceImpl effectivityDateService;

    @Override
    public boolean supports(Class<?> aClass) {
        return DateRange.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        DateRange effectivityDate = (DateRange) o;

        boolean hasNullDate = false;
        if (effectivityDate.getStart() == null) {
            errors.rejectValue("start", null, "Must select start date");
            hasNullDate = true;
        }

        if (effectivityDate.getEnd() == null) {
            errors.rejectValue("end", null, "Must select end date");
            hasNullDate = true;
        }

        if (!hasNullDate) {

            Date start = new Date(effectivityDate.getStart().getTime());
            Date end   = new Date(effectivityDate.getEnd().getTime());

            if (start.after(end) || start.compareTo(end) == 0) {
                errors.rejectValue("end", null, "End date must be later than start date");
                return;
            }

            Integer currentId = effectivityDate.getId() == null ? 0 : effectivityDate.getId();

            List<DateRange> overlapping = this.effectivityDateService.findOverlapping(start, end);
            boolean hasConflict = overlapping.stream()
                    .anyMatch(r -> !r.getId().equals(currentId));

            if (hasConflict) {
                errors.rejectValue("end", null, "Date range overlaps with an existing effectivity date");
            }
        }
    }

    public void setService(EffectivityDateServiceImpl effectivityDateService) {
        this.effectivityDateService = effectivityDateService;
    }
}
