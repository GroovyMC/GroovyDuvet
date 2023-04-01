/*
 * Copyright (C) 2022-2023 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.core.impl.mappings

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.stc.POJO
import io.github.lukebemish.groovyduvet.core.impl.compile.ClassMappings
import net.fabricmc.api.EnvType
import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.format.ProGuardReader
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.zip.GZIPInputStream

@CompileStatic
@POJO
class MetaclassMappingsProvider implements PreLaunchEntrypoint {
    private static final String OFFICIAL_NAMESPACE = 'official'
    private static final String PISTON_META = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
    private static final Path CACHE_DIR = QuiltLoader.gameDir.resolve('mod_data/groovyduvet')
    private static final String MC_VERSION = QuiltLoader.getRawGameVersion()
    private static final Path OFFICIAL_FILE = CACHE_DIR.resolve("official_${MC_VERSION}.txt")
    private static final Path VERSION_FILE = CACHE_DIR.resolve("version_${MC_VERSION}.json")
    private static final JsonSlurper JSON_SLURPER = new JsonSlurper()
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaclassMappingsProvider)

    @Lazy
    private static volatile HttpClient client = HttpClient.newBuilder().build()

    @Override
    void onPreLaunch(ModContainer mod) {
        if (!QuiltLoader.developmentEnvironment) {
            setup()
        }
    }

    static setup() {
        LOGGER.info('Starting runtime mappings setup...')
        if (Files.exists(VERSION_FILE)) {
            LOGGER.info('Found cached version file; attempting to load mappings...')
            try {
                loadLayeredMappings()
                LOGGER.info('Finished runtime mappings setup.')
                return // success
            } catch (Exception e) {
                LOGGER.warn('Failed to load cached version file; attempting to download mappings...', e)
            }
        }
        try (InputStream manifestInput = downloadFile(PISTON_META)) {
            Map manifestMeta = (Map) JSON_SLURPER.parse(manifestInput)
            try {
                Map versionMeta = manifestMeta.versions.find { (it as Map)['id'] == MC_VERSION } as Map
                if (!Files.exists(CACHE_DIR)) Files.createDirectories(CACHE_DIR)
                LOGGER.info('Found version metadata from piston-meta.')
                checkAndUpdateVersionFile(versionMeta)
                LOGGER.info('version.json is up to date.')
                checkAndUpdateOfficialFile()
                LOGGER.info('Official mappings are up to date.')
                loadLayeredMappings()
                LOGGER.info('Finished runtime mappings setup.')
            } catch (IOException e) {
                // Error state, I couldn't make mappings.
                throw e
            } catch (NoSuchElementException e) {
                // Error state, not a known version? Huh?
                throw e
            }
        } catch (IOException e) {
            LOGGER.error('Couldn\'t connect to piston-meta and cached mappings either do not exist or have the wrong checksum.')
            throw new RuntimeException(e)
        }
    }

    private static void checkAndUpdateVersionFile(Map versionMeta) throws IOException {
        String sha1 = versionMeta.sha1

        if (Files.exists(VERSION_FILE)) {
            byte[] existingSha1 = calcSha1(VERSION_FILE)
            try {
                byte[] knownSha1 = decodeHex(sha1)
                if (Arrays.equals(knownSha1, existingSha1)) return
            } catch (Exception ignored) {}
        }

        try (InputStream versionStream = downloadFile(versionMeta.url as String)) {
            Files.copy(versionStream, VERSION_FILE, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private static byte[] decodeHex(String original) {
        char[] chars = original.chars
        byte[] out = new byte[chars.length >> 1]

        final int len = chars.length

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Cannot decode odd number of characters!")
        }

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int digit = Character.digit(chars[j], 16)
            if (digit == -1)
                throw new RuntimeException("Illegal non-hex character!")
            int f = digit << 4
            j++
            digit = Character.digit(chars[j], 16)
            if (digit == -1)
                throw new RuntimeException("Illegal non-hex character!")
            f = f | digit
            j++
            out[i] = (byte) (f & 0xFF)
        }

        return out
    }

    private static void checkAndUpdateOfficialFile() throws IOException {
        Map versionMeta = (Map) JSON_SLURPER.parse(VERSION_FILE)
        String sha1 = switch (MinecraftQuiltLoader.environmentType) {
            case EnvType.CLIENT -> ((versionMeta.downloads as Map).client_mappings as Map).sha1
            case EnvType.SERVER -> ((versionMeta.downloads as Map).server_mappings as Map).sha1
        }
        String url = switch (MinecraftQuiltLoader.environmentType) {
            case EnvType.CLIENT -> ((versionMeta.downloads as Map).client_mappings as Map).url
            case EnvType.SERVER -> ((versionMeta.downloads as Map).server_mappings as Map).url
        }

        if (Files.exists(OFFICIAL_FILE)) {
            byte[] existingSha1 = calcSha1(OFFICIAL_FILE)
            try {
                byte[] knownSha1 = decodeHex(sha1)
                if (Arrays.equals(knownSha1, existingSha1)) return
            } catch (Exception ignored) {}
        }

        try (InputStream officialStream = downloadFile(url)) {
            Files.copy(officialStream, OFFICIAL_FILE, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private static byte[] calcSha1(Path file) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1")
        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192]
            int len = input.read(buffer)

            while (len != -1) {
                sha1.update(buffer, 0, len)
                len = input.read(buffer)
            }

            return sha1.digest()
        }
    }

    private static void loadLayeredMappings() throws IOException {
        final mapper = new ClassmapVisitor()
        ProGuardReader.read(Files.newBufferedReader(OFFICIAL_FILE), mapper)
        final visitor = new LoadingVisitor(mapper.mojToObf)
        ProGuardReader.read(Files.newBufferedReader(OFFICIAL_FILE), visitor)
        MappingMetaClassCreationHandle.applyCreationHandle(visitor.build())
    }

    /**
     * Downloads a file from the given URI, with GZip if the server supports it.
     * @param url
     * @return InputStream
     * @throws IOException
     * @throws InterruptedException
     */
    private static InputStream downloadFile(final String url) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder(new URI(url))
                .setHeader('Accept-Encoding', 'gzip')
                .GET()
                .build()
        final HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() !== 200)
            throw new IOException("Failed to download file from \"$url\" (${response.statusCode()})")

        final boolean isGzipEncoded = response.headers().firstValue('Content-Encoding').orElse('') == 'gzip'
        return isGzipEncoded ? new GZIPInputStream(response.body()) : response.body()
    }

    private static class LoadingVisitor implements MappingVisitor {
        // runtime class name (with dots) to map of moj -> runtime names
        final Map<String, Map<String, List<String>>> methods = [:]
        final Map<String, Map<String, String>> fields = [:]
        // moj class name to runtime class name
        final Map<String, String> classes = [:]

        String lastClassObf
        String lastClassMoj
        String lastMethodMoj
        String lastMethodDesc
        String lastFieldMoj
        String lastFieldDesc

        final BiMap<String, String> mojToObf

        LoadingVisitor(BiMap<String, String> mojToObj) {
            this.mojToObf = mojToObj
        }

        @Override
        void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {

        }

        @Override
        boolean visitClass(String srcName) throws IOException {
            lastClassMoj = srcName.replace('/','.')
            return true
        }

        @Override
        boolean visitField(String srcName, String srcDesc) throws IOException {
            lastFieldMoj = srcName
            lastFieldDesc = srcDesc
            return true
        }

        @Override
        boolean visitMethod(String srcName, String srcDesc) throws IOException {
            lastMethodMoj = srcName
            lastMethodDesc = srcDesc
            return true
        }

        @Override
        boolean visitMethodArg(int argPosition, int lvIndex, String srcName) throws IOException {
            return false
        }

        @Override
        boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, String srcName) throws IOException {
            return false
        }

        @Override
        void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
            switch (targetKind) {
                case MappedElementKind.CLASS:
                    lastClassObf = name.replace('/','.')
                    classes.put(lastClassMoj, getRuntimeClassName())
                    break
                case MappedElementKind.METHOD:
                    methods.computeIfAbsent(getRuntimeClassName(), {[:]})
                            .computeIfAbsent(lastMethodMoj, {[]}).add(getRuntimeMethodName(name))
                    break
                case MappedElementKind.FIELD:
                    fields.computeIfAbsent(getRuntimeClassName(), {[:]})
                            .put(lastFieldMoj, getRuntimeFieldName(name))
                    break
                default:
                    break
            }
        }

        @Override
        void visitComment(MappedElementKind targetKind, String comment) throws IOException {

        }

        String getRuntimeClassName() {
            QuiltLoader.mappingResolver.mapClassName(OFFICIAL_NAMESPACE, lastClassObf)
        }

        String getRuntimeMethodName(String obf) {
            QuiltLoader.mappingResolver.mapMethodName(OFFICIAL_NAMESPACE, lastClassObf, obf, descMojToObf(lastMethodDesc))
        }

        String getRuntimeFieldName(String obf) {
            QuiltLoader.mappingResolver.mapFieldName(OFFICIAL_NAMESPACE, lastClassObf, obf, descMojToObf(lastFieldDesc))
        }

        String descMojToObf(String moj) {
            moj.replaceAll(/L(.*?);/,{ full, inner ->
                "L${mojToObf[inner]?:inner};"
            })
        }

        String descMojToRuntime(String moj) {
            descMojToObf(moj).replaceAll(/L(.*?);/, { full, inner ->
                "L${QuiltLoader.mappingResolver.mapClassName(OFFICIAL_NAMESPACE, (inner as String).replace('/','.')).replace('.','/')};"
            })
        }

        LoadedMappings build() {
            ClassMappings.addMappings(classes, methods, fields)
            return new LoadedMappings(methods, fields)
        }
    }

    private static class ClassmapVisitor implements MappingVisitor {
        BiMap<String, String> mojToObf = HashBiMap.create()

        String lastClassMoj

        @Override
        void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {

        }

        @Override
        boolean visitClass(String srcName) throws IOException {
            return lastClassMoj = srcName
        }

        @Override
        boolean visitField(String srcName, String srcDesc) throws IOException {
            return false
        }

        @Override
        boolean visitMethod(String srcName, String srcDesc) throws IOException {
            return false
        }

        @Override
        boolean visitMethodArg(int argPosition, int lvIndex, String srcName) throws IOException {
            return false
        }

        @Override
        boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, String srcName) throws IOException {
            return false
        }

        @Override
        void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
            if (targetKind == MappedElementKind.CLASS)
                mojToObf[lastClassMoj] = name
        }

        @Override
        void visitComment(MappedElementKind targetKind, String comment) throws IOException {

        }
    }
}
