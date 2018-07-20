package com.atwork.centralrn.model;

import java.util.List;

public class ResultsEntity {
    private List<Result> results;

    public ResultsEntity() {}

    public void setResults(List<Result> results) { this.results = results; }
    public List<Result> getResults() { return this.results; }
}