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

package com.bizcom.util.xlog.formatter.log;


import com.bizcom.util.xlog.LogLevel;

/**
 * Simply join the timestamp, log level, tag and message together.
 */
public class DefaultLogFormatter implements LogFormatter {

    @Override
    public String format(int logLevel, String tag, String message, long timestamp) {
        return new StringBuilder(tag.length() + message.length() + 20)
                .append(Long.toString(timestamp))
                .append('\t')
                .append(LogLevel.getShortLevelName(logLevel))
                .append('\t')
                .append(tag)
                .append('\t')
                .append(message)
                .toString();
    }
}
