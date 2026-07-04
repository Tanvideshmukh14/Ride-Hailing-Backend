package com.demo.ridebackend.dto.response;

import com.demo.ridebackend.enums.RideStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideResponse {

    private Long rideId;

    private String riderName;

    private String driverName;

    private RideStatus status;

    private Double fare;

}