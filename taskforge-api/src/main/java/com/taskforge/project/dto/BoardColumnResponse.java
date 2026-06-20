package com.taskforge.project.dto;

import com.taskforge.project.BoardColumn;

import java.util.UUID;

public record BoardColumnResponse(
        UUID id,
        String name,
        int position,
        String color
) {
    public static BoardColumnResponse from(BoardColumn column) {
        return new BoardColumnResponse(
                column.getId(),
                column.getName(),
                column.getPosition(),
                column.getColor()
        );
    }
}
