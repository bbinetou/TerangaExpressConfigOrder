package sn.edu.ept.order_service.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectedUser {

    private Long   id;
    private String email;
    private String role;

    public boolean isAdmin()  { return "ADMIN".equals(role); }

}
