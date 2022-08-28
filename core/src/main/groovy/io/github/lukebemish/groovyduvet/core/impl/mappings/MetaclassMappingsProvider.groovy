/*
 * Copyright (C) 2022 Luke Bemish
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */

package io.github.lukebemish.groovyduvet.core.impl.mappings

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import net.fabricmc.api.EnvType
import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.format.ProGuardReader
import org.apache.commons.codec.binary.Hex
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader
import org.quiltmc.loader.impl.QuiltLoaderImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@CompileStatic
class MetaclassMappingsProvider {
    private static final String OFFICIAL_NAMESPACE = 'official'
    private static final String RUNTIME_NAMESPACE = QuiltLoader.mappingResolver.currentRuntimeNamespace
    private static final String PISTON_META = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
    private static final Path CACHE_DIR = QuiltLoader.gameDir.resolve('mod_data/groovyduvet')
    private static final String MC_VERSION = QuiltLoaderImpl.INSTANCE.gameProvider.rawGameVersion
    private static final Path OFFICIAL_FILE = CACHE_DIR.resolve('official.txt')
    private static final Path VERSION_FILE = CACHE_DIR.resolve('version.json')
    private static final JsonSlurper JSON_SLURPER = new JsonSlurper()
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaclassMappingsProvider)

    static setup() {
        final URL url = new URL(PISTON_META)
        try {
            Map manifestMeta = (Map) JSON_SLURPER.parse(url)
            LOGGER.info("Starting runtime mappings setup...")
            try {
                Map versionMeta = manifestMeta.versions.find { (it as Map)['id'] == MC_VERSION } as Map
                if (!Files.exists(CACHE_DIR)) Files.createDirectories(CACHE_DIR)
                LOGGER.info("Found version metadata from piston-meta.")
                checkAndUpdateVersionFile(versionMeta)
                LOGGER.info("version.json is up to date.")
                checkAndUpdateOfficialFile()
                LOGGER.info("Official mappings are up to date.")
                loadLayeredMappings()
                LOGGER.info("Finished runtime mappings setup.")
            } catch (IOException e) {
                // Error state, I couldn't make mappings.
                throw e
            } catch (NoSuchElementException e) {
                // Error state, not a known version? Huh?
                throw e
            }
        } catch (IOException e) {
            LOGGER.info("Couldn't connect to piston-meta. Looking for cached file instead.")
            loadLayeredMappings()
            LOGGER.info("Finished runtime mappings setup.")
        }
    }

    private static void checkAndUpdateVersionFile(Map versionMeta) throws IOException {
        String sha1 = versionMeta.sha1

        if (Files.exists(VERSION_FILE)) {
            byte[] existingSha1 = calcSha1(VERSION_FILE)
            byte[] knownSha1 = Hex.decodeHex(sha1)
            if (Arrays.equals(knownSha1, existingSha1)) return
        }

        try (InputStream versionStream = new URL((String) versionMeta.url).openStream()) {
            Files.copy(versionStream, VERSION_FILE, StandardCopyOption.REPLACE_EXISTING)
        }
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
            byte[] knownSha1 = Hex.decodeHex(sha1)
            if (Arrays.equals(knownSha1, existingSha1)) return
        }

        try (InputStream officialStream = new URL(url).openStream()) {
            Files.copy(officialStream, OFFICIAL_FILE, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private static byte[] calcSha1(Path file) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
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

    private static class LoadingVisitor implements MappingVisitor {
        // runtime class name (with dots) to map of moj -> runtime names
        final Map<String, Map<String, List<String>>> methods = [:]
        final Map<String, Map<String, String>> fields = [:]

        String lastClassObf
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
