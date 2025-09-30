package org.example.service;

public interface BranchService {
    void createBranch(String branchName) throws Exception;
    void checkoutBranch(String branchName) throws Exception;
    void mergeBranch(String branchName) throws Exception;
}
