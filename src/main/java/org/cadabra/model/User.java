package org.cadabra.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    private String name;
    private String username;
    private String email;
    private String phone;
    private String website;

    // Address fields (flattened)
    private String street;
    private String suite;
    private String city;
    private String zipcode;
    private String lat;
    private String lng;

    // Company fields (flattened)
    private String companyName;
    private String companyCatchPhrase;
    private String companyBs;
}

