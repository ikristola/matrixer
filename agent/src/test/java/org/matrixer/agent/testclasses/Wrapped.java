package org.matrixer.agent.testclasses;

public class Wrapped {

    // Locals: 0 - this,, 1 - path
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
        return 100;
    }

    private void thrower() {
        throw new RuntimeException("thrower");
    }

    // Locals: 0 - this,, 1 - path
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
                throw new IllegalArgumentException();
            }
            return 100;
        } finally {
            System.out.println("Finally");
        }
    }

}
