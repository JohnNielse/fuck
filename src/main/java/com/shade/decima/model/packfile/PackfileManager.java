package com.shade.decima.model.packfile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.shade.decima.model.rtti.objects.Language;
import com.shade.decima.model.util.Compressor;
import com.shade.decima.model.util.IOUtils;
import com.shade.decima.model.util.NotNull;
import com.shade.decima.model.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.shade.decima.model.packfile.PackfileBase.getNormalizedPath;
import static com.shade.decima.model.packfile.PackfileBase.getPathHash;

public class PackfileManager implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(PackfileManager.class);

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Language.class, (JsonDeserializer<Object>) (json, type, context) -> Language.values()[json.getAsInt()])
        .create();

    private final Compressor compressor;
    private final SortedSet<Packfile> packfiles;
    private final Map<String, PackfileInfo> packfilesInfo;

    public PackfileManager(@NotNull Compressor compressor, @Nullable Path packfileInfoPath) {
        Map<String, PackfileInfo> info = null;

        if (packfileInfoPath != null) {
            try (Reader reader = IOUtils.newCompressedReader(packfileInfoPath)) {
                info = GSON.fromJson(reader, new TypeToken<Map<String, PackfileInfo>>() {}.getType());
            } catch (IOException e) {
                log.warn("Can't load packfile name mappings", e);
            }
        }

        this.compressor = compressor;
        this.packfiles = new TreeSet<>();
        this.packfilesInfo = info;
    }

    public boolean mount(@NotNull Path packfile) throws IOException {
        log.info("Mounting {}", packfile);

        if (!Files.exists(packfile)) {
            log.info("Cannot mount {} because the file does not exist", packfile);
            return false;
        }

        String name = packfile.getFileName().toString();

        if (name.indexOf('.') >= 0) {
            name = name.substring(0, name.indexOf('.'));
        }

        final PackfileInfo info = packfilesInfo != null
            ? packfilesInfo.get(name)
            : null;

        packfiles.add(new Packfile(
            packfile,
            info,
            FileChannel.open(packfile, StandardOpenOption.READ),
            compressor
        ));

        return true;
    }

    @Nullable
    public Packfile findAny(@NotNull String path) {
        return findAny(getPathHash(getNormalizedPath(path)));
    }

    @Nullable
    public Packfile findAny(long hash) {
        return packfiles.stream()
            .filter(x -> x.getFileEntry(hash) != null)
            .findAny().orElse(null);
    }


    @NotNull
    public List<Packfile> findAll(@NotNull String path) {
        return findAll(getPathHash(getNormalizedPath(path)));
    }

    @NotNull
    public List<Packfile> findAll(long hash) {
        return packfiles.stream()
            .filter(x -> x.getFileEntry(hash) != null)
            .toList();
    }

    @NotNull
    public Collection<Packfile> getPackfiles() {
        return packfiles;
    }

    @Override
    public void close() throws IOException {
        for (Packfile packfile : packfiles) {
            packfile.close();
        }

        packfiles.clear();
    }

}
