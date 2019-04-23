package nxt.crypto;

import java.math.BigInteger;
import java.util.Random;

public interface SecretSharing {

    SecretShare[] split(BigInteger secret, int needed, int available, BigInteger prime, Random random);

    BigInteger combine(SecretShare[] shares, BigInteger prime);
}
