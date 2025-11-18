import java.math.BigInteger;
import java.util.Iterator;
import java.util.Scanner;

public class Bit {

    public static boolean isSet(long x, int position) {
        assert position >= 0 && position < 64;
        return (x & (1L << position)) != 0;
    }

    public static long set(long x, int position) {
        assert position >= 0 && position < 64;
        return x | (1L << position);
    }

    public static long clear(long x, int position) {
        assert position >= 0 && position < 64;
        return x & ~(1L << position);
    }

    public static int countLeadingZeros(long x) {
        // Returns the number of leading zeros
        // Fast binary search algorithm
        int count = 0;
        if (x == 0) return 64;
        if ((x >>> 32) == 0) { count += 32; x <<= 32; }
        if ((x >>> 48) == 0) { count += 16; x <<= 16; }
        if ((x >>> 56) == 0) { count += 8;  x <<= 8; }
        if ((x >>> 60) == 0) { count += 4;  x <<= 4; }
        if ((x >>> 62) == 0) { count += 2;  x <<= 2; }
        if ((x >>> 63) == 0) count++;
        return count;
    }

    public static int msb(long x) {
        // Returns the position of the most significant one bit
        // Returns -1 if there are no one bits (x == 0)
        return 63 - countLeadingZeros(x);
    }

    public static int countOnes(long x) {
        int count = 0;
        while (x != 0) {
            x &= (x - 1); // Clear the least significant one bit
            count++;
        }
        return count;
    }

    public static int countZeros(long x) {
        return countOnes(~x);
    }

    // Image & Value methods
    
    public static String toString(long x) {
        // Returns a list of the bits positions containing a one
        String separator = "";
        String result = "{";
        for (Integer position : ones(x)) {
            result += separator;
            result += Integer.toString(position);
            separator = ",";
        }
        result += "}";
        return result;
    }

    public static long valueOf(String s) {
        // Converts a list of bit positions to a long
        // Inverse ot the above toString method
        Scanner scanner = new Scanner(s);
        scanner.useDelimiter("[, \t\\{\\}]+");
        long result = 0;
        while (scanner.hasNextInt()) {
            int position = scanner.nextInt();
            result = set(result, position);
        }
        return result;
    }

    // Formatting

    private static long quo(long dividend, long divisor) {
        long result = dividend / divisor;
        if ((dividend < 0) != (divisor < 0) && (divisor * result != dividend)) {
            result--;
        }
        return result;
    }

    private static long rem(long dividend, long divisor) {
        long result = dividend % divisor;
        if (result >= 0) {
            return result;
        } else {
            return result + divisor;
        }
    }

    private static int digits(int bits, int base) {
        // Number of digits in a n-bit number base b
        BigInteger limit = BigInteger.ONE.shiftLeft(bits);
        BigInteger radix = BigInteger.valueOf(base);
        BigInteger power = BigInteger.ONE;
        int digits = 0;
        while (power.compareTo(limit) < 0) {
            power = power.multiply(radix);
            digits++;
        }
        return digits;
    }

    private static final int[] digits = new int[37];
    static {
        for (int base = 2; base < digits.length; base++) {
            digits[base]= digits(64, base);
        }
    }

    private static char digit(int digit) {
        return "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(digit);
    }

    public static String toString(long x, int base, int digits) {
        if (digits <= 0 && (x == 0 || x == -1)) return "";
        String prefix = toString(quo(x, base), base, digits-1);
        char suffix = digit((int) rem(x, base));
        return prefix + suffix;
    }

    private static int group(int base) {
        switch (base) {
            case 2:  return 8;
            case 8:  return 3;
            case 16: return 4;
            default: return 3;
        }
    }

    private static char separator(int base) {
        switch (base) {
            case 10: return ',';
            default: return '_';
        }
    }

    private static String group(String s, int base) {
        // Insert the separator character between groups of digits
        int group = group(base);
        int count = s.length() % group + 1;
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (--count == 0) {
                if (i > 0) result += separator(base);
                count = group;
            }
            result += c;
        }
        return result;
    }


    public static String toString(long x, int base, boolean group) {
        if (base == 0) {
            return toString(x);
        } else {
            String result = toString(x, base,digits[base]);
            return group ? group(result, base) : result;
        }
    }

    public static String toString(long x, int base) {
        return toString(x, base, true);
    }


    // Iterators: iterate over bit positions containing a one.
    //
    //     Allows loops such as
    //
    //     int numberOnes = 0;
    //     for (Integer position : Bit.ones(x)) {
    //         numberOnes++;
    //       }

    public static Iterator<Integer> iterator(long x) {
        return new Iterator<Integer>() {

            private long bits = x;

            @Override
            public boolean hasNext() {
                return this.bits != 0;
            }

            @Override
            public Integer next() {
                long temp = this.bits & (this.bits - 1);   // Clear the least significant 1 bit
                long mask = this.bits - temp;              // Mask for the bit just cleared
                this.bits = temp;                          // Update the iterator state
                return msb(mask);                          // Position of the bit just cleared
            }
        };

    }

    public static Iterable<Integer> ones(long x) {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return Bit.iterator(x);
            }
        };
    }

    public static Iterable<Integer> zeros(long x) {
        return ones(~x);
    }

    // A more efficient version of an iterator that doesn't
    // create objecgs of class Integer each time next() is
    // invoked
    
    public static class BitIterator {

        private long bits;

        public BitIterator(long bits) {
            this.bits = bits;
        }

        public boolean hasNext() {
            return this.bits != 0;
        }

        public int next() {
            long temp = this.bits & (this.bits - 1);   // Clear the least significant 1 bit
            long mask = this.bits - temp;              // Mask for the bit just cleared
            this.bits = temp;                          // Update the iterator state
            return msb(mask);                          // Position of the bit just cleared
        }
    }
}
