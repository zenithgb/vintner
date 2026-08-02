package com.zenith.vintner.wine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WineFeastState(long expiresAt) {
    public static final WineFeastState EMPTY = new WineFeastState(0L);

    public static final Codec<WineFeastState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.LONG.fieldOf("expires_at")
                            .forGetter(WineFeastState::expiresAt)
            ).apply(instance, WineFeastState::new));

    public WineFeastState activeAt(long gameTime) {
        return isActiveAt(gameTime) ? this : EMPTY;
    }

    public boolean isActiveAt(long gameTime) {
        return expiresAt > gameTime;
    }
}
