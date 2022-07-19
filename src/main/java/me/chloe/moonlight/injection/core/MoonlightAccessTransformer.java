package me.chloe.moonlight.injection.core;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class MoonlightAccessTransformer extends AccessTransformer {

    public MoonlightAccessTransformer() throws IOException {
        super("moonlight_at.cfg");
    }
}
