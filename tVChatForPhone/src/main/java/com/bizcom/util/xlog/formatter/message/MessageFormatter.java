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

package com.bizcom.util.xlog.formatter.message;

/**
 * A message formatter is used for format a message or part of message that is not a string, or that
 * is a string but not well formatted, we should format the data to a well formatted string so
 * printers can print them.
 *
 * @param <T> the type of the message
 */
public interface MessageFormatter<T> {

    /**
     * Format the data.
     *
     * @param data the data to format
     * @return the formatted string data
     */
    String format(T data);
}
