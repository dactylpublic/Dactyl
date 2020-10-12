package me.fluffy.dactyl.injection.core;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class DactylAccessTransformer extends AccessTransformer {

    public DactylAccessTransformer() throws IOException {
        super("dactyl_at.cfg");
    }
}
