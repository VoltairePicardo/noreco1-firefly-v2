package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaterialIssueRegister;
import com.noreco1.fireflyv2.model.MaterialIssueRegisterDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public interface MaterialIssueRegisterDetailRepo extends JpaRepository<MaterialIssueRegisterDetail, Integer> {

    List<MaterialIssueRegisterDetail> findByMaterialIssueRegisterId(Integer id);

}
