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
package org.matrixer.agent.testclasses;

public class Wrapped {

    private int x = 100;

    public int towrap(int path) throws IllegalArgumentException {
        if (path == 1) {
            return 1;
        }
        if (path == 2) {
            throw new RuntimeException();
        }
        if (path == 3) {
            throw new IllegalArgumentException();
        }
        if (path == 4) {
            thrower();
        }
        return x;
    }

    private void thrower() {
        throw new RuntimeException("thrower");
    }

    public int wrapped(int path) throws IllegalArgumentException {
        System.out.println("Before");
        try {
            if (path == 1) {
                return 1;
            }
            if (path == 2) {
                throw new RuntimeException();
            }
            if (path == 3) {
                try {
                    throw new IllegalArgumentException();
                } finally {
                    System.out.println("Inner Finally");
                }
            }
            if (path == 4) {
                thrower();
            }
            return 100;
        } finally {
            System.out.println("Finally");
        }
    }

}
