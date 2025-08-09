import java.io.*;
import java.util.*;
import java.math.BigInteger;

public class SecretFinder {
    public static void main(String[] args) {
        // Hardcoded file names for two test cases
        String[] files = {"input.json","input2.json"};

        for (String fileName : files) {
            try {
                // 1. Read JSON file into a String
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                String json = sb.toString();

                // 2. Extract n and k
                int n = extractInt(json, "\"n\"");
                int k = extractInt(json, "\"k\"");

                // 3. Extract points
                List<BigInteger[]> points = new ArrayList<>();
                String withoutKeys = json.replaceFirst("\\\"keys\\\"\\s*:\\s*\\{[^}]*\\},", "");

                String[] entries = withoutKeys.split("\\},");
                for (String entry : entries) {
                    entry = entry.trim();
                    if (entry.length() == 0 || entry.equals("}")) continue;
                    int keyStart = entry.indexOf("\"") + 1;
                    int keyEnd = entry.indexOf("\"", keyStart);
                    if (keyStart == 0 || keyEnd == -1) continue;
                    String keyStr = entry.substring(keyStart, keyEnd);
                    BigInteger x = BigInteger.valueOf(Long.parseLong(keyStr));

                    int base = extractInt(entry, "\"base\"");
                    String valueStr = extractString(entry, "\"value\"");
                    BigInteger y = new BigInteger(valueStr, base);

                    points.add(new BigInteger[]{x, y});
                }

                // 4. Sort points by x
                points.sort(Comparator.comparing(a -> a[0]));

                // 5. Take first k points
                BigInteger[] xs = new BigInteger[k];
                BigInteger[] ys = new BigInteger[k];
                for (int i = 0; i < k; i++) {
                    xs[i] = points.get(i)[0];
                    ys[i] = points.get(i)[1];
                }

                // 6. Lagrange interpolation to get c at x=0
                BigInteger c = lagrangeInterpolation(xs, ys, BigInteger.ZERO);

                // 7. Output
                System.out.println(fileName + " -> { \"c\": " + c + " }");

            } catch (Exception e) {
                System.err.println("Error processing file: " + fileName);
                e.printStackTrace();
            }
        }
    }

    private static int extractInt(String src, String key) {
        int idx = src.indexOf(key);
        if (idx == -1) return -1;
        int colon = src.indexOf(":", idx);
        int end = src.indexOf(",", colon);
        if (end == -1) end = src.indexOf("}", colon);
        return Integer.parseInt(src.substring(colon + 1, end).replaceAll("[^0-9]", ""));
    }

    private static String extractString(String src, String key) {
        int idx = src.indexOf(key);
        if (idx == -1) return "";
        int colon = src.indexOf(":", idx);
        int quote1 = src.indexOf("\"", colon + 1);
        int quote2 = src.indexOf("\"", quote1 + 1);
        return src.substring(quote1 + 1, quote2);
    }

    private static BigInteger lagrangeInterpolation(BigInteger[] x, BigInteger[] y, BigInteger value) {
        BigInteger result = BigInteger.ZERO;
        int n = x.length;
        for (int i = 0; i < n; i++) {
            BigInteger term = y[i];
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    BigInteger numerator = value.subtract(x[j]);
                    BigInteger denominator = x[i].subtract(x[j]);
                    term = term.multiply(numerator).divide(denominator);
                }
            }
            result = result.add(term);
        }
        return result;
    }
}
