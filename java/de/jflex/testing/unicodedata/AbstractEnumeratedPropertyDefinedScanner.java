/*
 * Copyright (C) 2009-2021 Steve Rowe <sarowe@gmail.com>
 * Copyright (C) 2017-2021 Google, LLC.
 *
 * License: https://opensource.org/licenses/BSD-3-Clause
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided with
 *    the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.jflex.testing.unicodedata;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import de.jflex.ucd.CodepointRange;
import de.jflex.ucd.NamedCodepointRange;
import java.io.IOException;
import java.lang.reflect.Array;

public abstract class AbstractEnumeratedPropertyDefinedScanner<T> {
  private final int maxCodePoint;
  private final T[] propertyValues;

  protected AbstractEnumeratedPropertyDefinedScanner(int maxCodePoint, Class<T> clazz) {
    this.maxCodePoint = maxCodePoint;
    propertyValues = (T[]) Array.newInstance(clazz, maxCodePoint + 1);
  }

  public ImmutableList<NamedCodepointRange<T>> blocks() {
    ImmutableList.Builder<NamedCodepointRange<T>> blocks = ImmutableList.builder();
    T prevPropertyValue = propertyValues[0];
    int begCodePoint = 0;
    for (int codePoint = 1; codePoint <= maxCodePoint; ++codePoint) {
      if (codePoint == 0xD800) { // Skip the surrogate blocks
        if (prevPropertyValue != null) {
          blocks.add(NamedCodepointRange.create(prevPropertyValue, begCodePoint, codePoint - 1));
        }
        begCodePoint = codePoint = 0xE000;
        prevPropertyValue = propertyValues[codePoint];
        continue;
      }
      T propertyValue = propertyValues[codePoint];
      if (null == propertyValue || !propertyValue.equals(prevPropertyValue)) {
        if (null != prevPropertyValue) {
          blocks.add(NamedCodepointRange.create(prevPropertyValue, begCodePoint, codePoint - 1));
        }
        prevPropertyValue = propertyValue;
        begCodePoint = codePoint;
      }
    }
    if (null != prevPropertyValue) {
      blocks.add(NamedCodepointRange.create(prevPropertyValue, begCodePoint, maxCodePoint));
    }
    return blocks.build();
  }

  public ImmutableList<CodepointRange> ranges() {
    return blocks().stream().map(NamedCodepointRange::range).collect(toImmutableList());
  }

  protected void setCurCharPropertyValue(String index, int length, T propertyValue) {
    int i = 0;
    while (i < length) {
      int codePoint = index.codePointAt(i);
      propertyValues[codePoint] = propertyValue;
      i += Character.charCount(codePoint);
    }
  }

  public T getPropertyValue(int codepoint) {
    return propertyValues[codepoint];
  }

  public abstract int yylex() throws IOException;
}
