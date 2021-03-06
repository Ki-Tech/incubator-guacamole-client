/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.auth.cas.conf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import org.apache.guacamole.properties.GuacamoleProperty;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.environment.LocalEnvironment;

/**
 * A GuacamoleProperty whose value is derived from a private key file.
 */
public abstract class PrivateKeyGuacamoleProperty implements GuacamoleProperty<PrivateKey>  {

    @Override
    public PrivateKey parseValue(String value) throws GuacamoleServerException {

        if (value == null || value.isEmpty())
            return null;

        try {

            // Open and read the file specified in the configuration.
            File keyFile = new File(value);
            FileInputStream keyStreamIn = new FileInputStream(keyFile);
            ByteArrayOutputStream keyStreamOut = new ByteArrayOutputStream();
            byte[] keyBuffer = new byte[1024];

            for (int readBytes; (readBytes = keyStreamIn.read(keyBuffer)) != -1;)
                keyStreamOut.write(keyBuffer, 0, readBytes);

            final byte[] keyBytes = keyStreamOut.toByteArray();

            // Set up decryption infrastructure
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return keyFactory.generatePrivate(keySpec);

        }
        catch (FileNotFoundException e) {
            throw new GuacamoleServerException("Could not find the specified key file.", e);
        }
        catch (IOException e) {
            throw new GuacamoleServerException("Could not read in the specified key file.", e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new GuacamoleServerException("RSA algorithm is not available.", e);
        }
        catch (InvalidKeySpecException e) {
            throw new GuacamoleServerException("Key is not in expected PKCS8 encoding.", e);
        }

    }

}
