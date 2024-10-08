package com.easymap.easymap.dto.request.poi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceRequestDTO {

    private String address;

    @JsonProperty("lat")
    private Double latitude;

    @JsonProperty("lng")
    private Double longitude;
}
