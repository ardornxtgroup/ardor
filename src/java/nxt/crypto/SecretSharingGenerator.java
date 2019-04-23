/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.crypto;

import nxt.Constants;
import nxt.util.Convert;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SecretSharingGenerator {

    private static final int CURRENT_VERSION = 1;

    static final BigInteger PRIME_4096_BIT = new BigInteger(
            "1671022210261044010706804337146599012127" +
                    "9427984758140486147735732543262527544919" +
                    "3095812289909599609334542417074310282054" +
                    "0780117501097269771621177740562184444713" +
                    "5311624699359973445785442150139493030849" +
                    "1201896951396220211014303634039307573549" +
                    "4951338587994892653929285926514054477984" +
                    "1897745831487644537568464106991023630108" +
                    "6045751504900830441750495932712549251755" +
                    "0884842714308894440025555839788342744866" +
                    "7101368958164663781091806630951947745404" +
                    "9899622319436016030246615841346729868014" +
                    "9869334160881652755341231281231973786191" +
                    "0590928243420749213395009469338508019541" +
                    "0958855418900088036159728065975165578015" +
                    "3079187511387238090409461192977321170936" +
                    "6081401737953645348323163171237010704282" +
                    "8481068031277612787461827099245660019965" +
                    "4423851454616735972464821439378482870833" +
                    "7709298145449348366148476664877596527269" +
                    "1765522730435723049823184958030880339674" +
                    "1433100452606317504985611860713079871716" +
                    "8809146278034477061142090096734446658190" +
                    "8273334857030516871663995504285034522155" +
                    "7158160427604895839673593745279150722839" +
                    "3997083495197879290548002853265127569910" +
                    "9306488129210915495451479419727501586051" +
                    "1232507931203905482587057398637416125459" +
                    "0876872367709717423642369650017374448020" +
                    "8386154750356267714638641781056467325078" +
                    "08534977443900875333446450467047221"
    );
    static final BigInteger PRIME_384_BIT = new BigInteger("830856716641269388050926147210" +
            "378437007763661599988974204336" +
            "741171904442622602400099072063" +
            "84693584652377753448639527"
    );
    static final BigInteger PRIME_192_BIT = new BigInteger("14976407493557531125525728362448106789840013430353915016137");

    private static Map<String, Integer> WORDS_MAP = IntStream.range(0, Constants.ALL_SECRET_PHRASE_WORDS.length).boxed().collect(Collectors.toMap(i -> Constants.ALL_SECRET_PHRASE_WORDS[i], i -> i));

    private static final String[] NON_STANDARD_SECRET = new String[0]; // Signal that the secret is not composed from 12 words selected from the WORDS array

    private static SecretSharing getSecretSharingEngine() {
        return new SimpleShamirSecretSharing();
    }

    /**
     * Given a secretPhrase split it into totalPieces pieces where each minPieces of them are enough to reproduce the secret.
     * If the secret phrase is 12 words from the list, the secret is converted into 128 bit number which is very compact.
     * Otherwise the secret phrase is converted to bytes using UTF8 then to a BigInteger.
     *
     * Pieces are returned with a piece number followed by : and the piece content.
     * The piece number is required when reproducing the secret.
     *
     * @param secretPhrase the secret phrase
     * @param totalPieces the number of generated pieces
     * @param minPieces the number of pieces needed to reproduce the secret phrase
     * @param declaredModePrime the modulo prime used for secret sharing calculations. Specify BigInteger.ZERO to calculate automatically
     * @return the secret pieces
     */
    public static String[] split(String secretPhrase, int totalPieces, int minPieces, BigInteger declaredModePrime) {
        if (minPieces <= 1 || minPieces > totalPieces) {
            throw new IllegalArgumentException(String.format("Illegal number of minimum pieces %d, must be between 2 and %d", minPieces, totalPieces));
        }
        BigInteger secretInteger = secretToNumber(secretPhrase);
        BigInteger modPrime = getModPrime(declaredModePrime, secretInteger);

        // Split the number into pieces
        Random random = Crypto.getSecureRandom();
        SecretShare[] splitSecretOutput = getSecretSharingEngine().split(secretInteger, minPieces, totalPieces, modPrime, random);
        int version = is12WordsSecret(secretPhrase.split(" ")) ? CURRENT_VERSION : 0;

        // Convert the pieces back to readable strings prefixed by piece number.
        // We encode the secret data using hex string even if the original secret phrase was composed of 12 words
        String prefix = String.format("%d:%d:%d:%d:%s:", version, random.nextInt(), totalPieces, minPieces, declaredModePrime.toString());
        return Arrays.stream(splitSecretOutput).map(piece -> prefix + piece.getX() + ":" + Convert.toHexString(piece.getShare().toByteArray())).toArray(String[]::new);
    }

    /**
     * Given minPieces pieces out of totalPieces of a secretPhrase, combine the pieces into the original phrase.*
     * Pieces are formatted as [version]:[split id]:[total pieces]:[minimum pieces]:[prime field size or zero]:[piece number]:[piece content]
     *
     * @param encodedSecretPhrasePieces the pieces of the secret phrase formatted as specified above
     * @return the reproduced secret phrase
     */
    public static String combine(String[] encodedSecretPhrasePieces) {
        String[] secretPhrasePieces = new String[encodedSecretPhrasePieces.length];

        // Parse the combination parameters from the first piece
        String[] tokens = encodedSecretPhrasePieces[0].split(":", 6);
        if (tokens.length < 6) {
            throw new IllegalArgumentException("Wrong piece format, should be v:id:n:k:p:#:data");
        }
        int version = Integer.parseInt(tokens[0]);
        if (version != 0 && version != CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported piece version " + version);
        }
        int id = Integer.parseInt(tokens[1]);
        int totalPieces = Integer.parseInt(tokens[2]);
        int minPieces = Integer.parseInt(tokens[3]);
        BigInteger modPrime = new BigInteger(tokens[4]);
        secretPhrasePieces[0] = tokens[5];

        // Make sure all other pieces contains the same parameters
        for (int i = 1; i < encodedSecretPhrasePieces.length; i++) {
            tokens = encodedSecretPhrasePieces[i].split(":", 6);
            if (tokens.length != 6) {
                throw new IllegalArgumentException("Wrong piece format, should be n:k:p:#:data");
            }
            try {
                if (version != Integer.parseInt(tokens[0])) {
                    throw new IllegalArgumentException("Version differs between pieces");
                } else if (id != Integer.parseInt(tokens[1])) {
                    throw new IllegalArgumentException("Id differs between pieces");
                } else if (totalPieces != Integer.parseInt(tokens[2])) {
                    throw new IllegalArgumentException("Total number of shares differs between pieces");
                } else if (minPieces != Integer.parseInt(tokens[3])) {
                    throw new IllegalArgumentException("Minimum number of shares differs between pieces");
                } else if (!modPrime.equals(new BigInteger(tokens[4]))) {
                    throw new IllegalArgumentException("Modulo prime differs between pieces");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Wrong piece numeric values, should be v:id:n:k:p:#:data");
            }
            secretPhrasePieces[i] = tokens[5];
        }
        if (secretPhrasePieces.length < minPieces) {
            throw new IllegalArgumentException(String.format("Need %d pieces to combine the original secret, only %d unique piece(s) available", minPieces, secretPhrasePieces.length));
        }
        return combine(secretPhrasePieces, modPrime, version);
    }

    /**
     * Given minPieces pieces out of totalPieces of a secretPhrase, combine the pieces into the original phrase.*
     * Pieces should formatted with a piece number followed by ':' followed by the piece content.
     *
     * @param secretPhrasePieces the pieces of the secret phrase formatted as [index]:[data]
     * @param modPrime the prime number representing the finite field size used by the secret sharing polynomial. Specify BigInteger.ZERO to calculate automatically
     * @param version the piece version
     * @return the reproduced secret phrase
     */
    private static String combine(String[] secretPhrasePieces, BigInteger modPrime, int version) {
        List<SecretShare> secretShares = Arrays.stream(secretPhrasePieces).map(string -> {
            String[] split = string.split(":", 2);
            if (split.length != 2) {
                throw new IllegalArgumentException("shared secret not formatted as #:data");
            }
            return new SecretShare(Integer.parseInt(split[0]), new BigInteger(split[1], 16));
        }).collect(Collectors.toList());

        // Get the optimal mod prime size
        BigInteger maxShare = secretShares.stream().map(SecretShare::getShare).max(Comparator.naturalOrder()).orElse(BigInteger.ZERO);
        modPrime = getModPrime(modPrime, maxShare);

        // Combine the secret by converting the indexed element to shares (pieces in the expected format)
        BigInteger secretInteger = getSecretSharingEngine().combine(secretShares.toArray(new SecretShare[0]), modPrime);
        return numberToSecret(secretInteger, version != 0);
    }

    /**
     * Convert a secret represented as number, back to 12 words or general passphrase
     * @param secretInteger the secret number
     * @param is12Words true if the original secret composed of 12 words from the list, false otherwise
     * @return the original passphrase
     */
    private static String numberToSecret(BigInteger secretInteger, boolean is12Words) {
        if (is12Words) {
            String[] secretPhraseWords = from128bit(secretInteger);
            return String.join(" ", secretPhraseWords);
        } else {
            return new String(Convert.parseHexString(secretInteger.toString(16)), StandardCharsets.UTF_8);
        }
    }

    /**
     * Convert a 12 words passphrase or general passphrase to a secret number
     * @param secretPhrase the passphrase
     * @return the secret represented as number
     */
    private static BigInteger secretToNumber(String secretPhrase) {
        String[] words = secretPhrase.split(" ");
        if (is12WordsSecret(words)) {
            return to128bit(words);
        } else {
            return new BigInteger(Convert.toHexString(secretPhrase.getBytes(StandardCharsets.UTF_8)), 16);
        }
    }

    private static boolean is12WordsSecret(String[] words) {
        return words.length == 12 && Arrays.stream(words).allMatch(w -> WORDS_MAP.get(w) != null);
    }

    /**
     * Given array of secret phrase words, compose a 128+ bits integer based on the words offset in the words list.
     * This code is compatible with the secret phrase generation in passphrasegenerator.js
     * @param secretPhraseWords the secret phrase words
     * @return compact numeric representation
     */
    static BigInteger to128bit(String[] secretPhraseWords) {
        BigInteger n128 = BigInteger.ZERO;
        for (int i = 0; i < secretPhraseWords.length / 3; i++) {
            n128 = n128.add(new BigInteger("" + getSignedInt(secretPhraseWords[3 * i], secretPhraseWords[3 * i + 1], secretPhraseWords[3 * i + 2])));
            if (i == secretPhraseWords.length / 3 - 1) {
                break;
            }
            n128 = n128.shiftLeft(Integer.SIZE);
        }
        return n128;
    }

    private static long getSignedInt(String w1, String w2, String w3) {
        return getSignedInt(WORDS_MAP.get(w1), WORDS_MAP.get(w2), WORDS_MAP.get(w3));
    }

    private static long getSignedInt(int w1, int w2, int w3) {
        int n = Constants.ALL_SECRET_PHRASE_WORDS.length;

        // The calculation below reverses the calculation performed in the from128bit method which is compatible with passphrasegenerator.js
        // Note that normal % modulo operator won't work here since it returns negative values and this calculation must use positive values
        // for compatibility with Javascript code. Also note the & at the end to convert int to signed int represented as long
        return (w1 + Math.floorMod(w2 - w1, n) * n + Math.floorMod(w3 - w2, n) * n * n) & 0x00000000ffffffffL;
    }

    static String[] from128bit(BigInteger n128orig) {
        String[] words = new String[12];
        String[] allSecretPhraseWords = Constants.ALL_SECRET_PHRASE_WORDS;
        int n = allSecretPhraseWords.length;
        long w1, w2, w3;
        BigInteger n128 = new BigInteger(n128orig.toString());
        for (int i = 0; i < 4; i++) {
            long x = n128.intValue() & 0x00000000ffffffffL;
            n128 = n128.shiftRight(Integer.SIZE);
            w1 = x % n;
            w2 = (((x / n)) + w1) % n;
            w3 = (((((x / n)) / n)) + w2) % n;
            if (w2 < 0 || w2 >= n || w3 < 0 || w3 >= n) {
                return NON_STANDARD_SECRET;
            }
            int index = 3 * (4 - i - 1);
            words[index] = allSecretPhraseWords[(int) w1];
            words[index + 1] = allSecretPhraseWords[(int) w2];
            words[index + 2] = allSecretPhraseWords[(int) w3];
        }
        if (n128.compareTo(BigInteger.ZERO) > 0) {
            throw new IllegalStateException(String.format("number %s has more than 128 bit", n128orig));
        }
        return words;
    }

    /**
     * Given a secret phrase calculate the modulo prime finite field size which is sufficiently large to split the
     * shared secrets
     * @param secretPhrase the secret phrase
     * @return the prime number representing the finite field size
     */
    public static BigInteger getModPrime(String secretPhrase) {
        BigInteger secretInteger = secretToNumber(secretPhrase);
        return getModPrime(BigInteger.ZERO, secretInteger);
    }

    private static BigInteger getModPrime(BigInteger modPrime, BigInteger secretInteger) {
        // Determine the mod prime size
        if (!BigInteger.ZERO.equals(modPrime)) {
            if (secretInteger.compareTo(modPrime) >= 0) {
                throw new IllegalArgumentException("Secret cannot be larger than modulus.  " + "Secret=" + secretInteger + " Modulus=" + modPrime);
            }
            return modPrime;
        }
        return getModPrimeForSecret(secretInteger);
    }

    private static BigInteger getModPrimeForSecret(BigInteger secret) {
        if (secret.compareTo(SecretSharingGenerator.PRIME_192_BIT) < 0) {
            return SecretSharingGenerator.PRIME_192_BIT;
        } else if (secret.compareTo(SecretSharingGenerator.PRIME_384_BIT) < 0) {
            return SecretSharingGenerator.PRIME_384_BIT;
        } else if (secret.compareTo(SecretSharingGenerator.PRIME_4096_BIT) < 0) {
            return SecretSharingGenerator.PRIME_4096_BIT;
        } else {
            // if you make it here, you are 4000+ bits big and this call is going to be really expensive
            throw new IllegalStateException("Cannot split secrets of more than 4024 bit");
        }
    }

}
