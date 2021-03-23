/*
 * ******************************************************************************
 *  * Copyright (C) 2021, Contens
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  *
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * 3. Neither the name of the copyright holder nor the names of its contributors
 *  *    may be used to endorse or promote products derived from this software
 *  *    without specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *  *****************************************************************************
 */

package de.contens.trade.utils.reflection;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Contens
 * @created 21.03.2021
 */

public class Reflection {

    private static final Map<String, Class<?>> _loadedNMSClasses = new HashMap<String, Class<?>>();
    private static final Map<String, Class<?>> _loadedOBCClasses = new HashMap<String, Class<?>>();

    private static final Map<Class<?>, Map<String, Field>> _loadedFields =
            new HashMap<Class<?>, Map<String, Field>>();

    private static final Map<Class<?>, Map<String, Map<ArrayWrapper<Class<?>>, Method>>>
            _loadedMethods = new HashMap<Class<?>, Map<String, Map<ArrayWrapper<Class<?>>, Method>>>();
    private static String _versionString;

    private Reflection() {}

    public static synchronized String getVersion() {
        if (_versionString == null) {
            if (Bukkit.getServer() == null) {
                return null;
            }
            String name = Bukkit.getServer().getClass().getPackage().getName();
            _versionString = name.substring(name.lastIndexOf('.') + 1);
        }

        return _versionString;
    }

    public static synchronized Class<?> getNmsClass(String className) {
        if (_loadedNMSClasses.containsKey(className)) {
            return _loadedNMSClasses.get(className);
        }

        String fullName = "net.minecraft.server." + getVersion() + className;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
            _loadedNMSClasses.put(className, null);
            return null;
        }
        _loadedNMSClasses.put(className, clazz);
        return clazz;
    }

    public static synchronized Class<?> getObcClass(String className) {
        if (_loadedOBCClasses.containsKey(className)) {
            return _loadedOBCClasses.get(className);
        }

        String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
            _loadedOBCClasses.put(className, null);
            return null;
        }
        _loadedOBCClasses.put(className, clazz);
        return clazz;
    }

    public static synchronized Object getHandle(Object obj) {
        try {
            return getMethod(obj.getClass(), "getHandle").invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized Field getField(Class<?> clazz, String name) {
        Map<String, Field> loaded;
        if (!_loadedFields.containsKey(clazz)) {
            loaded = new HashMap<>();
            _loadedFields.put(clazz, loaded);
        } else {
            loaded = _loadedFields.get(clazz);
        }
        if (loaded.containsKey(name)) {
            return loaded.get(name);
        }
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            loaded.put(name, field);
            return field;
        } catch (Exception e) {
            e.printStackTrace();

            loaded.put(name, null);
            return null;
        }
    }

    public static synchronized Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        if (!_loadedMethods.containsKey(clazz)) {
            _loadedMethods.put(clazz, new HashMap<String, Map<ArrayWrapper<Class<?>>, Method>>());
        }

        Map<String, Map<ArrayWrapper<Class<?>>, Method>> loadedMethodNames = _loadedMethods.get(clazz);
        if (!loadedMethodNames.containsKey(name)) {
            loadedMethodNames.put(name, new HashMap<ArrayWrapper<Class<?>>, Method>());
        }

        Map<ArrayWrapper<Class<?>>, Method> loadedSignatures = loadedMethodNames.get(name);
        ArrayWrapper<Class<?>> wrappedArg = new ArrayWrapper<Class<?>>(args);
        if (loadedSignatures.containsKey(wrappedArg)) {
            return loadedSignatures.get(wrappedArg);
        }

        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && Arrays.equals(args, m.getParameterTypes())) {
                m.setAccessible(true);
                loadedSignatures.put(wrappedArg, m);
                return m;
            }
        }
        loadedSignatures.put(wrappedArg, null);
        return null;
    }

    public static synchronized void setFieldValue(
            Field field, Object associatedObject, Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(associatedObject, fieldValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
