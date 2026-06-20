package com.taskforge.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {
}
