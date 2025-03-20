package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestorId(Long requestorId, Sort sort);

    List<ItemRequest> findByRequestorIdNot(Long requestorId, Sort sort);
}