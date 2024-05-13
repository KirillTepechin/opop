package opopproto.service;

import opopproto.model.GenerateResult;
import opopproto.model.Head;
import opopproto.model.InspectionResult;
import opopproto.repository.GenerateResultRepository;
import opopproto.repository.InspectionResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {
    @Autowired
    private GenerateResultRepository generateResultRepository;
    @Autowired
    private InspectionResultRepository inspectionResultRepository;

    public List<GenerateResult> getAllGenerationsByHead(Head head){
        return generateResultRepository.findAllByHead(head);
    }

    public List<InspectionResult> getAllInspectionsByHead(Head head){
        Pageable pageable = PageRequest.of(0, 10);
        return inspectionResultRepository.findAllByHeadOrderByCreationDateDesc(head, pageable).getContent();
    }
}
