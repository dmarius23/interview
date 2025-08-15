package com.interview.company.service.impl;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.repo.BookingRepository;
import com.interview.common.annotation.Loggable;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import com.interview.company.dto.RentalLocationCreateDto;
import com.interview.company.dto.RentalLocationResponseDto;
import com.interview.company.dto.RentalLocationUpdateDto;
import com.interview.company.mapper.RentalLocationMapper;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.repo.RentalLocationRepository;
import com.interview.company.service.RentalLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Loggable(logParams = true, logResult = false)
@RequiredArgsConstructor
public class RentalLocationServiceImpl implements RentalLocationService {

    private final RentalCompanyRepository companyRepository;
    private final RentalLocationRepository rentalLocationRepository;
    private final BookingRepository bookingRepository;
    private final RentalLocationMapper rentalLocationMapper;

    @Override
    @Transactional
    public RentalLocationResponseDto addRentalLocation(RentalLocationCreateDto createDto) {
        RentalLocation rentalLocation = rentalLocationMapper.toEntity(createDto);
        RentalCompany companyRef = companyRepository.findById(createDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFound("Company not found: " + createDto.getCompanyId()));

        rentalLocation.setCompany(companyRef);
        RentalLocation saved = rentalLocationRepository.save(rentalLocation);
        return rentalLocationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RentalLocationResponseDto updateRentalLocation(Long rentalLocationId, RentalLocationUpdateDto updateDto) {
        int attempts = 0, max = 3;
        while (true) {
            RentalLocation rentalLocation = rentalLocationRepository.findById(rentalLocationId)
                    .orElseThrow(() -> new EntityNotFound("Location not found: " + rentalLocationId));

            rentalLocationMapper.updateEntityFromDto(updateDto, rentalLocation);

            try {
                RentalLocation saved = rentalLocationRepository.saveAndFlush(rentalLocation);
                return rentalLocationMapper.toResponse(saved);
            } catch (OptimisticLockingFailureException ex) {
                if (++attempts >= max) throw ex;
                try {
                    Thread.sleep(25L * attempts);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteRentalLocationSoft(Long rentalLocationId) {
        RentalLocation location = rentalLocationRepository.findById(rentalLocationId)
                .orElseThrow(() -> new EntityNotFound("Location not found: " + rentalLocationId));

        Instant now = Instant.now();
        Set<BookingStatus> activeStatuses = BookingStatus.activeSet();

        // 1) Block if ongoing bookings exist
        if (bookingRepository.hasOngoingForLocation(rentalLocationId, now, activeStatuses)) {
            throw new BusinessRuleViolation("Cannot delete location: ongoing bookings exist.");
        }

        // 2) Cancel future bookings referencing this location and release cars
        List<Booking> futureBookings = bookingRepository.findFutureActiveForLocation(rentalLocationId, now, activeStatuses);
        for (Booking booking : futureBookings) {
            booking.setStatus(BookingStatus.CANCELED);
            //booking.getCar().setStatus(CarStatus.AVAILABLE);
        }

        // 3) Soft delete location
        location.setDeleted(true);
        location.setDeletedAt(now);
        rentalLocationRepository.delete(location); // triggers soft-delete SQL
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<RentalLocationResponseDto> listLocationsByCity(String city, Pageable pageable) {
        Page<RentalLocation> page = rentalLocationRepository.findByCityIgnoreCase(city, pageable);
        return PageResponse.from(page.map(rentalLocationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<RentalLocationResponseDto> listLocationsByCompanyId(Long companyId, Pageable pageable) {
        Page<RentalLocation> page = rentalLocationRepository.findByCompanyId(companyId, pageable);
        return PageResponse.from(page.map(rentalLocationMapper::toResponse));
    }

    @Transactional(readOnly = true)
    @Override
    public RentalLocationResponseDto getById(Long id) {
        RentalLocation location = rentalLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFound("Rental location not found: " + id));
        return rentalLocationMapper.toResponse(location);
    }

    @Transactional(readOnly = true)
    public RentalLocation getEntityById(Long id) {
        return rentalLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFound("RentalLocation not found: " + id));
    }
}