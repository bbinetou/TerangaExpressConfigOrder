package sn.edu.ept.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long clientId;
    
    @Column(nullable = false)
    private Long parcelId;
    
    private Long driverId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private String transportType;
    
    @Column(nullable = false)
    private Double totalPrice;
    
    @Column(nullable = false)
    private LocalDateTime scheduledAt;
    
    private LocalDateTime deliveredAt;
}
