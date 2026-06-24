package com.videochatbot.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqSegment(
        @JsonProperty("start") double start,
        @JsonProperty("end") double end,
        @JsonProperty("text") String text
) {}
