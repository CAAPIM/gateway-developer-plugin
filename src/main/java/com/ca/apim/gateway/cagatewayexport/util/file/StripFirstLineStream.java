/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.file;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This is used in order to remove the first line when printing the properties to an output stream
 * that contains a date-timestamp. Inspired by https://stackoverflow.com/a/39043903/1108370
 */
@SuppressWarnings("squid:S4349")
public class StripFirstLineStream extends FilterOutputStream {
    private boolean firstlineseen = false;

    public StripFirstLineStream(final OutputStream out) {
        super(out);
    }

    @Override
    public void write(final int b) throws IOException {
        if (firstlineseen) {
            super.write(b);
        } else if (b == '\n') {
            firstlineseen = true;
        }
    }
}
