package com.neo.dto;

import lombok.Builder;

@Builder
public record PaginationRequest (

    int page,

    int size
){}