package com.vg.dto;

import java.util.UUID;

public record BookResponseDTO(UUID id, String title, String author, boolean available) {}
