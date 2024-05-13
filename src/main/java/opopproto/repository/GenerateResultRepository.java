package opopproto.repository;

import opopproto.model.GenerateResult;
import opopproto.model.Head;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenerateResultRepository extends JpaRepository<GenerateResult, Long> {
    List<GenerateResult> findAllByHead(Head head);
}
