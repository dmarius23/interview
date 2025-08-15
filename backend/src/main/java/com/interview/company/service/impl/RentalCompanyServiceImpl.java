package com.interview.company.service.impl;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.repo.BookingRepository;
import com.interview.common.annotation.Loggable;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalCompany;
import com.interview.company.dto.RentalCompanyCreateDto;
import com.interview.company.dto.RentalCompanyResponseDto;
import com.interview.company.dto.RentalCompanyUpdateDto;
import com.interview.company.mapper.RentalCompanyMapper;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.service.RentalCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Service
@Loggable(logParams = true, logResult = false)
@RequiredArgsConstructor
public class RentalCompanyServiceImpl implements RentalCompanyService {

    private final RentalCompanyRepository companyRepo;
    private final BookingRepository bookingRepo;
    private final RentalCompanyMapper rentalCompanyMapper;

    @Transactional
    public RentalCompanyResponseDto addCompany(RentalCompanyCreateDto createDto) {
        RentalCompany company = rentalCompanyMapper.toEntity(createDto);
        RentalCompany saved = companyRepo.save(company);
        return rentalCompanyMapper.toResponse(saved);
    }

    @Transactional
    public RentalCompanyResponseDto updateCompany(Long companyId, RentalCompanyUpdateDto updateDto) {

        int attempts = 0, max = 3;
        while (true) {
            RentalCompany company = companyRepo.findById(companyId)
                    .orElseThrow(() -> new EntityNotFound("Company not found: " + companyId));

            rentalCompanyMapper.updateEntityFromDto(updateDto, company);

            try {
                RentalCompany saved = companyRepo.saveAndFlush(company); // bumps @Version
                return rentalCompanyMapper.toResponse(saved);
            } catch (OptimisticLockingFailureException ex) {
                if (++attempts >= max) throw ex;
                try {
                    Thread.sleep(25L * attempts);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Transactional
    public void deleteCompanySoft(Long companyId) {
        RentalCompany company = companyRepo.findById(companyId)
                .orElseThrow(() -> new EntityNotFound("Company not found: " + companyId));

        Set<BookingStatus> activeStatuses = BookingStatus.activeSet();
        Instant now = Instant.now();

        // 1) Block if ongoing bookings exist
        if (bookingRepo.hasOngoingForCompany(companyId, now, activeStatuses)) {
            throw new BusinessRuleViolation("Cannot delete company: ongoing bookings exist.");
        }

        // 2) Cancel future bookings and release cars
        List<Booking> future = bookingRepo.findFutureActiveForCompany(companyId, now, activeStatuses);
        for (Booking booking : future) {
            booking.setStatus(BookingStatus.CANCELED);
            //booking.getCar().setStatus(CarStatus.AVAILABLE);
        }

        // 3) Soft delete company: cascades to locations & cars via @SQLDelete
        company.setDeleted(true);
        company.setDeletedAt(now);
        companyRepo.delete(company); // triggers soft-delete SQL; cascades to locations and cars
    }

    @Transactional(readOnly = true)
    public PageResponse<RentalCompanyResponseDto> listCompanies(Pageable pageable) {
        Page<RentalCompany> page = companyRepo.findAll(pageable);
        return PageResponse.from(page.map(rentalCompanyMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public RentalCompanyResponseDto getCompany(Long id) {
        RentalCompany company = companyRepo.findById(id)
                .orElseThrow(() -> new EntityNotFound("Company not found: " + id));
        return rentalCompanyMapper.toResponse(company);
    }

    // Legacy method kept for backward compatibility if needed elsewhere
    @Deprecated
    @Transactional
    public RentalCompany updateCompany(Long companyId, Consumer<RentalCompany> mutator) {
        int attempts = 0, max = 3;
        while (true) {
            RentalCompany company = companyRepo.findById(companyId)
                    .orElseThrow(() -> new EntityNotFound("Company not found: " + companyId));
            mutator.accept(company);
            try {
                return companyRepo.saveAndFlush(company); // bumps @Version
            } catch (OptimisticLockingFailureException ex) {
                if (++attempts >= max) throw ex;
                try {
                    Thread.sleep(25L * attempts);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}