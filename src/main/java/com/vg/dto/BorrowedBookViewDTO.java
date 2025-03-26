package com.vg.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BorrowedBookViewDTO(UUID bookId, String title, String author, LocalDateTime borrowedAt) {}
