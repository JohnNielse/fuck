package com.shade.decima.killzone2.rtti;

import com.shade.decima.rtti.RTTICoreFile;

import java.nio.file.Files;
import java.nio.file.Path;

public class KZ2Testing {
    public static void main(String[] args) throws Exception {
        final Path elf = Path.of("D:/PlayStation Games/Killzone 2/reverse/EBOOT.ELF");
        final Path core = Path.of("D:/PlayStation Games/Killzone 2/dump/LocalCachePS3/lumps/assets_description.loading_loading_assets.core");

        final KZ2Factory rttiFactory = new KZ2Factory(elf);
        final KZ2Reader rttiReader = new KZ2Reader();
        final RTTICoreFile coreFile = rttiReader.read(rttiFactory, Files.readAllBytes(core));
    }
}
