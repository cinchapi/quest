/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Jeff Nelson, Cinchapi Software Collective
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.cinchapi.concourse.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

/**
 * Utilities to handle getting resources in a standard and portable way.
 * 
 * @author jnelson
 */
@SuppressWarnings("deprecation")
public class Resources {

    /**
     * Finds a resource with a given name. The rules for searching resources
     * associated with a given class are implemented by the defining
     * {@linkplain ClassLoader class loader} of the class. This method
     * delegates to this object's class loader. If this object was loaded by
     * the bootstrap class loader, the method delegates to
     * {@link ClassLoader#getSystemResourceAsStream}.
     * 
     * <p>
     * Before delegation, an absolute resource name is constructed from the
     * given resource name using this algorithm:
     * 
     * <ul>
     * 
     * <li>If the {@code name} begins with a {@code '/'} (<tt>'&#92;u002f'</tt>
     * ), then the absolute name of the resource is the portion of the
     * {@code name} following the {@code '/'}.
     * 
     * <li>Otherwise, the absolute name is of the following form:
     * 
     * <blockquote> {@code modified_package_name/name} </blockquote>
     * 
     * <p>
     * Where the {@code modified_package_name} is the package name of this
     * object with {@code '/'} substituted for {@code '.'} (
     * <tt>'&#92;u002e'</tt>).
     * 
     * </ul>
     * 
     * @param name name of the desired resource
     * @return A {@link java.io.InputStream} object or {@code null} if
     *         no resource with this name is found
     * @throws NullPointerException If {@code name} is {@code null}
     * @since JDK1.1
     */
    public static URL get(final String name) {
        File temp;
        try {
            temp = File.createTempFile("java-resource", "tmp");
            Files.copy(new InputSupplier<InputStream>() {

                @Override
                public InputStream getInput() throws IOException {
                    return this.getClass().getResourceAsStream(name);
                }

            }, temp);
            return temp.toURI().toURL();
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }

    }

}
