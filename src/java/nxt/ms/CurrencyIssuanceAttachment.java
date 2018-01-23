/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt.ms;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class CurrencyIssuanceAttachment extends Attachment.AbstractAttachment {

    private final String name;
    private final String code;
    private final String description;
    private final byte type;
    private final long initialSupplyQNT;
    private final long reserveSupplyQNT;
    private final long maxSupplyQNT;
    private final int issuanceHeight;
    private final long minReservePerUnitNQT;
    private final int minDifficulty;
    private final int maxDifficulty;
    private final byte ruleset;
    private final byte algorithm;
    private final byte decimals;

    public CurrencyIssuanceAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.name = Convert.readString(buffer, buffer.get(), Constants.MAX_CURRENCY_NAME_LENGTH);
        this.code = Convert.readString(buffer, buffer.get(), Constants.MAX_CURRENCY_CODE_LENGTH);
        this.description = Convert.readString(buffer, buffer.getShort(), Constants.MAX_CURRENCY_DESCRIPTION_LENGTH);
        this.type = buffer.get();
        this.initialSupplyQNT = buffer.getLong();
        this.reserveSupplyQNT = buffer.getLong();
        this.maxSupplyQNT = buffer.getLong();
        this.issuanceHeight = buffer.getInt();
        this.minReservePerUnitNQT = buffer.getLong();
        this.minDifficulty = buffer.get() & 0xFF;
        this.maxDifficulty = buffer.get() & 0xFF;
        this.ruleset = buffer.get();
        this.algorithm = buffer.get();
        this.decimals = buffer.get();
    }

    public CurrencyIssuanceAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.name = (String)attachmentData.get("name");
        this.code = (String)attachmentData.get("code");
        this.description = (String)attachmentData.get("description");
        this.type = ((Long)attachmentData.get("type")).byteValue();
        this.initialSupplyQNT = Convert.parseLong(attachmentData.get("initialSupplyQNT"));
        this.reserveSupplyQNT = Convert.parseLong(attachmentData.get("reserveSupplyQNT"));
        this.maxSupplyQNT = Convert.parseLong(attachmentData.get("maxSupplyQNT"));
        this.issuanceHeight = ((Long)attachmentData.get("issuanceHeight")).intValue();
        this.minReservePerUnitNQT = Convert.parseLong(attachmentData.get("minReservePerUnitNQT"));
        this.minDifficulty = ((Long)attachmentData.get("minDifficulty")).intValue();
        this.maxDifficulty = ((Long)attachmentData.get("maxDifficulty")).intValue();
        this.ruleset = ((Long)attachmentData.get("ruleset")).byteValue();
        this.algorithm = ((Long)attachmentData.get("algorithm")).byteValue();
        this.decimals = ((Long) attachmentData.get("decimals")).byteValue();
    }

    public CurrencyIssuanceAttachment(String name, String code, String description, byte type,
            long initialSupplyQNT, long reserveSupplyQNT, long maxSupplyQNT,
            int issuanceHeight, long minReservePerUnitNQT, int minDifficulty, int maxDifficulty,
            byte ruleset, byte algorithm, byte decimals) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.type = type;
        this.initialSupplyQNT = initialSupplyQNT;
        this.reserveSupplyQNT = reserveSupplyQNT;
        this.maxSupplyQNT = maxSupplyQNT;
        this.issuanceHeight = issuanceHeight;
        this.minReservePerUnitNQT = minReservePerUnitNQT;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
        this.ruleset = ruleset;
        this.algorithm = algorithm;
        this.decimals = decimals;
    }

    @Override
    protected int getMySize() {
        return 1 + Convert.toBytes(name).length + 1 + Convert.toBytes(code).length + 2 +
                Convert.toBytes(description).length + 1 + 8 + 8 + 8 + 4 + 8 + 1 + 1 + 1 + 1 + 1;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        byte[] name = Convert.toBytes(this.name);
        byte[] code = Convert.toBytes(this.code);
        byte[] description = Convert.toBytes(this.description);
        buffer.put((byte)name.length);
        buffer.put(name);
        buffer.put((byte)code.length);
        buffer.put(code);
        buffer.putShort((short) description.length);
        buffer.put(description);
        buffer.put(type);
        buffer.putLong(initialSupplyQNT);
        buffer.putLong(reserveSupplyQNT);
        buffer.putLong(maxSupplyQNT);
        buffer.putInt(issuanceHeight);
        buffer.putLong(minReservePerUnitNQT);
        buffer.put((byte)minDifficulty);
        buffer.put((byte)maxDifficulty);
        buffer.put(ruleset);
        buffer.put(algorithm);
        buffer.put(decimals);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("name", name);
        attachment.put("code", code);
        attachment.put("description", description);
        attachment.put("type", type);
        attachment.put("initialSupplyQNT", initialSupplyQNT);
        attachment.put("reserveSupplyQNT", reserveSupplyQNT);
        attachment.put("maxSupplyQNT", maxSupplyQNT);
        attachment.put("issuanceHeight", issuanceHeight);
        attachment.put("minReservePerUnitNQT", minReservePerUnitNQT);
        attachment.put("minDifficulty", minDifficulty);
        attachment.put("maxDifficulty", maxDifficulty);
        attachment.put("ruleset", ruleset);
        attachment.put("algorithm", algorithm);
        attachment.put("decimals", decimals);
    }

    @Override
    public TransactionType getTransactionType() {
        return MonetarySystemTransactionType.CURRENCY_ISSUANCE;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public byte getType() {
        return type;
    }

    public long getInitialSupplyQNT() {
        return initialSupplyQNT;
    }

    public long getReserveSupplyQNT() {
        return reserveSupplyQNT;
    }

    public long getMaxSupplyQNT() {
        return maxSupplyQNT;
    }

    public int getIssuanceHeight() {
        return issuanceHeight;
    }

    public long getMinReservePerUnitNQT() {
        return minReservePerUnitNQT;
    }

    public int getMinDifficulty() {
        return minDifficulty;
    }

    public int getMaxDifficulty() {
        return maxDifficulty;
    }

    public byte getRuleset() {
        return ruleset;
    }

    public byte getAlgorithm() {
        return algorithm;
    }

    public byte getDecimals() {
        return decimals;
    }
}
