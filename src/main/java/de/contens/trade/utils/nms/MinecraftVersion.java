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

package de.contens.trade.utils.nms;

import com.google.common.collect.ImmutableSet;
import de.contens.trade.utils.reflection.Reflection;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Contens
 * @created 22.03.2021
 */

public enum MinecraftVersion {
    V1_8("v1_8_R1", "v1_8_R2", "v1_8_R3"),
    v1_9("v1_9_R1", "v1_9_R2"),
    v1_10("v1_10_R1"),
    v1_11("v1_11_R1"),
    v1_12("v1_12_R1"),
    v1_13("v1_13_R1", "v1_13_R2");

    private List<String> aliases;

    MinecraftVersion(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }

    public static MinecraftVersion formatVersion(String version) {
        return Arrays.stream(MinecraftVersion.values())
                .filter(v -> v.aliases.contains(version))
                .findFirst()
                .get();
    }

    public static int getMajorVersion() {
        return Integer.parseInt(Reflection.getVersion().split("_")[1]);
    }
}
