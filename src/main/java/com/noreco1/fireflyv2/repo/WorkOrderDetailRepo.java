package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.WorkOrder;
import com.noreco1.fireflyv2.model.WorkOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderDetailRepo extends JpaRepository<WorkOrderDetail, Integer> {
    List<WorkOrderDetail> findByWorkOrderId(Integer workOrderId);
}
