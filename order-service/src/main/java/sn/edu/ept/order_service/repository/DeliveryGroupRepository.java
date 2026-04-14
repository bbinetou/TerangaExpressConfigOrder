package sn.edu.ept.order_service.repository;

import sn.edu.ept.order_service.entity.DeliveryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryGroupRepository extends JpaRepository<DeliveryGroup, Long> {
}
