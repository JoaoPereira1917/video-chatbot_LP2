package com.videochatbot.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GroqResponse(
        @JsonProperty("text") String text,
        @JsonProperty("segments") List<GroqSegment> segments
) {}
