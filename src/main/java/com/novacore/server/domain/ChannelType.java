package com.novacore.server.domain;

/**
 * Channel type for server channels. Mapped to the PostgreSQL enum channel_type
 * with values ('text', 'voice', 'stage', 'category').
 */
public enum ChannelType {
    text,
    voice,
    stage,
    category
}

