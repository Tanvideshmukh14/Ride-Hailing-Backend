package com.demo.ridebackend.entity;

import com.demo.ridebackend.enums.Role;
import com.demo.ridebackend.enums.Gender; // Import the new enum
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- Added for Gender Matching Support ---
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Double latitude;

    private Double longitude;

    @Builder.Default
    private Boolean available = true;

    // --- Added for Version 5: Feature of Preference ---
    private String vehicleType; // e.g., "SEDAN", "SUV"

    @OneToMany(mappedBy = "rider")
    private List<Ride> riderRides;

    @OneToMany(mappedBy = "driver")
    private List<Ride> driverRides;
}