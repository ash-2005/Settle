package com.settle.groups;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, UUID> {}
