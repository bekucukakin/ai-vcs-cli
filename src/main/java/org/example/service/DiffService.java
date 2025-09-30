package org.example.service;

import java.util.List;

public interface DiffService {
    List<String> getLineDiff(List<String> oldLines, List<String> newLines);
    void printUnifiedDiff(List<String> oldLines, List<String> newLines);
}
