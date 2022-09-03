/*
 * Copyright (C) 2022 Luke Bemish, GroovyMC, and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package io.github.lukebemish.groovyduvet.core.impl;

import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.LanguageAdapterException;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class GroovyAdapter implements LanguageAdapter {
    private final Pattern groovyJarMatcher = Pattern.compile("org.apache.groovy/groovy/[0-9.]+/");
    private final Supplier<DelegatedLanguageAdapter> adapter = new Supplier<>() {
        DelegatedLanguageAdapter adapter;
        volatile boolean initialized = false;
        @Override
        public DelegatedLanguageAdapter get() {
            if (initialized) {
                return adapter;
            }
            synchronized (this) {
                initialized = true;
                try {
                    Class<?> adapterImpl =
                            Class.forName("io.github.lukebemish.groovyduvet.core.impl.GroovyAdapterImpl", true, QuiltLauncherBase.getLauncher().getTargetClassLoader());
                    adapter = ((DelegatedLanguageAdapter) adapterImpl.getConstructor().newInstance());
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeLanguageAdapterException(new LanguageAdapterException(e));
                }
            }
            return adapter;
        }
    };

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        try {
            return adapter.get().create(mod, value, type);
        } catch (RuntimeLanguageAdapterException e) {
            throw e.wrapped;
        }
    }

    public interface DelegatedLanguageAdapter {
        <T> T create(ModContainer mod, String value, Class<T> type);
    }

    private static class RuntimeLanguageAdapterException extends RuntimeException {
        public final LanguageAdapterException wrapped;

        public RuntimeLanguageAdapterException(LanguageAdapterException wrapped) {
            this.wrapped = wrapped;
        }
    }
}
