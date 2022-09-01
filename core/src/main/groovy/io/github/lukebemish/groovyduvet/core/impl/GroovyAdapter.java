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

package io.github.lukebemish.groovyduvet.core.impl;

import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.LanguageAdapterException;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class GroovyAdapter implements LanguageAdapter {
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
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
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
