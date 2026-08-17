package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SiteInspectionReportDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteInspectionReportDescriptionRepo extends JpaRepository<SiteInspectionReportDescription, Integer> {
    Long deleteBySiteInspectionReportId(Integer id);
    List<SiteInspectionReportDescription> findBySiteInspectionReportId(Integer id);
}
