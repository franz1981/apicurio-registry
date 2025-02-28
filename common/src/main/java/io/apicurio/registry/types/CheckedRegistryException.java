/*
 * Copyright 2020 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.types;

/**
 * Generic checked project exception.
 * <p>
 * Use this exception if you expect the caller would want to handle the exception,
 * possibly making different decisions based on circumstances.
 * <p>
 * This class is intended for extension. Create a more specific exception.
 *
 * @author Jakub Senko <em>m@jsenko.net</em>
 */
public abstract class CheckedRegistryException extends Exception {

    private static final long serialVersionUID = 378403778033312738L;

    protected CheckedRegistryException(String message) {
        super(message);
    }

    protected CheckedRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    protected CheckedRegistryException(Throwable cause) {
        super(cause);
    }
}
