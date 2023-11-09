package org.vanillamodifier.plugin;


import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

public class ModAPI {

    private static ModLoader loader;

    private ModAPI(ModLoader loader) {
        ModAPI.loader = loader;
    }

    public static ModAPI getApi(Consumer<ModLoader> consumer) {
        consumer.accept(loader = new ModLoader());
        return new ModAPI(loader);
    }

    public ModLoader getLoader() {
        return loader;
    }

    public void loadAll(File folder) {
        if (!folder.exists())
            folder.mkdir();

        for (File fileIndex : Objects.requireNonNull(folder.listFiles())) {
            if (fileIndex.getName().endsWith(".jar")) {
                loader.load(fileIndex);
            }
        }
    }

    public void unloadAll(File folder) {
        for (File fileIndex : Objects.requireNonNull(folder.listFiles())) {
            loader.unload(fileIndex);
        }
    }


}
