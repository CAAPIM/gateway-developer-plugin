/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import java.util.regex.Pattern;

@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
final class LinkerConstants {
    
    static final Pattern STORED_PASSWORD_PATTERN = Pattern.compile("secpass.(.+?).plaintext");
    static final String ENCRYPTED_PASSWORD_PREFIX = "$L7C2$";
    
    private LinkerConstants() {
    }
}
