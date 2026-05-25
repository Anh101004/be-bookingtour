package com.bookingtour.invoice.mapper;

import com.bookingtour.invoice.dto.response.InvoiceResponse;
import com.bookingtour.invoice.entity.Invoice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {
    InvoiceResponse toResponse(Invoice invoice);
}