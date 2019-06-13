/******************************************************************************
 * Copyright © 2016-2019 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of this software, including this file, may be copied, modified,    *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

var sss = function () {

    var CURRENT_VERSION = 1;

    var PRIME_4096_BIT = bigInt(
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

    var PRIME_384_BIT = bigInt("830856716641269388050926147210" +
        "378437007763661599988974204336" +
        "741171904442622602400099072063" +
        "84693584652377753448639527"
    );

    var PRIME_192_BIT = bigInt("14976407493557531125525728362448106789840013430353915016137");

    /**
     * Given a secret, split it into "available" shares where providing "needed" shares is enough to reproduce the secret.
     * All calculations are performed mod p, where p is a large prime number.
     * @param secret the secret
     * @param needed the number of shares needed to reproduce it
     * @param available the total number of shares
     * @param prime the prime number
     * @return the secret shares
     */
    function split(secret, needed, available, prime) {
        // Create a polynomial of degree representing the number of needed pieces
        var coeff = new Array(needed);
        coeff[0] = secret; // our secret is encoded in the polynomial free term

        // The rest of the coefficients are selected randomly as integers mod p and adjusted to the secret size
        for (var i = 1; i < needed; i++) {
            coeff[i] = bigInt.randBetween(0, prime).mod(secret);
        }

        // Clearly the value of this polynomial at x=0 is our secret
        // We generate the shares by running x from 1 to the number of available shares calculating the polynomial value
        // mod p at each point
        var shares = [];
        for (var x = 1; x <= available; x++) {
            var accum = bigInt(secret);
            for (var exp = 1; exp < needed; exp++) {
                accum = bigIntMod(accum.add(bigIntMod(coeff[exp].multiply(bigInt(x).pow(exp)), prime)), prime);
            }
            shares.push({ x: x, share: accum });
        }

        // The resulting points represent the secret shares
        return shares;
    }

    /**
     * Given the needed number of shares or more, reproduce the original polynomial and extract the secret from its free
     * term. All calculations are performed mod p, where p is a large prime number
     * @param shares the shares represention points over the polynomial
     * @param prime the prime number
     * @return the original secret reproduced from the free term of the polynomial which passes through these points
     */
    function combine(shares, prime) {
        // An optimized approach to using Lagrange polynomials to find L(0) (the free term)
        // See https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing "Computationally Efficient Approach"
        var accum = bigInt.zero;
        for (var formula = 0; formula < shares.length; formula++) {
            var numerator = bigInt.one;
            var denominator = bigInt.one;
            for (var count = 0; count < shares.length; count++) {
                if (formula == count) {
                    continue; // If not the same value
                }

                var startposition = shares[formula].x;
                var nextposition = shares[count].x;

                numerator = bigIntMod(numerator.multiply(bigInt(nextposition).negate()), prime); // (numerator * -nextposition) % prime;
                denominator = bigIntMod(denominator.multiply(bigInt(startposition - nextposition)), prime); // (denominator * (startposition - nextposition)) % prime;
            }
            var value = shares[formula].share;
            var tmp = bigInt(value).multiply(numerator).multiply(denominator.modInv(prime));
            accum = bigIntMod(prime.add(accum).add(tmp), prime); //  (prime + accum + (value * numerator * modInverse(denominator))) % prime;
        }
        return accum;
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
     * @param declaredModPrime the modulo prime used for secret sharing calculations. Specify BigInteger.ZERO to calculate automatically
     * @return the secret pieces
     */
    function splitSecret(secretPhrase, totalPieces, minPieces, declaredModPrime) {
        if (minPieces <= 1 || minPieces > totalPieces) {
            throw new Error("Illegal number of minimum pieces " + minPieces + ", must be between 2 and " + totalPieces);
        }
        if (declaredModPrime === undefined) {
            declaredModPrime = bigInt.zero;
        }
        var secretInteger = secretToNumber(secretPhrase);
        var modPrime = getModPrime(declaredModPrime, secretInteger);

        // Split the number into pieces
        var splitSecretOutput = split(secretInteger, minPieces, totalPieces, modPrime);
        var version = is12WordsSecret(secretPhrase.split(" ")) ? CURRENT_VERSION : 0;

        // Convert the pieces back to readable strings prefixed by piece number.
        // We encode the secret data using hex string even if the original secret phrase was composed of 12 words
        var prefix = "" + version + ":" +
            Math.floor(Math.random() * NRS.constants.MAX_INT_JAVA) + ":" +
            totalPieces + ":" +
            minPieces + ":" +
            declaredModPrime + ":";
        var pieces = new Array(splitSecretOutput.length);
        for (var i=0; i<splitSecretOutput.length; i++) {
            pieces[i] = prefix + splitSecretOutput[i].x + ":" + splitSecretOutput[i].share.toString(16);
        }
        return pieces;
    }


    /**
     * Given minPieces pieces out of totalPieces of a secretPhrase, combine the pieces into the original phrase.*
     * Pieces are formatted as [version]:[split id]:[total pieces]:[minimum pieces]:[prime field size or zero]:[piece number]:[piece content]
     *
     * @param encodedSecretPhrasePieces the pieces of the secret phrase formatted as specified above
     * @return the reproduced secret phrase
     */
    function combineSecret(encodedSecretPhrasePieces) {
        var secretPhrasePieces = new Array(encodedSecretPhrasePieces.length);

        // Parse the combination parameters from the first piece
        var tokens = encodedSecretPhrasePieces[0].split(":", 7);
        if (tokens.length < 7) {
            throw new Error("wrong_piece_format");
        }
        var version = parseInt(tokens[0]);
        if (version != 0 && version != CURRENT_VERSION) {
            throw new Error("unsupported_piece_version");
        }
        var id = parseInt(tokens[1]);
        var totalPieces = parseInt(tokens[2]);
        var minPieces = parseInt(tokens[3]);
        var modPrime = bigInt(tokens[4]);
        secretPhrasePieces[0] = tokens[5] + ":" + tokens[6];

        // Make sure all other pieces contains the same parameters
        for (var i = 1; i < encodedSecretPhrasePieces.length; i++) {
            tokens = encodedSecretPhrasePieces[i].split(":", 7);
            if (tokens.length != 7) {
                throw new Error("wrong_piece_format");
            } else if (version != parseInt(tokens[0])) {
                throw new Error("version_differs_between_pieces");
            } else if (id != parseInt(tokens[1])) {
                throw new Error("id_differs_between_pieces");
            } else if (totalPieces != parseInt(tokens[2])) {
                throw new Error("total_differs_between_pieces");
            } else if (minPieces != parseInt(tokens[3])) {
                throw new Error("min_differs_between_pieces");
            } else if (!modPrime.equals(bigInt(tokens[4]))) {
                throw new Error("prime_differs_between_pieces");
            }
            secretPhrasePieces[i] = tokens[5] + ":" + tokens[6];
        }
        if (secretPhrasePieces.length < minPieces) {
            throw new Error("not_enough_pieces");
        }
        return combineImpl(secretPhrasePieces, modPrime, version);
    }

    /**
     * Given minPieces pieces out of totalPieces of a secretPhrase, combine the pieces into the original phrase.*
     * Pieces should formatted with a piece number followed by ':' followed by the piece content.
     *
     * @param secretPhrasePieces the pieces of the secret phrase formatted as [index]:[data]
     * @param modPrime prime number representing the finite field size used by the secret sharing polynomial. Specify BigInteger.ZERO to calculate automatically
     * @param version the combined pieces version
     * @return the reproduced secret phrase
     */
    function combineImpl(secretPhrasePieces, modPrime, version) {
        var secretShares = [];
        var maxShare = bigInt.zero;
        for (var i=0; i<secretPhrasePieces.length; i++) {
            var split = secretPhrasePieces[i].split(":", 2);
            if (split.length != 2) {
                throw new Error("shared_secret_format");
            }
            var share = bigInt(split[1], 16);
            if (share.compareTo(maxShare) > 0) {
                maxShare = share;
            }
            secretShares.push({x: split[0], share: share });
        }

        // Get the optimal mod prime size
        modPrime = getModPrime(modPrime, maxShare);

        // Combine the secret by converting the indexed element to shares (pieces in the expected format)
        var secretInteger = combine(secretShares, modPrime);
        return numberToSecret(secretInteger, version != 0);
    }

    /**
     * Convert a secret represented as number, back to 12 words or general passphrase
     * @param secretInteger the secret number
     * @param is12Words true is original secret is composed of 12 words, false otherwise
     * @return the original passphrase
     */
    function numberToSecret(secretInteger, is12Words) {
        if (is12Words) {
            return from128bit(secretInteger);
        } else {
            try {
                return converters.hexStringToString(secretInteger.toString(16));
            } catch (e) {
                throw new Error("cannot_combine_pieces");
            }
        }
    }

    /**
     * Convert a 12 words passphrase or general passphrase to a secret number
     * @param secretPhrase the passphrase
     * @return the secret represented as number
     */
    function secretToNumber(secretPhrase) {
        var words = secretPhrase.split(" ");
        if (is12WordsSecret(words)) {
            return to128bit(words);
        } else {
            return bigInt(converters.stringToHexString(secretPhrase), 16);
        }
    }

    function is12WordsSecret(words) {
        var allWordsFromList = true;
        for (var i = 0; i < words.length; i++) {
            if (!NRS.constants.SECRET_WORDS_MAP[words[i]]) {
                allWordsFromList = false;
                break;
            }
        }
        return words.length == 12 && allWordsFromList;
    }

    /**
     * Given array of secret phrase words, compose a 128+ bits integer based on the words offset in the words list.
     * This code is compatible with the secret phrase generation in passphrasegenerator.js
     * @param secretPhraseWords the secret phrase words
     * @return compact numeric representation
     */
    function to128bit(secretPhraseWords) {
        n128 = bigInt.zero;
        for (var i = 0; i < secretPhraseWords.length / 3; i++) {
            n128 = n128.add(bigInt(getWordsOffset(secretPhraseWords[3 * i], secretPhraseWords[3 * i + 1], secretPhraseWords[3 * i + 2])));
            if (i == secretPhraseWords.length / 3 - 1) {
                break;
            }
            n128 = n128.shiftLeft(32);
        }
        return n128;
    }

    function getWordsOffset(w1, w2, w3) {
        var WORDS_MAP = NRS.constants.SECRET_WORDS_MAP;
        return getOffset(WORDS_MAP[w1], WORDS_MAP[w2], WORDS_MAP[w3]);
    }

    function getOffset(w1, w2, w3) {
        var n = NRS.constants.SECRET_WORDS.length;

        // The calculation below reverses the calculation performed in the from128bit method which is compatible with passphrasegenerator.js
        // Note that normal % modulo operator won't work here since it returns negative values and this calculation must use positive values
        // for compatibility with Javascript code. Also note the & at the end to convert int to signed int represented as long
        return (w1 + mod(w2 - w1, n) * n + mod(w3 - w2, n) * n * n);
    }

    /**
     * Calculate positive mod
     * @param n number
     * @param m number
     * @returns the positive n%m even if n is negative
     */
    function mod(n, m) {
        return ((n % m) + m) % m;
    }

    /**
     * Calculate positive mod
     * @param n bigInt number
     * @param m bigInt number
     * @returns the positive n%m even if n is negative
     */
    function bigIntMod(n, m) {
        return (n.mod(m).add(m)).mod(m);
    }

    function from128bit(n128) {
        var ALL_WORDS = NRS.constants.SECRET_WORDS;
        var n = ALL_WORDS.length;
        var	words = new Array(12);
        var bitmask = bigInt("ffffffff", 16);
        for (i=0; i < 4; i++    ) {
            var x = n128.and(bitmask).toJSNumber();
            var w1 = x % n;
            var w2 = (((x / n) >> 0) + w1) % n;
            var w3 = (((((x / n) >> 0) / n) >> 0) + w2) % n;
            var index = 3 * (4 - i - 1);
            words[index] = ALL_WORDS[w1];
            words[index + 1] = ALL_WORDS[w2];
            words[index + 2] = ALL_WORDS[w3];
            n128 = n128.shiftRight(32);
        }
        return words.join(" ");
    }

    /**
     * Given a secret phrase calculate the modulo prime finite field size which is sufficiently large to split the
     * shared secrets
     * @param modPrime the modulo prime
     * @param secretInteger the secret integer
     * @return the prime number representing the finite field size
     */
    function getModPrime(modPrime, secretInteger) {
        // Determine the mod prime size
        if (!bigInt.zero.equals(modPrime)) {
            if (secretInteger.compareTo(modPrime) >= 0) {
                throw new Error("secret_larger_than_prime");
            }
            return modPrime;
        }
        return getModPrimeForSecret(secretInteger);
    }

    function getModPrimeForSecret(secret) {
        if (secret.compareTo(PRIME_192_BIT) < 0) {
            return PRIME_192_BIT;
        } else if (secret.compareTo(PRIME_384_BIT) < 0) {
            return PRIME_384_BIT;
        } else if (secret.compareTo(PRIME_4096_BIT) < 0) {
            return PRIME_4096_BIT;
        } else {
            // if you make it here, you are 4000+ bits big and this call is going to be really expensive
            throw new Error("secret_too_long");
        }
    }

    return {
        split: split,
        combine: combine,
        splitSecret: splitSecret,
        combineSecret: combineSecret,
        from128bit: from128bit,
        to128bit: to128bit,
        PRIME_4096_BIT: PRIME_4096_BIT,
        PRIME_384_BIT: PRIME_384_BIT,
        PRIME_192_BIT: PRIME_192_BIT
    };
}();

if (isNode) {
    module.exports = sss;
}