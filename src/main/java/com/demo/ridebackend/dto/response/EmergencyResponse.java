package com.demo.ridebackend.dto.response;
import com.demo.ridebackend.enums.EmergencyStatus;
import com.demo.ridebackend.enums.RideStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyResponse {

    private Long emergencyId;

    private Long rideId;

    private RideStatus rideStatus;

    private String riderName;

    private String riderPhone;

    private String driverName;

    private String driverPhone;

    private Double latitude;

    private Double longitude;

    private LocalDateTime timestamp;

    private EmergencyStatus status;
}