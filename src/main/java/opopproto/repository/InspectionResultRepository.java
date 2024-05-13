package opopproto.repository;

import opopproto.model.Head;
import opopproto.model.InspectionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionResultRepository extends JpaRepository<InspectionResult, Long> {
    Page<InspectionResult> findAllByHeadOrderByCreationDateDesc(Head head, Pageable pageable);
}
