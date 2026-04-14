package sn.edu.ept.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String orderIds;
    
    @Column(nullable = false)
    private String departureCity;
    
    @Column(nullable = false)
    private String arrivalCity;
    
    @Column(nullable = false)
    private Double estimatedDistanceKm;
    
    @Column(nullable = false)
    private String status;
}
