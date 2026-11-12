package com.cs408.linter_web_backend;

import core.LinterEngine;
import core.Violation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class codeAnalysis {
    private final LinterEngine linterEngine;

    public codeAnalysis() {
        this.linterEngine = new LinterEngine();
    }

    public List<Violation> sendAnalysisRequest(String sourceCode) {
        return linterEngine.analyse(sourceCode);
    }

}
