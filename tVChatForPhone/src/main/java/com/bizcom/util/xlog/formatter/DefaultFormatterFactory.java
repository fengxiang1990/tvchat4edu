/*
 * Copyright 2015 Elvis Hew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bizcom.util.xlog.formatter;


import com.bizcom.util.xlog.formatter.log.DefaultLogFormatter;
import com.bizcom.util.xlog.formatter.log.LogFormatter;
import com.bizcom.util.xlog.formatter.message.json.DefaultJsonFormatter;
import com.bizcom.util.xlog.formatter.message.json.JsonFormatter;
import com.bizcom.util.xlog.formatter.message.method.DefaultMethodFormatter;
import com.bizcom.util.xlog.formatter.message.method.MethodFormatter;
import com.bizcom.util.xlog.formatter.message.throwable.DefaultThrowableFormatter;
import com.bizcom.util.xlog.formatter.message.throwable.ThrowableFormatter;
import com.bizcom.util.xlog.formatter.message.xml.DefaultXmlFormatter;
import com.bizcom.util.xlog.formatter.message.xml.XmlFormatter;

/**
 * Factory for providing default formatter.
 */
public class DefaultFormatterFactory {

    /**
     * Create the default JSON formatter.
     */
    public static JsonFormatter createJsonFormatter() {
        return new DefaultJsonFormatter();
    }

    /**
     * Create the default XML formatter.
     */
    public static XmlFormatter createXmlFormatter() {
        return new DefaultXmlFormatter();
    }

    /**
     * Create the default method formatter.
     */
    public static MethodFormatter createMethodFormatter() {
        return new DefaultMethodFormatter();
    }

    /**
     * Create the default throwable formatter.
     */
    public static ThrowableFormatter createThrowableFormatter() {
        return new DefaultThrowableFormatter();
    }

    /**
     * Create the default log formatter.
     */
    public static LogFormatter createLogFormatter() {
        return new DefaultLogFormatter();
    }
}
