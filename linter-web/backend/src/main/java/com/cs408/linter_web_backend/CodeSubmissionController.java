package com.cs408.linter_web_backend;

import core.Violation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code-submission")
public class CodeSubmissionController {

  private final codeAnalysis codeAnalysis;

  public CodeSubmissionController(codeAnalysis codeAnalysis) {
    this.codeAnalysis = codeAnalysis;
  }

  @PostMapping("/submit")
  public ResponseEntity<List<Violation>> submitCode(@RequestBody String sourceCode) {
    // Placeholder for code submission logic
    List<Violation> violations = codeAnalysis.sendAnalysisRequest(sourceCode);

    return ResponseEntity.ok(violations);
  }
}
