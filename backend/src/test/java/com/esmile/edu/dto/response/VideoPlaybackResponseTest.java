package com.esmile.edu.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VideoPlaybackResponseTest {

    @Test
    void constructor_shouldSetAllFields() {
        var response = new VideoPlaybackResponse(
            "https://example.vod.com/video.mp4?t=abc&sign=xyz",
            3600,
            "https://example.vod.com/cover.jpg"
        );

        assertEquals("https://example.vod.com/video.mp4?t=abc&sign=xyz", response.playbackUrl());
        assertEquals(3600, response.duration());
        assertEquals("https://example.vod.com/cover.jpg", response.coverImage());
    }

    @Test
    void constructor_coverImageCanBeNull() {
        var response = new VideoPlaybackResponse(
            "https://example.vod.com/video.mp4",
            1800,
            null
        );

        assertNull(response.coverImage());
    }
}
