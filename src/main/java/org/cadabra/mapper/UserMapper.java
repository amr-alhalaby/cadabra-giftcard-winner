package org.cadabra.mapper;

import org.cadabra.dto.UserApiResponse;
import org.cadabra.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.suite", target = "suite")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.zipcode", target = "zipcode")
    @Mapping(source = "address.geo.lat", target = "lat")
    @Mapping(source = "address.geo.lng", target = "lng")
    @Mapping(source = "company.name", target = "companyName")
    @Mapping(source = "company.catchPhrase", target = "companyCatchPhrase")
    @Mapping(source = "company.bs", target = "companyBs")
    User toUser(UserApiResponse dto);
}

