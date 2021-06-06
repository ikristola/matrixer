/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
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
package org.matrixer.core.util;

/**
 * A range is an inclusive range of integer values
 */
public class Range {
    private final static int EMPTY_VAL = -1;
    private int high;
    private int low;

    /**
     * Creates a new range
     *
     * @param low
     *            the low end of the range
     * @param high
     *            the high end of the range
     */
    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    /**
     * Tests if a value is contained by this range
     *
     * @param value
     *            the value to test
     * @returns true if value is contained by this range, false otherwise
     */
    public boolean contains(int value) {
        return low <= value && value <= high;
    }

    /**
     * Extends this range in either direction to include the value
     *
     * @param value
     *            the value to contain
     */
    public void extendToInclude(int value) {
        high = Math.max(high, value);
        if (low == EMPTY_VAL) {
            low = value;
        } else {
            low = Math.min(low, value);
        }
    }

    /**
     * @returns the low end of this range
     */
    public int min() {
        return low;
    }

    /**
     * @returns the high end of this range
     */
    public int max() {
        return high;
    }

    /**
     * Returns true if this range is empty
     */
    public boolean isEmpty() {
        return high == EMPTY_VAL;
    }

    /**
     * Creates an empty range
     *
     * @returns a new empty range
     */
    public static Range empty() {
        return new Range(EMPTY_VAL, EMPTY_VAL);
    }
}
