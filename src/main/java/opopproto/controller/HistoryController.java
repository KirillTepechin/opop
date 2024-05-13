package opopproto.controller;

import opopproto.model.Head;
import opopproto.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HistoryController {
    @Autowired
    private HistoryService historyService;
    @GetMapping("/documents")
    public String showAllGenerationResults(Model model, @AuthenticationPrincipal Head head) {
        model.addAttribute("genDtos", historyService.getAllGenerationsByHead(head));
        return "documents";
    }

    @GetMapping("/inspections")
    public String showAllInspectionResults(Model model, @AuthenticationPrincipal Head head) {
        model.addAttribute("insDtos", historyService.getAllInspectionsByHead(head));
        return "inspections";
    }
}
