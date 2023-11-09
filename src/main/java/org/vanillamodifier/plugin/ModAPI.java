package org.vanillamodifier.plugin;


import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

public class ModAPI {

    private static ModLoader loader;
    private String outName;

    private ModAPI(ModLoader loader) {
        ModAPI.loader = loader;
    }

    public static ModAPI getApi(Consumer<ModLoader> consumer, String outName) {
        consumer.accept(loader = new ModLoader());
        ModAPI api = new ModAPI(loader);
        api.outName = outName;
        return api;
    }

    public ModLoader getLoader() {
        return loader;
    }

    public void loadAll(File folder) {
        try {
            if (!folder.exists())
                folder.mkdir();

            for (File fileIndex : Objects.requireNonNull(folder.listFiles())) {
                if (fileIndex.getName().endsWith(".jar")) {
                    loader.load(fileIndex);
                    System.out.println(outName + " " + "Loaded: " + fileIndex.getName());
                }
            }
        } catch (NullPointerException e) { // 通常是没有这个文件夹

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void unloadAll(File folder) {
        try {
            for (File fileIndex : Objects.requireNonNull(folder.listFiles())) {
                loader.unload(fileIndex);
                System.out.println(outName + " " + "Unloaded: " + fileIndex.getName());
            }
        }catch (NullPointerException e){e.printStackTrace();}
    }


}
