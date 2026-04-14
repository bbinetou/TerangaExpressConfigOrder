package sn.edu.ept.order_service.repository;

import sn.edu.ept.order_service.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByOrderId(Long orderId);
    List<TrackingEvent> findByOrderIdOrderByTimestampDesc(Long orderId);
}
